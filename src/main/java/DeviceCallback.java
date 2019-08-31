import java.util.Set;

@FunctionalInterface
public interface DeviceCallback {
    void callback(Set<ConnectedDevice> connectedDevices);
}
