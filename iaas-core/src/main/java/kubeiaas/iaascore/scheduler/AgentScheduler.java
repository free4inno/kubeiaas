package kubeiaas.iaascore.scheduler;

import kubeiaas.common.bean.Host;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
public class AgentScheduler {

    private static String targetHostIp = "";

    public void setTargetHost(Host host) {
        targetHostIp = host.getIp();
    }

    public String getAgentRequestUri() {
        // todo: use targetHostIp 选择目标 agent.
        return "http://" + targetHostIp + ":9090";
    }


}
