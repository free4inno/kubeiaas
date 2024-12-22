package kubeiaas.iaascore.utils;

public class LogUtils {

    public static String formatUuidInjection(String uuid1, String uuid2) {
        return String.format("%s, %s", uuid1, uuid2);
    }

    public static String formatUuidInjection(String uuid1, String uuid2, String uuid3) {
        return String.format("%s, %s, %s", uuid1, uuid2, uuid3);
    }

    public static String appendUuidInjection(String currStr, String uuid) {
        return String.format("%s, %s", currStr, uuid);
    }

}
