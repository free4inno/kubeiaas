package kubeiaas.iaasagent.service;

import kubeiaas.common.utils.ShellUtils;
import kubeiaas.iaasagent.config.VncConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

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
}
