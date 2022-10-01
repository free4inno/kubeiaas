package kubeiaas.iaascore.config;

import kubeiaas.common.bean.Host;
import kubeiaas.common.constants.bean.HostConstants;
import kubeiaas.iaascore.dao.TableStorage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Configuration
public class AgentConfig {

    @Resource
    private TableStorage tableStorage;

    /* constants */
    private static final String HTTP_URI = "http://";
    private static final String AGENT_PORT = ":9090";

    private static final String DHCP_HOST_IP = "DHCP_HOST_IP";
    private static final String VNC_HOST_IP = "VNC_HOST_IP";

    /* select agent's host ip */
    // < vmUuid : hostIp >
    private static Map<String, String> selected_host_ip = new HashMap<>();

    /**
     * 设置 target host
     * @param vmUuid
     * @param host 目标 agent 所在 host
     */
    public static void setSelectedHost(String vmUuid, Host host) {
        selected_host_ip.put(vmUuid, host.getIp());
    }

    /**
     * 清除 target host
     * @param vmUuid
     */
    public static void clearSelectedHost(String vmUuid) {
        selected_host_ip.remove(vmUuid);
    }

    /**
     * 获取 agent uri - 主动选择
     * @return uri
     */
    public static String getSelectedUri(String vmUuid) {
        String selectedIp = selected_host_ip.get(vmUuid);
        if (selectedIp == null || selectedIp.isEmpty()) {
            log.error("getSelectedUri -- no selected ip for " + vmUuid);
            return "";
        }
        return HTTP_URI + selectedIp + AGENT_PORT;
    }

    /**
     * 获取 agent uri - dhcp
     * @return uri
     */
    public String getDhcpUri() {
        // [old] direct from ENV variable.
        //String dhcpHostIp = System.getenv(DHCP_HOST_IP);

        // [new] analyze from DB hostRoles.
        Host host = tableStorage.hostQueryByRole(HostConstants.ROLE_DHCP);

        return HTTP_URI + host.getIp() + AGENT_PORT;
    }

    /**
     * 获取 agent uri - vnc
     * @return uri
     */
    public String getVncUri() {
        // [old] direct from ENV variable.
        //String dhcpHostIp = System.getenv(VNC_HOST_IP);

        // [new] analyze from DB hostRoles.
        Host host = tableStorage.hostQueryByRole(HostConstants.ROLE_VNC);

        return HTTP_URI + host.getIp() + AGENT_PORT;
    }

    /**
     * 获取 agent uri - host
     * @return uri
     */
    public static String getHostUri(String hostIp) {
        return HTTP_URI + hostIp + AGENT_PORT;
    }
}
