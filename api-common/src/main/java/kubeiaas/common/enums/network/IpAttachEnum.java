package kubeiaas.common.enums.network;

public enum IpAttachEnum {
    ATTACHED, DETACHED;

    public String toString(){
        switch (this) {
            case ATTACHED:
                return "attached";
            case DETACHED:
                return "detached";
        }
        return super.toString();
    }
}
