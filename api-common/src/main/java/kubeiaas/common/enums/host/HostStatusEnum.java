package kubeiaas.common.enums.host;

public enum HostStatusEnum {
    PREPARING, READY, ERROR, CHECKING, OFFLINE, CONFIGURING;

    public String toString() {
        switch (this) {
            case PREPARING:
                return "preparing";
            case READY:
                return "ready";
            case ERROR:
                return "error";
            case CHECKING:
                return "checking";
            case OFFLINE:
                return "offline";
            case CONFIGURING:
                return "configuring";
        }

        return super.toString();
    }
}
