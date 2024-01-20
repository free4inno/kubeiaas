package kubeiaas.common.enums.image;

public enum ImageUploadingStatusEnum {
    UPLOADING, UPLOADED, ERROR;

    public String toString() {
        switch (this) {
            case UPLOADING:
                return "uploading";
            case UPLOADED:
                return "uploaded";
            case ERROR:
                return "error";
        }
        return super.toString();
    }
}
