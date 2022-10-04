package kubeiaas.iaasagent.service;

import kubeiaas.common.constants.bean.HostConstants;
import kubeiaas.common.utils.ShellUtils;
import kubeiaas.iaasagent.config.HostConfig;
import kubeiaas.iaasagent.config.VncConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Slf4j
@Service
public class VncService {

    public void addVncToken(String uuid, String address) {
        log.info("addVncToken ---- start ---- uuid:" + uuid + " address:" + address);
        String cmd = "echo \'" + uuid + ": " + address + "\'>> " + VncConfig.TOKEN_FILE_PATH;
        String result = ShellUtils.getCmd(cmd);
        log.info("result: " + result);
        log.info("addVncToken ---- end ----");
    }

    public void deleteVncToken(String uuid) {
        log.info("deleteVncToken ---- start ---- uuid:" + uuid);
        String cmd = "sed -i \'/" + uuid + "/d\' " + VncConfig.TOKEN_FILE_PATH;
        String result = ShellUtils.getCmd(cmd);
        log.debug("deleteVncToken -- Command: " + cmd + " executed");
        log.debug("deleteVncToken -- Result: " + result);
        log.info("deleteVncToken ---- end ----");
    }

    @Resource
    private HostService hostService;

    public void checkVncStatus() {
        boolean res = hostService.checkEnvRes("novnc");
        if (res) {
            log.info("checkVncStatus -- vnc is on this host!");
            if (!hostService.hasHostRole(HostConstants.ROLE_VNC)) {
                // if not set role, then set
                hostService.setHostRole(HostConstants.ROLE_VNC);
            }
        } else {
            log.info("checkVncStatus -- vnc is not on this host.");
            if (hostService.hasHostRole(HostConstants.ROLE_VNC)) {
                // if already set role, then delete
                hostService.delHostRole(HostConstants.ROLE_VNC);
            }
        }
    }
}
