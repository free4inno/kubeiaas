package kubeiaas.iaascore.config;

import kubeiaas.common.bean.Host;
import lombok.extern.slf4j.Slf4j;

public class AgentConfig {
    /* constants */
    private static final String HTTP_URI = "http://";
    private static final String AGENT_PORT = ":9090";

    private static final String DHCP_HOST_IP = "DHCP_HOST_IP";
    private static final String VNC_HOST_IP = "VNC_HOST_IP";

    /* select agent's host ip */
    private static String selected_host_ip = "";

    /**
     * 设置 target host
     * @param host 目标 agent 所在 host
     */
    public static void setSelectedHost(Host host) {
        selected_host_ip = host.getIp();
    }

    /**
     * 获取 agent uri - 主动选择
     * @return uri
     */
    public static String getSelectedUri() {
        // todo: use targetHostIp 选择目标 agent.
        return HTTP_URI + selected_host_ip + AGENT_PORT;
    }

    /**
     * 获取 agent uri - dhcp
     * @return uri
     */
    public static String getDhcpUri() {
        String dhcpHostIp = System.getenv(DHCP_HOST_IP);
        // todo: use targetHostIp 选择目标 agent.
        return HTTP_URI + dhcpHostIp + AGENT_PORT;
    }

    /**
     * 获取 agent uri - vnc
     * @return uri
     */
    public static String getVncUri() {
        String dhcpHostIp = System.getenv(VNC_HOST_IP);
        // todo: use targetHostIp 选择目标 agent.
        return HTTP_URI + dhcpHostIp + AGENT_PORT;
    }
}
