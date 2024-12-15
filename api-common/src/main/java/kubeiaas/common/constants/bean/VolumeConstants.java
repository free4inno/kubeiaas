package kubeiaas.common.constants.bean;

public class VolumeConstants {
    /* DB key */
    public static final String NAME = "name";
    public static final String DESCRIPTION = "description";
    public static final String STATUS = "status";
    public static final String UUID = "uuid";
    public static final String INSTANCE_UUID = "instanceUuid";
    public static final String USAGE_TYPE = "usageType";

    /* statistics */
    public static final String TOTAL = "total";
    public static final String USED = "used";
    public static final String MNT_DIR = "mount_dir";
    public static final String MNT_FS = "mount_fs";

    /* config */
    public static final String SPILT = "/";
    public static final String STORAGE_TYPE = "file";
    public static final String DEFAULT_DISK_TYPE = "qcow2";
    // bus
    public static final String VOLUME_BUS_IDE = "ide";
    public static final String VOLUME_BUS_VIRTIO = "virtio";
    // path
    public static final String DEFAULT_NFS_SRV_PATH = "/usr/local/kubeiaas/data/";
//    public static final String VOLUME_PATH = "sys-volumes/";
    public static final String DATA_VOLUME_PATH = "data-volumes/";
    /*
    * 2024-03-08
    * 1. 为了使系统盘采用共享存储的方式，将系统盘的路径改为共享存储路径（/usr/local/kubeiaas/data/volumes）
    * 2. 使用新常量 SHARED_VOLUME_PATH 代替 VOLUME_PATH
     */
    public static final String SHARED_VOLUME_PATH = "volumes/";
    public static final String ISO_PATH = "images/iso/";
    public static final String IMAGE_PATH = ImageConstants.IMAGE_PATH;
    // suffix
    public static final String IMG_VOLUME_SUFFIX = ".img";
    public static final String WIN_VOLUME_SUFFIX = ".qcow2";
    // create loop
    public static final int CREATING_WAIT_LOOP = 30;
    public static final int CREATING_WAIT_TIME = 5;
    // mount
    public static final String DEV_PREFIX = "vd";    //这是硬盘挂载时的盘符名称，比如vda
    public static final String WIN_PREFIX = "hd";    //这是硬盘挂载时的盘符名称，比如vda
    public static final String ISO_PREFIX = "sr";    //这是ISO挂载时的盘符名称，比如sr0
    public static final String TAP_PREFIX = "v_";
    public static final String VOLUME_DEVICE_CDROM = "cdrom";
    public static final String VOLUME_DEVICE_DISK = "disk";
    public static final String VOLUME_DRIVER_TYPE_QCOW2 = "qcow2";
    public static final String VOLUME_DRIVER_TYPE_RAW = "raw";

    // resize
    public static final String SHRINK_OPTION = "--shrink";
}
