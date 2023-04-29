package kubeiaas.common.enums.device;

public enum DeviceStatusEnum {
    ATTACHED, AVAILABLE, UNREACHABLE;

    public String toString() {
        switch (this) {
            case ATTACHED:
                return "attached";
            case AVAILABLE:
                return "available";
            case UNREACHABLE:
                return "unreachable";
        }
        return super.toString();
    }
}
