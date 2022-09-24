package kubeiaas.common.enums.host;

public enum HostStatusEnum {
    PREPARING, READY, ERROR, CHECKING, OFFLINE;

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
        }

        return super.toString();
    }
}
