package kubeiaas.dhcpcontroller.config;

public class DhcpConfig {
    /* config file */
    public static final String DHCP_CONF_FILE_PATH = "/etc/dhcp/dhcpd.conf";
    public static final String DHCP_CONF_FILE_TEMP_PATH = "/tmp/dhcpd.conf.tmp";

    /* command */
    public static final String DHCP_RESTART_CMD = "service dhcpd restart";

    /* template */
    public static final String BIND_VM_TEMPLATE = "host vm${hostName} {hardware ethernet ${mac};fixed-address ${ip};}\r\n";
}
