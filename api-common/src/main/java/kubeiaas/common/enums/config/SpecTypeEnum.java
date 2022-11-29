package kubeiaas.common.enums.config;

public enum SpecTypeEnum {
    VM_COMPUTE, SYS_VOLUME, DATA_VOLUME;

    public String toString() {
        switch (this) {
            case VM_COMPUTE:
                return "vm-compute";
            case SYS_VOLUME:
                return "sys-volume";
            case DATA_VOLUME:
                return "data-volume";
        }
        return super.toString();
    }
}
