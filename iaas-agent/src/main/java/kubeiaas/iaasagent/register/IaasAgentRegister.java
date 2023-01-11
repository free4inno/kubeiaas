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
        register.register(svcName, nodeName, System.currentTimeMillis());
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

    private final String CHECK_SUCCESS = "1";

    private static final String CMD_CHECK_LIBVIRT = "if [[ $(virsh version) =~ \"QEMU\" ]]; then echo \"1\"; else echo \"0\"; fi";
    private boolean checkLibvirt() {
        String res = ShellUtils.getCmd(CMD_CHECK_LIBVIRT);
        return res.contains(CHECK_SUCCESS);
    }

    private static final String CMD_CHECK_DHCP = "if [[ $(service dhcpd status) =~ \"active\" ]]; then echo \"1\"; else echo \"0\"; fi";
    private boolean checkDHCP() {
        String res = ShellUtils.getCmd(CMD_CHECK_DHCP);
        return res.contains(CHECK_SUCCESS);
    }

    private static final String CMD_CHECK_VNC = "if [[ $(ps -ef | grep noVNC) =~ \"websockify\" ]]; then echo \"1\"; else echo \"0\"; fi";
    private boolean checkVNC() {
        String res = ShellUtils.getCmd(CMD_CHECK_VNC);
        return res.contains(CHECK_SUCCESS);
    }
}
