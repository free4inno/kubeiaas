package kubeiaas.iaasagent.config;

public class DhcpConfig {
    /* config file */
    public static final String DHCP_CONF_FILE_PATH = "/etc/dhcp/dhcpd.conf";
    public static final String DHCP_CONF_FILE_TEMP_PATH = "/tmp/dhcpd.conf.tmp";

    /* command */
    public static final String DHCP_RESTART_CMD = "service dhcpd restart";

    /* template */
    // 1.name (fix id) / 2.bridge / 3.subnet / 4.netmask / 5.gateway
    public static final String BIND_IP_SEG_TEMPLATE = "shared-network %s {interface %s; subnet %s netmask %s {option routers %s;}}";

    public static final String BIND_VM_TEMPLATE = "host vm${hostName} {hardware ethernet ${mac}; fixed-address ${ip};}";
    public static final String BIND_VM_TEMPLATE_PARAM_HOSTNAME = "hostName";
    public static final String BIND_VM_TEMPLATE_PARAM_MAC = "mac";
    public static final String BIND_VM_TEMPLATE_PARAM_IP = "ip";
    public static final String BIND_VM_TEMPLATE_LOG_TAG = "bind_code_gen";

    /* escape */
    public static final String ESCAPE_NEWLINE = "\n";
}
