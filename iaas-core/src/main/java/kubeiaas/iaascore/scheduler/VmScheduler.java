package kubeiaas.iaascore.scheduler;

import kubeiaas.common.bean.Vm;
import kubeiaas.common.bean.Volume;
import kubeiaas.iaascore.dao.TableStorage;
import kubeiaas.iaascore.dao.feign.VmController;
import kubeiaas.iaascore.process.MountProcess;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;

import javax.annotation.Resource;
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

        if (!mountProcess.attachVolumes(volumeList, vm)) {
             return false;
        }
        vmController.createVmInstance(vmUuid);
        return true;
    }

}
