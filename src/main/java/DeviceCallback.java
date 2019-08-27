import java.util.List;

@FunctionalInterface
public interface DeviceCallback {
    void callback(List<ConnectedDevice> connectedDevices);
}
