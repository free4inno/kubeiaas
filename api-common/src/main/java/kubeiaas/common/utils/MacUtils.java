package kubeiaas.common.utils;

import kubeiaas.common.enums.network.IpTypeEnum;
import org.apache.commons.lang.math.RandomUtils;

/**
 * Mac工具类，以随机方式生成一个可用的Mac地址，一般00为私网Mac，01为公网开头.
 */
public class MacUtils {

    // 转到大写
    // 00:20:33:b4:2c:40 -> 00:20:33:B4:2C:40
    public static String format(String macAddress) {
        macAddress = macAddress.toUpperCase();
        macAddress = macAddress.replaceAll("^([0-9a-zA-Z])(?=:)", "0$1");
        macAddress = macAddress.replaceAll("(?<=:)([0-9a-zA-Z])(?=:)", "0$1");
        macAddress = macAddress.replaceAll("(?<=:)([0-9a-zA-Z])$", "0$1");
        return macAddress;
    }

    // 删除冒号
    public static String deleteAllColon(String mac) {
        String[] macs = mac.split(":");
        return macs[macs.length - 2] +
                macs[macs.length - 1];
    }

    /**
     * getMacPre 根据 clusterId 生成，区分 private 和public
     * 根据id和netSegment生产MAC地址的前两个字节  XX:XX，一共16bit.
     *
     * @param id         分配12个bit，最多允许4096个机房
     * @param netSegment 分配4个bit，最多允许16个段
     * @return String
     */
    public static String getMacPre(Integer id, IpTypeEnum netSegment) {
        assert (id >= 0 && id < 4096);
        int segment = -1;
        if (netSegment.equals(IpTypeEnum.PRIVATE)) {
            segment = 0;
        }
        if (netSegment.equals(IpTypeEnum.PUBLIC)) {
            segment = 1;
        }
        id = (id << 4) + segment;
        String upper = Integer.toHexString((id & 0x0000ff00) >>> 8);
        String lower = Integer.toHexString(id & 0x000000ff);
        return upper.toUpperCase() + ":" + lower.toUpperCase();
    }

    /**
     * @return String
     * @Title: getMACAddress
     * @Description: 获取一个未被使用的mac
     */
    public static String getMACAddress(String macPre) {
        int macId = nextMACAddress();
        String pos0 = Integer.toHexString(((macId & 0xff000000) >>> 24));
        String pos1 = Integer.toHexString(((macId & 0x00ff0000) >>> 16));
        String pos2 = Integer.toHexString(((macId & 0x0000ff00) >>> 8));
        String pos3 = Integer.toHexString(((macId & 0x000000ff)));
        String macAddress = macPre + ":" + pos0 + ":" + pos1 + ":" + pos2 + ":" + pos3;

        macAddress = MacUtils.format(macAddress);
        return macAddress;
    }

    private static synchronized int nextMACAddress() {
        return RandomUtils.nextInt();
    }
}
