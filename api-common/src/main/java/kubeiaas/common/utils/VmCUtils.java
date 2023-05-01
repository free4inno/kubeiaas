package kubeiaas.common.utils;

//import java.security.MessageDigest;

/**
 * 工具方法.
 */
public class VmCUtils {
    //--------------------------工具方法：以后考虑单独放在util--------------------------------

    /**
     * 内存的单位转换，传入的是GB单位，传给libvirt配置文件的是KiB.
     *
     * @param instanceMemory 10
     * @return.
     */
    public static long memUnitConvert(Integer instanceMemory) {
        return instanceMemory * 1024 * 1024;
    }

    /**
     * 去除UUID中的-，改为系统统一格式.
     *
     * @param instanceUUID uuid
     * @return.
     */
    public static String convertUUID(String instanceUUID) {
        return instanceUUID.replace("-", "");
    }

    /**
     * 带宽的单位转换，传入的是Mb单位，传给libvirt配置文件的是Kb.
     *
     * @param maxBandwidth int
     * @return.
     */
    public static long bandwidthUnitConvert(Integer maxBandwidth) {
        return maxBandwidth * 1024 / 8;
    }

    /**
     * 生成名称：vm.
     *
     * @param uuid uuid
     * @param name vm-name
     * @return.
     */
    public static String generateName(String uuid, String name) {
        if (name == null || name.isEmpty()) {
            return uuid;
        } else {
            return uuid + "-" + name;
        }
    }

    /**
     * 生成名称：security group.
     *
     * @param uuid .
     * @return String.
     */
    public static String generateName(String uuid) {
        return uuid;
    }

    public static String getVNCPasswd(Integer id, String uuid) {
        // 6.2.0 以上的 QEMU 要求 VNC 密码不超过 8 位
        String passwd = id + uuid;
        return passwd.substring(0, 8);
    }

}
