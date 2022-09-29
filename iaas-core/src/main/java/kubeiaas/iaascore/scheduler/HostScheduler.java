package kubeiaas.iaascore.scheduler;


import kubeiaas.iaascore.config.AgentConfig;
import kubeiaas.iaascore.dao.feign.HostController;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;

import javax.annotation.Resource;
import java.net.URI;
import java.net.URISyntaxException;

@Slf4j
@Configuration
public class HostScheduler {

    @Resource
    private HostController hostController;

    public void configEnv(String hostIp, String type) {
        hostController.configEnv(getHostUri(hostIp), type);
    }

    private URI getHostUri(String hostIp) {
        try {
            return new URI(AgentConfig.getHostUri(hostIp));
        } catch (URISyntaxException e) {
            e.printStackTrace();
            log.error("build URI failed!");
            return null;
        }
    }
}
