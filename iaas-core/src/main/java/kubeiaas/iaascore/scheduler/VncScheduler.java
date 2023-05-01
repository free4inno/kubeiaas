package kubeiaas.iaascore.scheduler;

import kubeiaas.iaascore.config.AgentConfig;
import kubeiaas.iaascore.dao.feign.VmController;
import kubeiaas.iaascore.dao.feign.VncController;
import kubeiaas.iaascore.exception.BaseException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;

import javax.annotation.Resource;
import java.net.URI;
import java.net.URISyntaxException;

@Slf4j
@Configuration
public class VncScheduler {

    @Resource
    private VncController vncController;

    @Resource
    private AgentConfig agentConfig;

    public void addVncToken(String uuid, String address){
        vncController.addVncToken(getVncUri(), uuid, address);
    }

    public void deleteVncToken(String vmUuid){
        vncController.deleteVncToken(getVncUri(), vmUuid);
    }

    public void flushVncToken(String vmUuid, String address){
        vncController.flushVncToken(getVncUri(), vmUuid, address);
    }

    private URI getVncUri() {
        try {
            return new URI(agentConfig.getVncUri());
        } catch (URISyntaxException | BaseException e) {
            e.printStackTrace();
            log.error("build URI failed!");
            return null;
        }
    }
}
