package kubeiaas.iaascore.utils;

import kubeiaas.common.constants.bean.VolumeConstants;
import kubeiaas.common.enums.image.ImageFormatEnum;

public class ImageUtils {

    public static String getImageSuffix(ImageFormatEnum formatEnum) {
        switch (formatEnum) {
            case IMAGE:
                return VolumeConstants.IMG_VOLUME_SUFFIX;
            case QCOW2:
                return VolumeConstants.WIN_VOLUME_SUFFIX;
            default:
                return null;
        }
    }
}
