package kubeiaas.dbproxy.register;

import kubeiaas.common.constants.ComponentConstants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Slf4j
@Component
public class DBProxyRegister {

    @Resource
    private Register register;

    private static final String svcName = ComponentConstants.DB_PROXY;
    private static final String nodeName = System.getenv("HOST_NAME");

    @Scheduled(cron = "0 0/1 * * * ?")
    private void register() {
        log.info("== REGISTER");
        register.register(svcName, nodeName, System.currentTimeMillis());
    }
}
