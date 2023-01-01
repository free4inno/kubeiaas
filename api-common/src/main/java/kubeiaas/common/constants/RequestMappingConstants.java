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
    public static final String SPEC_CONFIG = "spec_config";
    public static final String SERVICE = "service";

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
    public static final String DELETE = "delete";
    public static final String ATTACH = "attach";
    public static final String DETACH = "detach";
    public static final String DELETE_BY_UUID = "delete_by_uuid";
    public static final String DELETE_ALL_BY_UUID = "delete_all_by_uuid";
    public static final String SAVE = "save";
    public static final String UPDATE = "update";
    public static final String REDUCE = "reduce";
    public static final String EDIT = "edit";
    public static final String STOP = "stop";
    public static final String START = "start";
    public static final String REBOOT = "reboot";
    public static final String SUSPEND = "suspend";
    public static final String RESUME = "resume";
    public static final String HEARTBEAT = "heartbeat";
    public static final String SET_ROLE = "set_role";
    public static final String VNC_URL = "vnc_url";
    // -- query all
    public static final String QUERY_ALL = "query_all";
    public static final String QUERY_ALL_BY_SINGLE_KEY = "query_all_by_single_key";
    public static final String QUERY_ALL_BY_TYPE = "query_all_by_type";
    public static final String QUERY_ALL_LIKE_BY_SINGLE_KEY = "query_all_like_by_single_key";
    public static final String QUERY_ALL_DATA_VOLUME = "query_all_data_volume";
    // -- fuzzy query
    public static final String FUZZY_QUERY = "fuzzy_query";
    public static final String FUZZY_QUERY_ATTACH = "fuzzy_query_attach";
    public static final String FUZZY_QUERY_DATA_VOLUME = "fuzzy_query_data_volume";
    // -- page query
    public static final String PAGE_QUERY_ALL = "page_query_all";
    public static final String PAGE_FUZZY_QUERY = "page_fuzzy_query";
    public static final String PAGE_QUERY_ALL_DATA_VOLUME = "page_query_all_data_volume";
    // -- query by
    public static final String QUERY_BY_UUID = "query_by_uuid";

    // >>> Resource Operator <<<
    public static final String SELECT_HOST_BY_APPOINT = "select_host_by_appoint";
    public static final String SELECT_HOST_BY_HOST_UUID = "select_host_by_host_uuid";
    public static final String SELECT_HOST_BY_OPERATOR = "select_host_by_operator";
    public static final String SELECT_HOST_BY_HOST_OPERATOR = "select_host_by_host_operator";

    // >>> Image Operator <<<
    public static final String QUERY_IMAGE_BY_UUID = "query_image_by_uuid";
    public static final String QUERY_IMAGE_RAW_BY_UUID = "query_image_raw_by_uuid";
    public static final String QUERY_IMAGE_ALL = "query_image_all";
    public static final String PUBLISH_IMAGE = "publish_image";
    public static final String VOLUME_PUBLISH_IMAGE = "volume_publish_image";
    public static final String IMAGE_CREATE_YAML = "image_create_yaml";
    public static final String IMAGE_SAVE_YAML = "image_save_yaml";

    // >>> DHCP Controller <<<
    public static final String BIND_MAC_IP = "bind_mac_ip";
    public static final String UNBIND_MAC_IP = "unbind_mac_ip";

    // >>> Volume Controller <<<
    public static final String CREATE_SYSTEM_VOLUME = "create_system_volume";
    public static final String CREATE_DATA_VOLUME = "create_data_volume";
    public static final String DELETE_SYSTEM_VOLUME = "delete_system_volume";
    public static final String DELETE_DATA_VOLUME = "delete_data_volume";
    public static final String ATTACH_DATA_VOLUME = "attach_data_volume";
    public static final String DETACH_DATA_VOLUME = "detach_data_volume";

    // >>> Vm Controller <<<
    public static final String CREATE_VM_INSTANCE = "create_vm_instance";
    public static final String DELETE_VM_INSTANCE = "delete_vm_instance";
    public static final String STOP_VM_INSTANCE = "stop_vm_instance";
    public static final String START_VM_INSTANCE = "start_vm_instance";
    public static final String REBOOT_VM_INSTANCE = "reboot_vm_instance";
    public static final String SUSPEND_VM_INSTANCE = "suspend_vm_instance";
    public static final String RESUME_VM_INSTANCE = "resume_vm_instance";
    public static final String MODIFY_VM_INSTANCE = "modify_vm_instance";

    // >>> Vnc Controller <<<
    public static final String ADD_VNC_TOKEN = "add_vnc_token";
    public static final String FLUSH_VNC_TOKEN = "flush_vnc_token";
    public static final String DELETE_VNC_TOKEN = "delete_vnc_token";

    // >>> host Controller <<<
    public static final String CHECK_ENV = "check_env";
    public static final String CONFIG_ENV = "config_env";

    // >>> service Controller <<<
    public static final String REGISTER = "register";
    public static final String QUERY_SERVICE = "query_service";
    public static final String QUERY_STORAGE = "query_storage";

    /* =============== produce =============== */
    public static final String APP_JSON = "application/json";
    public static final String APP_XML = "application/xml";

}
