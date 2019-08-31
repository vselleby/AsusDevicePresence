class AsusDevicePresenceException extends RuntimeException {
    private String message;

    AsusDevicePresenceException(String message) {
        this.message = message;
    }

    @Override
    public String toString() {
        return message;
    }
}
