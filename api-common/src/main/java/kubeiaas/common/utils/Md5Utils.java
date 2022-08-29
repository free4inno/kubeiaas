package kubeiaas.common.utils;

import java.math.BigInteger;
import java.security.MessageDigest;

public class Md5Utils {
    /**
     * 0x0F.
     */
    public static int mask = 15;
    private static MessageDigest md5 = null;

    static {
        try {
            md5 = MessageDigest.getInstance("MD5");
        } catch (Exception e) {
            System.out.println(e.toString());
            e.printStackTrace();
        }
    }

    /**
     * 用于获取一个String的md5值.
     *
     * @param str uuid
     * @return.
     */
    public static String getMd5(String str) {
        byte[] bs = md5.digest(str.getBytes());
        StringBuilder sb = new StringBuilder(40);
        for (int b : bs) {
            int x = b;
            if (x < 0) {
                x += 256;
            }
            sb.append(Integer.toHexString(x));
        }
        return sb.toString();
    }

    /**
     * 用于获取一个String的md5数值.
     *
     * @param str uuid
     * @return.
     */
    public static BigInteger getMd5Num(String str) {
        byte[] bs = md5.digest(str.getBytes());
        BigInteger num = BigInteger.ZERO;
        for (int b : bs) {
            int x = b;
            if (x < 0) {
                x += 256; //+1,0000,0000
            }
            num = num.shiftLeft(4);
            num = num.add(BigInteger.valueOf(x & mask));
        }
        return num;
    }
}
