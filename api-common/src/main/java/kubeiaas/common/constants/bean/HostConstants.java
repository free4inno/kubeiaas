package kubeiaas.common.constants.bean;

public class HostConstants {
    /* DB key */
    public static final String UUID = "uuid";
    public static final String IP = "ip";
    public static final String ROLE = "role";

    /* statistics */
    public static final String TOTAL = "total";
    public static final String ACTIVE = "active";

    public static final String TOTAL_vCPU = "total_vcpu";
    public static final String USED_vCPU = "used_vcpu";
    public static final String TOTAL_MEM = "total_mem";
    public static final String USED_MEM = "used_mem";
    public static final String TOTAL_STORAGE = "total_storage";
    public static final String USED_STORAGE = "used_storage";

    // roles
    public static final String ROLE_DHCP = "dhcp";
    public static final String ROLE_VNC = "vnc";
    public static final String ROLE_MNT = "mnt";

    // checker
    public static final String CHECKER_DIR = "dir";
    public static final String CHECKER_KVM = "kvm";
    public static final String CHECKER_MNT = "mnt";
    public static final String CHECKER_MNT_EXPORT = "mnt-export";
    public static final String CHECKER_LIBVIRT = "libvirt";
    public static final String CHECKER_DHCP = "dhcp";
    public static final String CHECKER_VNC = "vnc";

}
