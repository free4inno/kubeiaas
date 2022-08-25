package kubeiaas.common.utils;

import java.util.UUID;

public class UuidUtils {
    /**
     * 随机生成一个32位的uuid.
     *
     * @return string
     */
    private static final String getUuidCom = "blkid | grep sda1 | awk -F '\\\"' '{print $2}'";

    public static String getRandomUuid() {
        return UUID.randomUUID().toString().replace("-", "");
    }

    public static String getUuidByDisk() {
        return ShellUtils.getCmd(getUuidCom).replace("-", "");
    }
}
