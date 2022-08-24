package kubeiaas.common.enums.image;

public enum ImageStatusEnum {
    AVAILABLE, CREATING, DELETING, ERROR, ERROR_CREATING, ERROR_DELETING, PREPARE;

    public String toString() {
        switch (this) {
            case AVAILABLE:
                return "available";
            case CREATING:
                return "creating";
            case DELETING:
                return "deleting";
            case ERROR:
                return "error";
            case ERROR_DELETING:
                return "error_deleting";
            case PREPARE:
                return "prepare";
            case ERROR_CREATING:
                return "error_creating";
        }

        return super.toString();
    }
}
