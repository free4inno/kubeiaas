package kubeiaas.common.constants.bean;

public class VmConstants {
    /* DB key */
    public static final String NAME = "name";
    public static final String DESCRIPTION = "description";
    public static final String STATUS = "status";
    public static final String UUID = "uuid";
    public static final String HOST_UUID = "hostUuid";
    public static final String IMAGE_UUID = "imageUuid";

    /* default value */
    public static final String DEFAULT_DESCRIPTION = "无";       // vm's default description
    public static final String DEFAULT_PASSWORD = "abc123";     // vm's default password
    public static final int DEFAULT_DISK_SIZE = 20;             // vm's default diskSize

    /* deleteVm type */
    public static final String DELETE_FORCE = "force";
    public static final String DELETE_NORMAL = "normal";

    /* vnc config */
    // public static final String VNC_URL_TEMPLATE = "http://%s:8787/vnc.html?path=?token=%s";
    public static final String VNC_URL_TEMPLATE = "http://kubeiaasvnc.free4inno.com/vnc.html?path=?token=%s";
}
