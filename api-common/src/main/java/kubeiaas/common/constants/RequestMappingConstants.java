package kubeiaas.common.constants;

/**
 * Constants for HTTP Request Mapping Constants
 */
public class RequestMappingConstants {
    /* =============== entity =============== */
    public static final String VM = "vm";
    public static final String IMAGE = "image";
    public static final String HOST = "host";
    public static final String VOLUME = "volume";
    public static final String IP_SEGMENT = "ip_segment";
    public static final String IP_USED = "ip_used";

    // -- agent component
    public static final String DHCP_C = "dhcp_c";
    public static final String VNC_C = "vnc_c";
    public static final String IMAGE_C = "image_c";
    public static final String VM_C = "vm_c";
    public static final String VOLUME_C = "volume_c";
    public static final String HOST_C = "host_c";

    /* =============== action =============== */
    public static final String TEST = "test";
    public static final String CREATE = "create";
    public static final String QUERY_ALL = "query_all";
    public static final String QUERY_ALL_BY_SINGLE_KEY = "query_all_by_single_key";
    public static final String SAVE = "save";
    public static final String HEARTBEAT = "heartbeat";

    // -- Resource Operator
    public static final String SELECT_HOST_BY_APPOINT = "select_host_by_appoint";
    public static final String SELECT_HOST_BY_OPERATOR = "select_host_by_operator";

    // -- DHCP Controller
    public static final String BIND_MAC_IP = "bind_mac_ip";

    // -- Volume Controller
    public static final String CREATE_SYSTEM_VOLUME = "create_system_volume";

    // -- Vm Controller
    public static final String CREATE_VM_INSTANCE = "create_vm_instance";

    // -- Vnc Controller
    public static final String ADD_VNC_TOKEN = "add_vnc_token";

    // -- host Controller
    public static final String CHECK_ENV = "check_env";

    /* =============== produce =============== */
    public static final String APP_JSON = "application/json";
    public static final String APP_XML = "application/xml";

}
