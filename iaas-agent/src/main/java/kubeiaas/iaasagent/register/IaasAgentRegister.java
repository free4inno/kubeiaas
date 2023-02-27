package kubeiaas.iaasagent.register;

import kubeiaas.common.constants.ComponentConstants;
import kubeiaas.common.enums.host.HostStatusEnum;
import kubeiaas.common.utils.ShellUtils;
import kubeiaas.iaasagent.service.HostService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Slf4j
@Component
public class IaasAgentRegister {

    @Resource
    private Register register;

    @Resource
    private HostService hostService;

    private static final String svcName = ComponentConstants.IAAS_AGENT;
    private static final String nodeName = System.getenv("HOST_NAME");

    public void agentRegister() {
        log.info("== AGENT REGISTER");
        try {
            register.register(svcName, nodeName, System.currentTimeMillis());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Scheduled(cron = "0 0/1 * * * ?")
    private void register() {
        log.info("== REGISTER");
        boolean errorFlag = false;

        // == 1. Agent =====
        register.register(svcName, nodeName, System.currentTimeMillis());

        // == 2. Libvirt =====
        if (checkLibvirt()) {
            register.register(ComponentConstants.LIBVIRT, nodeName, System.currentTimeMillis());
        } else {
            errorFlag = true;
        }

        // == 3. DHCP =====
        if (checkDHCP()) {
            register.register(ComponentConstants.DHCP, nodeName, System.currentTimeMillis());
        }

        // == 4. VNC ======
        if (checkVNC()) {
            register.register(ComponentConstants.VNC, nodeName, System.currentTimeMillis());
        }

        // update hostNode status
        if (errorFlag) {
            hostService.setHostStatus(HostStatusEnum.ERROR);
        } else {
            hostService.setHostStatus(HostStatusEnum.READY);
        }
    }

    // ----------------------------------------------------------------------------------------------------------------

    private static final String CMD_CHECK_LIBVIRT = "systemctl status libvirtd";
    private boolean checkLibvirt() {
        String res = ShellUtils.getCmd(CMD_CHECK_LIBVIRT);
        return res.contains("active");
    }

    private static final String CMD_CHECK_DHCP = "service dhcpd status";
    private boolean checkDHCP() {
        String res = ShellUtils.getCmd(CMD_CHECK_DHCP);
        return res.contains("active");
    }

    private static final String CMD_CHECK_VNC = "ps -ef | grep noVNC";
    private boolean checkVNC() {
        String res = ShellUtils.getCmd(CMD_CHECK_VNC);
        return res.contains("websockify");
    }
}
