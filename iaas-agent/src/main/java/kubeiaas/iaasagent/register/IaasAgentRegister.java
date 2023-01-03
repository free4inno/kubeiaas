package kubeiaas.iaasagent.register;

import kubeiaas.common.constants.ComponentConstants;
import kubeiaas.common.utils.ShellUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Slf4j
@Component
public class IaasAgentRegister {

    @Resource
    private Register register;

    private static final String svcName = ComponentConstants.IAAS_AGENT;
    private static final String nodeName = System.getenv("HOST_NAME");

    @Scheduled(cron = "0 0/1 * * * ?")
    private void register() {
        log.info("== REGISTER");
        register.register(svcName, nodeName, System.currentTimeMillis());
        if (checkLibvirt()) {
            register.register(ComponentConstants.LIBVIRT, nodeName, System.currentTimeMillis());
        }
        if (checkDHCP()) {
            register.register(ComponentConstants.DHCP, nodeName, System.currentTimeMillis());
        }
        if (checkVNC()) {
            register.register(ComponentConstants.VNC, nodeName, System.currentTimeMillis());
        }
    }

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
