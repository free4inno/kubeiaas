package kubeiaas.iaasagent.config;

public class VolumeConfig {
    /* command */
    public static final String CREATE_VOLUME_DISK_CMD = "qemu-img create -f %s %s %sG";
    public static final String CREATE_VOLUME_OS_WITH_BLOCK_SIZE_CMD = "qemu-img create -f %s %s -b %s %sG";
    public static final String CREATE_VOLUME_OS_WITHOUT_BLOCK_SIZE_CMD = "qemu-img create -f %s %s -b %s";
    public static final String RESIZE_VOLUME_WITH_BLOCK_SIZE_CMD = "qemu-img resize %s +%sG";
    /* qemu-img resize [--object objectdef] [--image-opts] [-f fmt] [--preallocation=prealloc] [-q] [--shrink] filename [+ | -]size
    qemu-img resize [options] [filename] [newSize/+extraSize/-shrinkedSize]G
     */
    public static final String RESIZE_VOLUME_CMD = "qemu-img resize %s %s %sG";
    public static final String QUERY_NFS_MOUNT_FS = "df -B 1g %s | awk 'NR==2{print $1}'";
    public static final String QUERY_VOLUME_USED = "df -B 1g %s | awk 'NR==2{print $3}'";
    public static final String QUERY_VOLUME_TOTAL = "df -B 1g %s | awk 'NR==2{print $2}'";
    /*
    Command: qemu-img info [--object objectdef] [--image-opts] [-f fmt] [--output=ofmt] [--backing-chain] [-U] filename
    Output: virtual size: 10 GiB (10737418240 bytes)
            由于kubeiaas对volume的修改单位是GiB，所以只需要获得数字即可
     */
    public static final String QUERY_VOLUME_VIRTUAL_SIZE = "qemu-img info %s | grep 'virtual size' | awk '{print $3}'";
}
