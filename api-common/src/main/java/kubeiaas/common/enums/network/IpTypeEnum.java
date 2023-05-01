package kubeiaas.common.enums.network;

/**
 * network type .
 */
public enum IpTypeEnum {

    PRIVATE, PUBLIC;

    // 对应数字，0表示私网，1表示公网
    public static Integer toInteger(IpTypeEnum ipTypeEnum) {
        switch (ipTypeEnum) {
            case PRIVATE:
                return 0;
            case PUBLIC:
                return 1;
            default:
                return null;
        }
    }

    public String toString() {
        switch (this) {
            case PRIVATE:
                return "private";
            case PUBLIC:
                return "public";
        }
        return super.toString();
    }
}
