package kubeiaas.iaascore.config;

import kubeiaas.common.bean.Host;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;

import java.util.HashMap;
import java.util.Map;

import static jdk.nashorn.internal.runtime.regexp.joni.Config.log;

@Slf4j
public class AgentConfig {
    /* constants */
    private static final String HTTP_URI = "http://";
    private static final String AGENT_PORT = ":9090";

    private static final String DHCP_HOST_IP = "";
    private static final String VNC_HOST_IP = "";

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
        // todo: use targetHostIp 选择目标 agent. <pod_ip>
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
    public static String getDhcpUri() {
        String dhcpHostIp = System.getenv(DHCP_HOST_IP);
        // todo: use targetHostIp 选择目标 agent. <pod_ip>
        return HTTP_URI + dhcpHostIp + AGENT_PORT;
    }

    /**
     * 获取 agent uri - vnc
     * @return uri
     */
    public static String getVncUri() {
        String dhcpHostIp = System.getenv(VNC_HOST_IP);
        // todo: use targetHostIp 选择目标 agent. <pod_ip>
        return HTTP_URI + dhcpHostIp + AGENT_PORT;
    }
}
