package kubeiaas.common.enums.image;

public enum ImageOSArchEnum {
    X86, I386;

    public String toString() {
        switch (this) {
            case X86:
                return "x86";
            case I386:
                return "i386";
        }
        return super.toString();
    }
}
