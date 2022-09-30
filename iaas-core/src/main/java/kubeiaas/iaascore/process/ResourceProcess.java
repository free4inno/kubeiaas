package kubeiaas.iaascore.process;

import kubeiaas.common.bean.Host;
import kubeiaas.common.bean.Image;
import kubeiaas.common.bean.Vm;
import kubeiaas.common.constants.HostSelectStrategyConstants;
import kubeiaas.common.enums.image.ImageStatusEnum;
import kubeiaas.iaascore.config.AgentConfig;
import kubeiaas.iaascore.dao.TableStorage;
import kubeiaas.iaascore.exception.BaseException;
import kubeiaas.iaascore.scheduler.ResourceScheduler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Slf4j
@Service
public class ResourceProcess {

    @Resource
    private TableStorage tableStorage;

    @Resource
    private ResourceScheduler resourceScheduler;


    /**
     * Create VM.
     * 2. Resource Operator
     */
    public Vm createVmOperate(Vm newVm) throws BaseException {
        log.info("createVm -- 2. Resource Operator");

        // 2.1. check image
        // available
        Image image = tableStorage.imageQueryByUuid(newVm.getImageUuid());
        if (image == null || image.getStatus() != ImageStatusEnum.AVAILABLE) {
            throw new BaseException("ERROR: image not available!");
        }
        // mem
        if (newVm.getMemory() < image.getMinMem()) {
            throw new BaseException("ERROR: memory too low for image!");
        }
        // size
        int imageMinDisk = image.getMinDisk();
        int newVmDiskSize = newVm.getDiskSize();
        if (newVmDiskSize < imageMinDisk) {
            // 强制修改 diskSize，不终止
            newVm.setDiskSize(imageMinDisk);
        }

        // 2.2. select host
        Host selectedHost;
        String hostUUid = newVm.getHostUuid();

        if (hostUUid != null && !hostUUid.isEmpty()) {
            // 指定 host：检查可用性
            selectedHost = resourceScheduler.vmSelectHostByAppoint(newVm.getUuid(), hostUUid);
        } else {
            // 未指定 host：主动选择
            selectedHost = resourceScheduler.vmSelectHostByOperator(newVm.getUuid(), HostSelectStrategyConstants.ROUND_ROBIN);
        }
        if (selectedHost == null) {
            throw new BaseException("ERROR: no available host.");
        }
        log.info("selected host: " + selectedHost.getName());
        // set scheduler of iaas-agent
        AgentConfig.setSelectedHost(newVm.getUuid(), selectedHost);
        // save into DB
        newVm.setHostUuid(selectedHost.getUuid());
        tableStorage.vmSave(newVm);

        log.info("createVm -- 2. Resource Operator success!");
        return newVm;
    }

    /**
    * SelectHost by VmUuid
     */
    public void selectHostByVmUuid(String VmUuid) throws BaseException {

        //get Vm By VmUuid
        Vm vm = tableStorage.vmQueryByUuid(VmUuid);

        //SelectHost
        Host selectedHost;
        String hostUUid = vm.getHostUuid();
        if (hostUUid != null && !hostUUid.isEmpty()) {
            // 获取Vm所在的Host
            selectedHost = resourceScheduler.vmSelectHostByAppoint(vm.getUuid(), hostUUid);
        } else {
            // 若不存在，抛出服务器异常错误
            throw new BaseException("Error: HostUuid error!");
        }
        if (selectedHost == null) {
            throw new BaseException("ERROR: no available host.");
        }

        log.info("selected host: " + selectedHost.getName());
        AgentConfig.setSelectedHost(vm.getUuid(), selectedHost);

    }

}
