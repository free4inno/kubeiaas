package kubeiaas.common.utils;

import kubeiaas.common.bean.Image;
import kubeiaas.common.constants.bean.VolumeConstants;
import kubeiaas.common.enums.image.ImageOSTypeEnum;

import java.math.BigInteger;

public class PathUtils {

    private static final BigInteger CHAR_NUM = BigInteger.valueOf(26);

    /**
     * 根据传入的a/z和uuid生成directory.
     *
     * @param destImgDir a/z/
     * @param uuid       常规的uuid
     * @return volumes/a/z/uuid.img
     */
    public static String genVolumeFilePath(String destImgDir, String uuid) {
        return VolumeConstants.VOLUME_PATH + destImgDir + uuid + VolumeConstants.IMG_VOLUME_SUFFIX;
    }

    /**
     * 根据uuid生成directory.
     *
     * @param uuid 常规的uuid
     * @return volumes/a/z/uuid.img
     */
    public static String genVolumeDirectoryByUuid(String uuid, Image image) {
        String destImgDir = getImgDir(uuid);
        if (image != null && image.getOsType().equals(ImageOSTypeEnum.WINDOWS)){
            return VolumeConstants.VOLUME_PATH + destImgDir + uuid + VolumeConstants.WIN_VOLUME_SUFFIX;
        } else {
            return VolumeConstants.VOLUME_PATH + destImgDir + uuid + VolumeConstants.IMG_VOLUME_SUFFIX;
        }
    }

    /**
     * 根据uuid生成directory.
     *
     * @param uuid 常规的uuid
     * @return volumes/a/z/uuid.img
     */
    public static String genDataVolumeDirectoryByUuid(String uuid, Image image) {
        String destImgDir = getImgDir(uuid);
        if (image != null && image.getOsType().equals(ImageOSTypeEnum.WINDOWS)){
            return VolumeConstants.DATA_VOLUME_PATH + destImgDir + uuid + VolumeConstants.WIN_VOLUME_SUFFIX;
        } else {
            return VolumeConstants.DATA_VOLUME_PATH + destImgDir + uuid + VolumeConstants.IMG_VOLUME_SUFFIX;
        }
    }

    /**
     * 根据directory生成fullPath.
     *
     * @param filePath volumes/a/z/uuid.img 或者 images/a/z/uuid.img
     * @return /srv/nfs4/volumes/a/z/uuid.img  或者 /srv/nfs4/images/a/z/uuid.img
     */
    public static String genFullPath(String filePath) {
        return VolumeConstants.DEFAULT_NFS_SRV_PATH + filePath;
    }

    /**
     * 获得文件全路径的目录，必须是以/结尾.
     *
     * @param volumeFullPath /srv/nfs4/volumes/a/z/uuid.img
     * @return /srv/nfs4/volumes/a/z
     */
    public static String genVolumePrePath(String volumeFullPath) {
        if (volumeFullPath == null || volumeFullPath.equals("")) {
            return volumeFullPath;
        }
        int lastIndex = volumeFullPath.lastIndexOf(VolumeConstants.SPILT);
        if (lastIndex != -1) {
            volumeFullPath = volumeFullPath.substring(0, lastIndex);
        }
        return volumeFullPath;
    }

    /**
     * uuid to u/j/.
     *
     * @param uuid regular uuid
     * @return directory prefix
     */
    public static String getImgDir(String uuid) {
        BigInteger md5Num = Md5Utils.getMd5Num(uuid.trim());

        // md5 % 26
        int firstCharVal = md5Num.remainder(CHAR_NUM).intValue();
        // (md5 / 26 ) % 26
        int secondCharVal = md5Num.divide(CHAR_NUM).remainder(CHAR_NUM).intValue();

        char aChar = 'a';

        char firstChar = (char) ((int) aChar + firstCharVal);
        char secondChar = (char) ((int) aChar + secondCharVal);

        return firstChar + VolumeConstants.SPILT
                + secondChar + VolumeConstants.SPILT;
    }

    /**
     * @param uuid regular uuid
     * @return images/a/z/uuid.img
     */
    public static String genImgDirectory(String uuid) {
        return VolumeConstants.IMAGE_PATH + getImgDir(uuid) + uuid + VolumeConstants.IMG_VOLUME_SUFFIX;
    }

}
