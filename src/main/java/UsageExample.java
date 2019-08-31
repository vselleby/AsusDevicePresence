import com.jcraft.jsch.JSchException;

import java.util.Set;

public class UsageExample {
    private final DeviceCallback deviceCallback = this::deviceCallback;
    private int receivedCallbacks;
    private AsusDevicePresence asusDevicePresence;

    private void deviceCallback(Set<ConnectedDevice> connectedDevices) {
        connectedDevices.forEach(System.out::println);
        if (receivedCallbacks++ > 5) {
            stop();
        }
    }

    private void initialise() {
        asusDevicePresence = new AsusDevicePresence("vselleby", "192.168.1.1", 449, "~/.ssh/asus_rsa");
        try {
            asusDevicePresence.connect();
            asusDevicePresence.startDevicePresencePolling(10000, deviceCallback);
        } catch (JSchException e) {
            System.out.println("Opsie, something went wrong here: " + e.getMessage());
            stop();
        }
    }

    private void stop() {
        asusDevicePresence.disconnect();
    }

    public static void main(String[] args) {
        UsageExample usageExample = new UsageExample();
        usageExample.initialise();
    }
}
