package kubeiaas.common.enums.image;

public enum ImageOSModeEnum {
    HVM, XEN, LXC, UML;

    public String toString() {
        switch (this) {
            case HVM:
                return "hvm";
            case XEN:
                return "xen";
            case LXC:
                return "lxc";
            case UML:
                return "uml";
        }
        return super.toString();
    }
}
