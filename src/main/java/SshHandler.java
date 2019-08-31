import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Semaphore;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import static java.util.Collections.emptySet;

class SshHandler {
    private static final String CONNECTED_DEVICES_PATH = "/tmp/clientlist.json";
    private final Logger logger = Logger.getLogger(SshHandler.class.getName());
    private final String asusUser;
    private final String ipAddress;
    private final int port;
    private final String sshKeyPath;
    boolean isInitialised;
    private Session session;
    private ChannelExec channel;
    private Semaphore semaphore;

    SshHandler(String asusUser, String ipAddress, int port, String sshKeyPath) {
        this.asusUser = asusUser;
        this.ipAddress = ipAddress;
        this.port = port;
        this.sshKeyPath = sshKeyPath;
        isInitialised = false;
        semaphore = new Semaphore(1);
    }

    void initialise() throws JSchException {
        logger.fine("Initialising SshHandler");
        JSch jsch = new JSch();
        jsch.addIdentity(sshKeyPath);
        session = jsch.getSession(asusUser, ipAddress, port);
        session.setConfig("StrictHostKeyChecking", "no");
        session.connect();
        isInitialised = true;
    }

    void stop() {
        logger.fine("Stopping SshHandler");
        isInitialised = false;
        if (channel != null && channel.isConnected()) {
            channel.disconnect();
        }
        if (session != null && session.isConnected()) {
            session.disconnect();
        }
    }

    synchronized Set<ConnectedDevice> readConnectedDevices() {
        try {
            logger.finest("Trying to acquire semaphore");
            semaphore.acquire();
            logger.finest("Semaphore acquired");
            channel = (ChannelExec) session.openChannel("exec");
            channel.setCommand("cat " + CONNECTED_DEVICES_PATH);
            channel.connect();
            var bufferedReader = new BufferedReader(new InputStreamReader(channel.getInputStream()));
            var deviceJsonString = bufferedReader.lines().reduce((String s1, String s2) -> {
                if (s2 != null) {
                    return s1.concat(s2);
                }
                else {
                    return s1;
                }
            });

            return deviceJsonString.map(this::parseJsonString).orElse(emptySet());

        } catch (JSchException | IOException | InterruptedException e) {
            logger.log(Level.WARNING, "Failed to read devices from Asus router", e);
            return emptySet();

        } finally {
            channel.disconnect();
            semaphore.release();
            logger.finest("Semaphore released");
        }
    }

    private Set<ConnectedDevice> parseJsonString(String jsonString) {
        var foundDevices = new HashSet<ConnectedDevice>();
        var rootElement = new JsonParser().parse(jsonString).getAsJsonObject();

        JsonObject firstEntry = rootElement.entrySet().stream().findFirst().map(entry -> entry.getValue().getAsJsonObject()).orElse(new JsonObject());
        foundDevices.addAll(getDeviceType(firstEntry, "2G"));
        foundDevices.addAll(getDeviceType(firstEntry, "5G"));
        return foundDevices;
    }

    private Set<ConnectedDevice> getDeviceType(JsonObject firstEntry, String key) {
        var nodesFound = new HashSet<ConnectedDevice>();
        var nodes = firstEntry.entrySet().stream().filter(entry -> entry.getKey().equals(key)).findFirst().map(Map.Entry::getValue);
        nodes.ifPresent(jsonElement -> nodesFound.addAll(jsonElement.getAsJsonObject().entrySet().stream().
                map(jsonEntry -> parseDevice(jsonEntry, key.equals("2G") ? FrequencyBand.TWO_GHZ : FrequencyBand.FIVE_GHZ)).
                collect(Collectors.toSet())));
        return nodesFound;
    }

    private ConnectedDevice parseDevice(Map.Entry<String, JsonElement> entry, FrequencyBand frequencyBand) {
        var macAddress = entry.getKey();
        var ipAddress = entry.getValue().getAsJsonObject().get("ip").getAsString();
        var rssi = entry.getValue().getAsJsonObject().get("rssi").getAsInt();
        return new ConnectedDevice(macAddress, ipAddress, rssi, frequencyBand);
    }
}
