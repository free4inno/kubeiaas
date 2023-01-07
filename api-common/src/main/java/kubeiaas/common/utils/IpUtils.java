package kubeiaas.common.utils;

import kubeiaas.common.bean.IpSegment;
import kubeiaas.common.bean.IpUsed;
import lombok.extern.slf4j.Slf4j;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.*;

/**
 * IP 工具类，例如提供一个可用的IP地址.
 */
@Slf4j
public class IpUtils {

    private static int remainIpNum = 5;

    /**
     * Total
     * 获取 IP 段内 IP 总数
     */
    public static Integer getTotalIpNum(IpSegment ipSegment) {
        int ipBegin = IpUtils.stringToInt(ipSegment.getIpRangeStart());
        int ipEnd = IpUtils.stringToInt(ipSegment.getIpRangeEnd());
        return (ipEnd - ipBegin + 1);
    }

    /**
     * Available
     * 获取 IP 段内可用 IP 数
     */
    public static Integer getAvailableIpNum(Map<String, IpUsed> usedIps, IpSegment ipSegment) {
        int remainIp = 0;

        int ipBegin = IpUtils.stringToInt(ipSegment.getIpRangeStart());
        int ipEnd = IpUtils.stringToInt(ipSegment.getIpRangeEnd());
        remainIp = remainIp + (ipEnd - ipBegin + 1);

        remainIp = remainIp - usedIps.size();
        if (remainIp < remainIpNum) {
            log.info("-- the " + ipSegment.getId() + " only " + remainIp + " ip left");
            remainIpNum = remainIp;
        }
        return remainIp;
    }

    /**
     * 为了保证获取的Ip地址唯一，应该保证在方法执行期间只能有一个进程调用.
     *
     * @param usedIps   已经使用的ip地址，通过Map方式存放，key是Ip
     * @param ipSegment IP分段
     * @return 返回一个 VmUsedIp
     */
    public static synchronized IpUsed getIpAddress(Map<String, IpUsed> usedIps, IpSegment ipSegment) {
        Set<Integer> gatewayIps = new HashSet<>();

        if (IpUtils.stringToInt(ipSegment.getGateway()) != -1) {
            gatewayIps.add(IpUtils.stringToInt(ipSegment.getGateway()));
        }
        if (IpUtils.stringToInt(ipSegment.getDns()) != -1) {
            gatewayIps.add(IpUtils.stringToInt(ipSegment.getDns()));
        }

        int ipBegin = IpUtils.stringToInt(ipSegment.getIpRangeStart());
        int ipEnd = IpUtils.stringToInt(ipSegment.getIpRangeEnd());

        for (int ip = ipBegin; ip <= ipEnd; ip++) {
            if (gatewayIps.contains(ip)) {
                continue;
            }
            if (!(usedIps.containsKey(IpUtils.intToString(ip)))) {
                String ipStr = IpUtils.intToString(ip);
                IpUsed newUsedIp = new IpUsed();
                newUsedIp.setIpSegmentId(ipSegment.getId());
                newUsedIp.setIp(ipStr);
                newUsedIp.setType(ipSegment.getType());
                return newUsedIp;
            }
        }
        log.error("there is no available ip.");
        return null;
    }

    public static String getNetmask(Integer netmask) {
        return intToString(getSegment(0xffffffff, netmask));
    }

    public static Integer getSegment(Integer ipSeg, Integer netmask) {
        return ipSeg & (-1 << (32 - netmask));
    }

    /**
     * @param ip 192.168.32.1
     * @return String
     */
    public static String intToString(Integer ip) {
        Integer[] pos = {0, 0, 0, 0};
        for (int i = 0; i < 4; i++) {
            pos[3 - i] = (ip & (0xff << (i * 8))) >>> (i * 8);
        }

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 4; i++) {
            sb.append(pos[i]);
            if (i != 3) {
                sb.append('.');
            }
        }
        return sb.toString();
    }

    /**
     * @param ipStr 192.168.33.1  -> -1062723327  -> 1111 1111 1111 1111 1111 1111 1111 1111 1100 0000 1010 1000 0010 0001 0000 0001
     * @Title: stringToIp
     * @Description: 将ip字符串转换为整数
     * @return.
     */
    public static int stringToInt(String ipStr) {
        int ip = 0;
        String[] strs = ipStr.split("\\.");
        for (int i = 0; i < 4; i++) {
            ip += Integer.parseInt(strs[i]) << ((3 - i) * 8);
        }
        return ip;
    }

    public static int getNetmaskInt(String netmask) {
        int netmaskInt = stringToInt(netmask);
        String str = Integer.toBinaryString(netmaskInt);
        int r = 0;
        for (int i = 0; i < str.length(); i++) {
            if (str.charAt(i) == '1') {
                r++;
            } else {
                break;
            }
        }
        return r;
    }

    public static boolean contain(IpSegment ipSegment, Integer ip) {
        int ipStart = stringToInt(ipSegment.getIpRangeStart());
        int ipEnd = stringToInt(ipSegment.getIpRangeEnd());
        return (ip >= ipStart && ip <= ipEnd);
    }

    /*
    public static void main(String[] args) {
        // System.out.println(getSegment(0xffffffff, 8));   // -16777216	1111 1111 0000 0000 0000 0000 0000 0000
        // System.out.println(getNetmask(8));
        // System.out.println(stringToInt("192.168.32.1"));
        System.out.println(getAllIps());
    }
    */

    //获取本机上所有的IP信息，包括127.0.0.1
    public static String getAllIps() {
        StringBuilder result = new StringBuilder();
        try {
            InetAddress candidateAddress = null;
            // 遍历所有的网络接口
            Enumeration<NetworkInterface> ifaces = NetworkInterface.getNetworkInterfaces();
            for (NetworkInterface iface = ifaces.nextElement(); ifaces.hasMoreElements(); iface = ifaces.nextElement()) {

                // 在所有的接口下再遍历IP
                for (Enumeration<InetAddress> inetAddrs = iface.getInetAddresses(); inetAddrs.hasMoreElements(); ) {
                    InetAddress inetAddr = inetAddrs.nextElement();
                    if (!inetAddr.isLoopbackAddress()) {// 排除loopback类型地址
                        if (inetAddr.isSiteLocalAddress()) {
                            // 如果是site-local地址，就是它了
                            result.append(inetAddr.toString());
                        } else if (candidateAddress == null) {
                            // site-local类型的地址未被发现，先记录候选地址
                            candidateAddress = inetAddr;
                        }
                    }
                }
            }
            if (result.toString().equals("")) {
                if (candidateAddress != null) {
                    result.append(candidateAddress.toString());
                }
            }

            if (result.toString().equals("")) {                    // 如果没有发现 non-loopback地址.只能用最次选的方案
                if (candidateAddress != null) {
                    result.append(InetAddress.getLocalHost().toString());
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return result.toString();
    }
}
