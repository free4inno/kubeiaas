package kubeiaas.common.enums.image;

public enum ImageFormatEnum {
    IMAGE, ISO, QCOW2;

    public String toString() {
        switch (this) {
            case IMAGE:
                return "image";
            case ISO:
                return "iso";
            case QCOW2:
                return "qcow2";
        }
        return super.toString();
    }
}
