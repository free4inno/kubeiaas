package kubeiaas.common.enums.device;

public enum DeviceTypeEnum {
    USB, PCI, MDEV;

    public String toString() {
        switch (this) {
            case USB:
                return "USB";
            case PCI:
                return "PCI";
            case MDEV:
                return "MDEV";
        }
        return super.toString();
    }
}
