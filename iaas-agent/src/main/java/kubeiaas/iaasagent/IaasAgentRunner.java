package kubeiaas.iaasagent;

import kubeiaas.iaasagent.config.HostConfig;
import kubeiaas.iaasagent.register.IaasAgentRegister;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Component
public class IaasAgentRunner implements ApplicationRunner {

    @Resource
    private HostConfig hostConfig;

    @Resource
    private IaasAgentRegister iaasAgentRegister;

    @Override
    public void run(ApplicationArguments args) {
        hostConfig.hostInitialize();
        iaasAgentRegister.agentRegister();
    }
}
