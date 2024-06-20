package kubeiaas.common.enums.vm;

public enum VmBootEnum {
    DISK, CDROM;

    public static VmBootEnum fromString(String boot) {
        if (boot == null) {
            return null;
        }
        switch (boot) {
            case "hd":
                return DISK;
            case "cdrom":
                return CDROM;
            default:
                return null;
        }
    }
}
