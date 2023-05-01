package kubeiaas.iaasagent.service;

import kubeiaas.common.bean.Vm;
import kubeiaas.common.utils.ShellUtils;
import kubeiaas.iaasagent.config.LibvirtConfig;
import kubeiaas.iaasagent.config.VncConfig;
import kubeiaas.iaasagent.dao.TableStorage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Slf4j
@Service
public class VncService {

    @Resource
    private TableStorage tableStorage;

    public void saveVncPort(String uuid) {
        // status
        Vm vm = tableStorage.vmQueryByUuid(uuid);
        // new vnc port
        String vncPort = ShellUtils.getCmd(LibvirtConfig.getVncPort + " " + uuid).replaceAll("\\r\\n|\\r|\\n|\\n\\r|:", "");
        vm.setVncPort(vncPort);
        // save
        tableStorage.vmSave(vm);
    }

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

    public void flushVncToken(String uuid, String address){
        log.info("flushVncToken ---- start ---- uuid:" + uuid + " address:" + address);
        deleteVncToken(uuid);
        addVncToken(uuid, address);
        log.debug("flushVncToken -- Vnc Token for " + uuid + " has been flushed.");
        log.info("flushVncToken ---- end ----");
    }
}
