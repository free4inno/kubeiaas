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
        log.info(String.format("saveVncPort info -- uuid: %s", uuid));
        // status
        log.info("-> invoke DB -- vmQueryByUuid");
        Vm vm = tableStorage.vmQueryByUuid(uuid);
        log.info("<- invoke DB -- done");
        // new vnc port
        String vncPort = ShellUtils.getCmd(LibvirtConfig.getVncPort + " " + uuid).replaceAll("\\r\\n|\\r|\\n|\\n\\r|:", "");
        vm.setVncPort(vncPort);
        // save
        log.info("-> invoke DB -- vmSave");
        tableStorage.vmSave(vm);
        log.info("<- invoke DB -- done");
    }

    public void addVncToken(String uuid, String address) {
        log.info(String.format("addVncToken info -- uuid: %s, address: %s", uuid, address));
        String cmd = "echo \'" + uuid + ": " + address + "\'>> " + VncConfig.TOKEN_FILE_PATH;
        log.debug(String.format("addVncToken -- Command: %s executed", cmd));
        String result = ShellUtils.getCmd(cmd);
        log.debug(String.format("addVncToken -- result is: %s", result));
    }

    public void deleteVncToken(String uuid) {
        log.info(String.format("deleteVncToken info -- uuid: %s", uuid));
        String cmd = "sed -i \'/" + uuid + "/d\' " + VncConfig.TOKEN_FILE_PATH;
        log.debug(String.format("deleteVncToken -- Command: %s executed", cmd));
        String result = ShellUtils.getCmd(cmd);
        log.debug(String.format("deleteVncToken -- result is: %s", result));
    }

    public void flushVncToken(String uuid, String address){
        log.info(String.format("flushVncToken info -- uuid: %s, address: %s", uuid, address));
        deleteVncToken(uuid);
        addVncToken(uuid, address);
    }

    public Integer getVncPort(String uuid) {
        log.info(String.format("getVncPort info -- uuid: %s", uuid));
        String vncPort = ShellUtils.getCmd(LibvirtConfig.getVncPort + " " + uuid).replaceAll("\\r\\n|\\r|\\n|\\n\\r|:", "");
        return Integer.parseInt(vncPort);
    }
}
