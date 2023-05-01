package kubeiaas.common.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * Shell Call Methods
 */
public class ShellUtils {

    /**
     * 返回 Java 调用 shell 结果，成功返回 true，失败返回 false.
     *
     * @param command 要执行的命令
     * @return boolean 执行结果
     */
    public static boolean run(String command) {
        String[] cmd = {"/bin/sh", "-c", command};
        Process p = null;
        try {
            p = Runtime.getRuntime().exec(cmd);
            try {
                p.waitFor();    // process 执行是非阻塞式的，如果立即返回，结果为空，结果一直显示不正确
                if (p.exitValue() == 0) {
                    return true;
                }
            } catch (IllegalThreadStateException | InterruptedException e) {
                e.printStackTrace();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (p != null) {
                p.destroy();
            }
        }
        return false;
    }

    /**
     * 返回 Java 调用 shell 结果，例如 cat /root/test.txt 会返回 test.txt内容.
     *
     * @param command 要执行的命令
     * @return String 执行结果
     */
    public static String getCmd(String command) {
        StringBuilder msg = new StringBuilder();
        InputStream is = null;
        BufferedReader br = null;
        try {
            String[] cmd = {"/bin/sh", "-c", command};
            Process proc = Runtime.getRuntime().exec(cmd);
            is = proc.getInputStream();
            br = new BufferedReader(new InputStreamReader(is));
            // fill msg

            // Method 1: updated by zht
            String lineStr;
            if ((lineStr = br.readLine()) != null) {
                msg.append(lineStr);
            }
            while ((lineStr = br.readLine()) != null) {
                msg.append("\n").append(lineStr);
            }

            // Method 2: old way (may cause bug, only get first line)
//            msg.append(br.readLine());
//            while (br.ready()) {
//                msg.append("\n");
//                msg.append(br.readLine());
//            }

            proc.destroy();     // destroy 此进程，可以回收相关资源，特别是“文件句柄”
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        // close BufferedReader
        if (br != null) {
            try {
                br.close();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        // close InputStream
        if (is != null) {
            try {
                is.close();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        return msg.toString();
    }

    public static String getNohupCmd(String command) {
        //String msg = "";
        StringBuilder msg = new StringBuilder();
        InputStream is = null;
        BufferedReader br = null;
        try {
            String[] cmd = new String[]{"/bin/sh", "-c", command};
            Process proc = Runtime.getRuntime().exec(cmd);
            is = proc.getInputStream();
            br = new BufferedReader(new InputStreamReader(is));
            msg.append(br.readLine());
            while (br.ready()) {
                msg.append("\n");
                msg.append(br.readLine());
            }
            // 不destroy，后台运行
            // proc.destroy();
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        if (br != null) {
            try {
                br.close();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        if (is != null) {
            try {
                is.close();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        return msg.toString();
    }
}
