package kubeiaas.common.enums.device;

public enum DeviceStatusEnum {
    ATTACHED, AVAILABLE;

    public String toString() {
        switch (this) {
            case ATTACHED:
                return "attached";
            case AVAILABLE:
                return "available";
        }
        return super.toString();
    }
}
