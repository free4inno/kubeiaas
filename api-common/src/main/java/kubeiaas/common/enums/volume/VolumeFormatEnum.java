package kubeiaas.common.enums.volume;

public enum VolumeFormatEnum {

    QCOW2, RAW, ISO;

    public String toString() {
        switch (this) {
            case QCOW2:
                return "qcow2";
            case RAW:
                return "raw";
            case ISO:
                return "iso";
        }
        return super.toString();
    }
}
