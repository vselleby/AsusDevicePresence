import java.util.Objects;

public class ConnectedDevice {
    private final String macAddress;
    private final String ipAddress;
    private final int rssi;
    private final FrequencyBand frequencyBand;

    public ConnectedDevice(String macAddress, String ipAddress, int rssi, FrequencyBand frequencyBand) {
        this.macAddress = macAddress;
        this.ipAddress = ipAddress;
        this.rssi = rssi;
        this.frequencyBand = frequencyBand;
    }

    public String getMacAddress() {
        return macAddress;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public int getRssi() {
        return rssi;
    }

    public FrequencyBand getFrequencyBand() {
        return frequencyBand;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ConnectedDevice that = (ConnectedDevice) o;
        return getMacAddress().equals(that.getMacAddress());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getMacAddress());
    }

    @Override
    public String toString() {
        return "ConnectedDevice{" +
                "macAddress='" + macAddress + '\'' +
                ", ipAddress='" + ipAddress + '\'' +
                ", rssi=" + rssi +
                ", frequencyBand=" + frequencyBand +
                '}';
    }
}
