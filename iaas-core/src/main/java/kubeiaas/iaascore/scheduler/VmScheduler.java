package kubeiaas.iaascore.scheduler;

import kubeiaas.common.bean.Vm;
import kubeiaas.common.bean.Volume;
import kubeiaas.common.constants.ResponseMsgConstants;
import kubeiaas.iaascore.config.AgentConfig;
import kubeiaas.iaascore.dao.TableStorage;
import kubeiaas.iaascore.dao.feign.VmController;
import kubeiaas.iaascore.process.MountProcess;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;

import javax.annotation.Resource;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

@Slf4j
@Configuration
public class VmScheduler {

    @Resource
    private TableStorage tableStorage;

    @Resource
    private VmController vmController;

    @Resource
    private MountProcess mountProcess;

    public boolean createVmInstance(String vmUuid) {
        List<Volume> volumeList = tableStorage.volumeQueryAllByInstanceUuid(vmUuid);
        Vm vm = tableStorage.vmQueryByUuid(vmUuid);

        // 1. 挂载 volume
        if (!mountProcess.attachVolumes(volumeList, vm)) {
             return false;
        }

        // 2. 调用 agent 执行 create
        vmController.createVmInstance(getSelectedUri(vmUuid), vmUuid);

        return true;
    }

    public boolean deleteVmInstance(String vmUuid){
        // 调用 agent 执行 delete
        if (vmController.deleteVmInstance(getSelectedUri(vmUuid), vmUuid).equals(ResponseMsgConstants.SUCCESS)) {
            return true;
        } else {
            return false;
        }
    }

    public void deleteVmInDataBase(String vmUuid){
        tableStorage.vmDeleteByUuid(vmUuid);
    }

    private URI getSelectedUri(String vmUuid) {
        try {
            return new URI(AgentConfig.getSelectedUri(vmUuid));
        } catch (URISyntaxException e) {
            e.printStackTrace();
            log.error("build URI failed!");
            return null;
        }
    }

}
