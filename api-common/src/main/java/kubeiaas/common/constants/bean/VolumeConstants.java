package kubeiaas.common.constants.bean;

public class VolumeConstants {
    /* DB key */
    public static final String UUID = "uuid";
    public static final String INSTANCE_UUID = "instanceUuid";

    /* config */
    public static final String SPILT = "/";
    public static final String DEFAULT_DISK_TYPE = "qcow2";
    // bus
    public static final String VOLUME_BUS_IDE = "ide";
    public static final String VOLUME_BUS_VIRTIO = "virtio";
    // path
    public static final String DEFAULT_NFS_SRV_PATH = "/srv/nfs4/";
    public static final String VOLUME_PATH = "volumes/";
    public static final String DATA_VOLUME_PATH = "data-volumes/";
    public static final String IMAGE_PATH = ImageConstants.IMAGE_PATH;
    // suffix
    public static final String IMG_VOLUME_SUFFIX = ".img";
    public static final String WIN_VOLUME_SUFFIX = ".qcow2";
    // create loop
    public static final int CREATING_WAIT_LOOP = 20;
    public static final int CREATING_WAIT_TIME = 1;
    // mount
    public static final String DEV_PREFIX = "vd";    //这是硬盘挂载时的盘符名称，比如vda
    public static final String WIN_PREFIX = "hd";    //这是硬盘挂载时的盘符名称，比如vda
    public static final String TAP_PREFIX = "v_";
    public static final String VOLUME_DEVICE_CDROM = "cdrom";
    public static final String VOLUME_DEVICE_DISK = "disk";
}
