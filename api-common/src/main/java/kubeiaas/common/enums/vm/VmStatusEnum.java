package kubeiaas.common.enums.vm;

/**
 * 虚拟机状态.
 */
public enum VmStatusEnum {

    ACTIVE, APPLYING, BUILDING, REBUILDING, PAUSED, SUSPENDED, RESCUED, DELETED, STOPPED,
    MIGRATING, RESIZING, ERROR, REJECTED;

    public String toString() {
        switch (this) {
            case ACTIVE:
                return "active";
            case APPLYING:
                return "applying";
            case BUILDING:
                return "building";
            case REBUILDING:
                return "rebuilding";
            case PAUSED:
                return "paused";
            case SUSPENDED:
                return "suspended";
            case RESCUED:
                return "rescued";
            case DELETED:
                return "deleted";
            case STOPPED:
                return "stopped";
            case MIGRATING:
                return "migrating";
            case RESIZING:
                return "resizing";
            case ERROR:
                return "error";
            case REJECTED:
                return "rejected";
        }
        return super.toString();
    }
}
