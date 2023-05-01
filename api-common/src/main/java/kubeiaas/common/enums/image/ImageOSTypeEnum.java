package kubeiaas.common.enums.image;

public enum ImageOSTypeEnum {
    LINUX, WINDOWS;

    public String toString() {
        switch (this) {
            case LINUX:
                return "Linux";
            case WINDOWS:
                return "Windows";
        }
        return super.toString();
    }
}
