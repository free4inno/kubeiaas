package kubeiaas.iaascore.scheduler;

import kubeiaas.iaascore.config.AgentConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;

import java.net.URI;
import java.net.URISyntaxException;

@Slf4j
@Configuration
public class VncScheduler {


    private URI getVncUri() {
        try {
            return new URI(AgentConfig.getVncUri());
        } catch (URISyntaxException e) {
            e.printStackTrace();
            log.error("build URI failed!");
            return null;
        }
    }
}
