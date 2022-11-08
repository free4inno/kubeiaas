package kubeiaas.common.enums.vm;

/**
 * 虚拟机状态.
 */
public enum VmStatusEnum {

    ACTIVE, STARTING ,APPLYING, BUILDING, REBUILDING, PAUSED, SUSPENDED, RESCUED, DELETED, STOPPED, STOPPING,
    MIGRATING, RESIZING, ERROR, REJECTED, REBOOTING, SUSPENDING ,RESUMING, DELETING;

    public String toString() {
        switch (this) {
            case ACTIVE:
                return "active";
            case STARTING:
                return "starting";
            case APPLYING:
                return "applying";
            case BUILDING:
                return "building";
            case REBUILDING:
                return "rebuilding";
            case PAUSED:
                return "paused";
            case SUSPENDING:
                return "suspending";
            case SUSPENDED:
                return "suspended";
            case RESCUED:
                return "rescued";
            case DELETED:
                return "deleted";
            case STOPPING:
                return "stopping";
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
            case REBOOTING:
                return "rebooting";
            case RESUMING:
                return "resuming";
            case DELETING:
                return "deleting";
        }
        return super.toString();
    }
}
