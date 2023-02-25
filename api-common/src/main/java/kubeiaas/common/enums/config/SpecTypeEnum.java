package kubeiaas.common.enums.config;

public enum SpecTypeEnum {
    VM_COMPUTE, SYS_VOLUME, DATA_VOLUME, VM_STATUS, VOLUME_STATUS, NETWORK_TYPE, VNC_DOMAIN;

    public String toString() {
        switch (this) {
            case VM_COMPUTE:
                return "vm_compute";
            case SYS_VOLUME:
                return "sys_volume";
            case DATA_VOLUME:
                return "data_volume";
            case VM_STATUS:
                return "vm_status";
            case VOLUME_STATUS:
                return "volume_status";
            case NETWORK_TYPE:
                return "network_type";
            case VNC_DOMAIN:
                return "vnc_domain";
        }
        return super.toString();
    }
}
