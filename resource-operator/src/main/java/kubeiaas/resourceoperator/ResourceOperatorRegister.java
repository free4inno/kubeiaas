package kubeiaas.resourceoperator;

import kubeiaas.common.constants.ComponentConstants;
import kubeiaas.resourceoperator.dao.feign.Register;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Slf4j
@Component
public class ResourceOperatorRegister {

    @Resource
    private Register register;

    private final String svcName = ComponentConstants.RESOURCE_OPERATOR;
    private final String nodeName = System.getenv("HOST_NAME");

    @Scheduled(cron = "0 0/1 * * * ?")
    private void cleanServiceList() {
        log.info("== REGISTER");
        register.register(svcName, nodeName, System.currentTimeMillis());
    }
}
