import com.jcraft.jsch.JSchException;

import java.util.Set;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import static java.util.concurrent.Executors.newSingleThreadScheduledExecutor;

public class AsusDevicePresence {
    private final String asusUser;
    private final String ipAddress;
    private final int port;
    private final String sshKeyPath;
    private SshHandler sshHandler;
    private ScheduledFuture pollingFuture;
    private ScheduledExecutorService scheduler;
    private boolean connected;

    /**
     *
     * @param asusUser Username of user allowed to SSH to router
     * @param ipAddress IP-address of router
     * @param port port open for SSH on router
     * @param sshKeyPath Path to key used for SSH connection to router
     */
    public AsusDevicePresence(String asusUser, String ipAddress, int port, String sshKeyPath) {
        this.asusUser = asusUser;
        this.ipAddress = ipAddress;
        this.port = port;
        this.sshKeyPath = sshKeyPath;
        scheduler = newSingleThreadScheduledExecutor();
        sshHandler = new SshHandler(asusUser, ipAddress, port, sshKeyPath);
        connected = false;
    }

    /**
     * Synchronously fetches a set of unique {@link ConnectedDevice} instances. Logs a warning and returns an empty set if
     * something goes wrong with the connection to the router.
     *
     * @return Set of unique {@link ConnectedDevice} instances currently connected to the router
     * @throws AsusDevicePresenceException if AsusDevicePresence.connect has not been called
     */
    public Set<ConnectedDevice> getConnectedDevices() {
        if (connected) {
            return sshHandler.readConnectedDevices();
        }
        throw new AsusDevicePresenceException("Not connected");
    }

    /**
     * Checks whether a specific device is connected or not. Logs a warning and returns false if something goes wrong with
     * the connection to the router.
     *
     * @param macAddress String representing the Mac address of the device
     * @return true if router reports the device as being connected
     * @throws AsusDevicePresenceException if AsusDevicePresence.connect has not been called
     */
    public boolean isDeviceConnected(String macAddress) {
        if (connected) {
            return sshHandler.readConnectedDevices().stream().anyMatch(device -> device.getMacAddress().equals(macAddress));
        }
        throw new AsusDevicePresenceException("Not connected");
    }

    /**
     * Schedules a Runnable that asynchronously fetches a set of {@link ConnectedDevice} and calls the callback method in
     * {@link DeviceCallback}. Will keep running until stopDevicePresencePolling is called. Logs a warning and calls
     * the callback method with an empty set if something goes wrong with the router connection.
     *
     * @param pollingFrequency {@link Long} describing frequency of Callbacks in milliseconds
     * @param callbackFunction An instance of a {@link DeviceCallback} function where a Set of {@link ConnectedDevice}
     *                         will be called once available
     * @throws AsusDevicePresenceException if AsusDevicePresence.connect has not been called
     */
    public void startDevicePresencePolling(long pollingFrequency, DeviceCallback callbackFunction) {
        if (connected) {
            pollingFuture = scheduler.scheduleAtFixedRate(() ->
                            callbackFunction.callback(sshHandler.readConnectedDevices()),
                    0, pollingFrequency, TimeUnit.MILLISECONDS);
        }
        else {
            throw new AsusDevicePresenceException("Not connected");
        }
    }

    /**
     * Stops asynchronous polling of connected devices.
     */
    public void stopDevicePresencePolling() {
        if (pollingFuture != null) {
            pollingFuture.cancel(true);
        }
    }

    /**
     * Initialises the ssh connection with the router. Has to be called before calls that fetches information from the router.
     *
     * @throws JSchException if something goes wrong when initialising the SSH connection.
     */
    public void connect() throws JSchException {
        connected = true;
        if (!sshHandler.isInitialised) {
            sshHandler.initialise();
        }
    }

    /**
     *  Disconnect from router. If this is called, connect has to be called before any subsequent calls that fetches
     *  information from the router.
     */
    public void disconnect() {
        connected = false;
        if (sshHandler.isInitialised) {
            sshHandler.stop();
        }
    }

    /**
     *
     * @return true if asynchronous polling is active
     */
    public boolean isPollingActive() {
        return pollingFuture != null && !pollingFuture.isCancelled();
    }

    public String getSshKeyPath() {
        return sshKeyPath;
    }

    public int getPort() {
        return port;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public String getAsusUser() {
        return asusUser;
    }

    /**
     *
     * @return true if connected to router through ssh
     */
    public boolean isConnected() {
        return connected;
    }
}
