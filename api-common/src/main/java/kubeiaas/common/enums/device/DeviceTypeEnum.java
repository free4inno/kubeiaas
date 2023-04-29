package kubeiaas.common.enums.device;

public enum DeviceTypeEnum {
    USB, PCI;

    public String toString() {
        switch (this) {
            case USB:
                return "USB";
            case PCI:
                return "PCI";
        }
        return super.toString();
    }
}
