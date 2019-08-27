import java.util.Collections;
import java.util.List;

public class AsusDevicePresence {
    private String asusUser;
    private String ipAddress;
    private int port;
    private String sshKeyPath;
    private boolean pollingActive;

    public AsusDevicePresence(String asusUser, String ipAddress, int port, String sshKeyPath) {
        this.asusUser = asusUser;
        this.ipAddress = ipAddress;
        this.port = port;
        this.sshKeyPath = sshKeyPath;
        pollingActive = false;
    }

    public List<ConnectedDevice> getConnectedDevices() {
        return Collections.emptyList();
    }

    public boolean isDeviceConnected(String macAddress) {
        return false;
    }

    public void startDevicePresencePolling(long pollingFrequency, DeviceCallback callbackFunction) {
        pollingActive = true;
    }

    public void stopDevicePresencePolling() {
        pollingActive = false;
    }

    public boolean isPollingActive() {
        return pollingActive;
    }

    public String getAsusUser() {
        return asusUser;
    }

    public void setAsusUser(String asusUser) {
        this.asusUser = asusUser;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getSshKeyPath() {
        return sshKeyPath;
    }

    public void setSshKeyPath(String sshKeyPath) {
        this.sshKeyPath = sshKeyPath;
    }
}
