package kubeiaas.iaascore.config;

import kubeiaas.common.bean.Host;
import kubeiaas.common.constants.bean.HostConstants;
import kubeiaas.iaascore.dao.TableStorage;
import kubeiaas.iaascore.exception.BaseException;
import lombok.AllArgsConstructor;
import lombok.Data;
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
    private static final String AGENT_PORT = ":32201";

    @Data
    @AllArgsConstructor
    private static class Agent {
        private String ip;
        private int cnt;

        public boolean isEmpty() {
            return null == ip || ip.isEmpty() || cnt <= 0;
        }

        public void add() { cnt++; }

        public void pop() { cnt--; }
    }

    /**
     *  select agent's host ip
     *
     *  < vmUuid     : Agent >
     *  < volumeUuid : Agent >
     *
     *  缓存当前访问 uuid 与 host 关联关系
     */
    private static final Map<String, Agent> selected_host_ip = new HashMap<>();

    public static void setSelectedHost(String uuid, Host host) {
        Agent agent = selected_host_ip.get(uuid);
        if (agent == null) {
            selected_host_ip.put(uuid, new Agent(host.getIp(), 1));
            return;
        }
        if (agent.isEmpty()) {
            selected_host_ip.remove(uuid);
            selected_host_ip.put(uuid, new Agent(host.getIp(), 1));
        } else {
            agent.add();
            selected_host_ip.put(uuid, agent);
        }

    }

    public static void clearSelectedHost(String uuid) {
        Agent agent = selected_host_ip.get(uuid);
        if (agent == null) return;
        if (agent.isEmpty()) {
            selected_host_ip.remove(uuid);
        } else {
            agent.pop();
            selected_host_ip.put(uuid, agent);
        }
    }

    public static String getSelectedUri(String uuid) {
        Agent agent = selected_host_ip.get(uuid);
        if (agent == null || agent.isEmpty()) {
            log.error("getSelectedUri -- no selected ip for " + uuid);
            return "";
        }
        return HTTP_URI + agent.getIp() + AGENT_PORT;
    }

    /**
     * ===== 根据节点角色获取 URI =====
     */
    public String getDhcpUri() throws BaseException {
        // analyze from DB hostRoles.
        Host host = tableStorage.hostQueryByRole(HostConstants.ROLE_DHCP);
        if (host == null) {
            throw new BaseException("getDhcpUri -- dhcp host not found!");
        }

        return HTTP_URI + host.getIp() + AGENT_PORT;
    }

    public String getVncUri() throws BaseException {
        // analyze from DB hostRoles.
        Host host = tableStorage.hostQueryByRole(HostConstants.ROLE_VNC);
        if (host == null) {
            throw new BaseException("getVncUri -- vnc host not found!");
        }

        return HTTP_URI + host.getIp() + AGENT_PORT;
    }

    /**
     * ===== 通过 Host 直接获取 URI =====
     */
    public static String getHostUri(Host host) {
        return HTTP_URI + host.getIp() + AGENT_PORT;
    }
}
