package kubeiaas.iaasagent.config;

public class VolumeConfig {
    /* command */
    public static final String CREATE_VOLUME_DISK_CMD = "qemu-img create -f %s %s %sG";
    public static final String CREATE_VOLUME_OS_WITH_BLOCK_SIZE_CMD = "qemu-img create -f %s %s -b %s %sG";
    public static final String CREATE_VOLUME_OS_WITHOUT_BLOCK_SIZE_CMD = "qemu-img create -f %s %s -b %s";
    public static final String RESIZE_VOLUME_WITH_BLOCK_SIZE_CMD = "qemu-img resize %s +%sG";
    public static final String QUERY_NFS_MOUNT_FS = "df -B 1g %s | awk 'NR==2{print $1}'";
    public static final String QUERY_VOLUME_USED = "df -B 1g %s | awk 'NR==2{print $3}'";
    public static final String QUERY_VOLUME_TOTAL = "df -B 1g %s | awk 'NR==2{print $4}'";
}
