package kubeiaas.common.enums.volume;

public enum VolumeStatusEnum {

    DEFINED, AVAILABLE, DETACHED, ATTACHED, APPLYING, CREATING, DELETING, ERROR, ERROR_PREPARE, ERROR_DELETING, DELETED, USED, REJECTED;

    public String toString() {
        switch (this) {
            case DEFINED:
                return "defined";
            case AVAILABLE:
                return "available";
            case ATTACHED:
                return "attached";
            case DETACHED:
                return "detached";
            case APPLYING:
                return "applying";
            case CREATING:
                return "creating";
            case DELETING:
                return "deleting";
            case ERROR:
                return "error";
            case DELETED:
                return "deleted";
            case ERROR_DELETING:
                return "error_deleting";
            case ERROR_PREPARE:
                return "error_prepare";
            case USED:
                return "used";
            case REJECTED:
                return "rejected";
        }
        return super.toString();
    }
}
