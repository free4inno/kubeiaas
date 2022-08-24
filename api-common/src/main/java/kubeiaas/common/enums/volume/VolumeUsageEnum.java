package kubeiaas.common.enums.volume;

public enum VolumeUsageEnum {

    SYSTEM, DATA, NETWORK, ISO;

    public String toString() {
        switch (this) {
            case SYSTEM:
                return "system";
            case DATA:
                return "data";
            case NETWORK:
                return "network";
            case ISO:
                return "iso";
        }
        return super.toString();
    }
}
