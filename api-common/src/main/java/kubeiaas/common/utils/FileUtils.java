package kubeiaas.common.utils;

import lombok.extern.slf4j.Slf4j;

import java.io.*;
import java.util.Properties;

@Slf4j
public class FileUtils {

    private static final String FILE_SEPARATOR = "/";

    public static boolean exists(String path) {
        File file = new File(path);
        return file.exists();
    }

    public static boolean isEmptyString(Object input) {
        if (input == null) {
            return true;
        } else {
            return "".equalsIgnoreCase(input.toString());
        }
    }

    /**
     * 将文件move到目的地址.
     *
     * @param srcPath  将被拷贝的文件路径
     * @param destPath 目标文件地址
     * @throws IOException 文件不存在
     */
    public static void move(String srcPath, String destPath) throws IOException {
        move(new File(srcPath), destPath);
    }

    public static void move(File srcFile, File destFile) throws IOException {
        org.apache.commons.io.FileUtils.moveFile(srcFile, destFile);
    }

    /**
     * 将文件copy到目的地址.
     *
     * @param srcPath  将被拷贝的文件路径
     * @param destPath 目标文件地址
     * @throws IOException 文件不存在
     */
    public static void copy(String srcPath, String destPath) throws IOException {
        if (srcPath == null) {
            throw new IOException("File: srcPath should not be null!");
        }
        copy(new File(srcPath), destPath);
    }

    /**
     * 将文件copy到目的地址.
     *
     * @param srcFile  将被拷贝的文件，不可为空
     * @param destPath 目标文件地址
     * @throws IOException 文件不存在
     */
    private static void copy(File srcFile, String destPath) throws IOException {
        File destFile = new File(destPath);
        if (destFile.exists()) {
            String tmpPath = destPath.concat(".tmp");
            File tmpFile = new File(tmpPath);
            if (tmpFile.exists()) {
                if (!tmpFile.delete()) {
                    throw new IOException("When copy files, Delete previous Temp File Error!");
                }
            }
            org.apache.commons.io.FileUtils.copyFile(srcFile, new File(tmpPath));
            if (!destFile.delete()) {
                throw new IOException("When copy files, Delete Temp File Error!");
            }
            move(tmpPath, destPath);
        } else {
            org.apache.commons.io.FileUtils.copyFile(srcFile, new File(destPath));
        }
    }

    /**
     * 将文件move到目的地址.
     *
     * @param srcFile  将被拷贝的文件，不可为空
     * @param destPath 目标文件地址
     * @throws IOException 文件不存在
     */
    public static void move(File srcFile, String destPath) throws IOException {
        File destFile = new File(destPath);
        if (destFile.exists()) {
            throw new IOException("File: " + destPath + " existed!");
        }
        org.apache.commons.io.FileUtils.moveFile(srcFile, destFile);
    }

    /**
     * 清空文件.
     *
     * @param file 将要被清空的文件
     * @throws Exception 文件不存在
     */
    public static File cleanFile(File file) throws Exception {
        boolean ret = file.delete();
        if (!ret) {
            throw new Exception("delete file failed.");
        }
        ret = file.createNewFile();
        if (!ret) {
            throw new Exception("recreate file failed.");
        }
        return file;
    }

    /**
     * 强制递归创建文件夹 -> 必须是绝对路径.
     *
     * @param filePath /srv/nfs4/volumes/z/p/uuid.img
     * @return 创建的结果
     */
    public static boolean createDirIfNotExist(String filePath) {
        // filepath: /srv/nfs4/volumes/z/p/      /srv/nfs4/volumes/z/p/uuid.img
        if (filePath != null && !"".equals(filePath)) {
            if (new File(filePath).exists()) {
                return true;
            }
        } else {
            log.warn("filePath is empty!");
            return true;
        }
        int lastIndex = filePath.lastIndexOf(FILE_SEPARATOR);   // 找最后一个分隔符 /
        if (lastIndex != -1) {
            filePath = filePath.substring(0, lastIndex);
        } else {
            log.warn(filePath + " not valid");
            return true;
        }
        if (!"".equalsIgnoreCase(filePath)) {
            try {
                org.apache.tomcat.util.http.fileupload.FileUtils.forceMkdir(new File(filePath));
            } catch (IOException e) {
                log.error("无法创建文件夹" + filePath);
                log.error(e.getMessage());
                return false;
            }
        }
        return true;
    }

    /**
     * @param properties content to write, save in Properties file;
     * @param filePath   file full path, include file name, like /root/iaas/vm.conf
     * @param comment    write comment, will show in first line of File
     * @return boolean       operate result;
     */
    public static boolean writeFile(Properties properties, String filePath, String comment) {
        try {
            boolean res = createDirIfNotExist(filePath);      //if file path not exist, create it
            if (!res) {
                throw new IOException("Create new VM select File failed");
            }
            FileOutputStream oFile = new FileOutputStream(filePath);
            properties.store(oFile, comment);
            oFile.close();
        } catch (IOException e) {
            log.error("write file error!");
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public static Properties readFile(String filePath) {
        Properties properties = new Properties();
        File file = new File(filePath);
        if (!file.exists()) {
            log.warn("Configuration file is not exist, use default setting");
            return properties;
        }
        try {
            InputStream in = new BufferedInputStream(new FileInputStream(file));
            properties.load(in);     ///加载属性列表
            for (String key : properties.stringPropertyNames()) {
                log.info("read info of " + filePath + " is: " + key + " " + properties.getProperty(key));
            }
            in.close();
        } catch (IOException e) {
            e.printStackTrace();
            return properties;
        }
        return properties;
    }
}
