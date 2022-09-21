package kubeiaas.iaascore.process;

import kubeiaas.common.bean.Host;
import kubeiaas.common.bean.Image;
import kubeiaas.common.bean.Vm;
import kubeiaas.common.constants.HostSelectStrategyConstants;
import kubeiaas.common.constants.bean.VmConstants;
import kubeiaas.common.enums.image.ImageStatusEnum;
import kubeiaas.common.enums.vm.VmStatusEnum;
import kubeiaas.common.utils.UuidUtils;
import kubeiaas.iaascore.config.AgentConfig;
import kubeiaas.iaascore.dao.TableStorage;
import kubeiaas.iaascore.exception.BaseException;
import kubeiaas.iaascore.scheduler.ResourceScheduler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.sql.Timestamp;

@Slf4j
@Service
public class VmProcess {

    @Resource
    private TableStorage tableStorage;

    @Resource
    private ResourceScheduler resourceScheduler;

    public Vm preCreate(
            String name,
            int cpus,
            int memory,
            String imageUuid,
            Integer diskSize,
            String description,
            String hostUUid)
    {
        log.info("createVm -- 1. pre create VM");
        Vm newVm = new Vm();
        String newVmUuid = UuidUtils.getRandomUuid();

        // 1.1. set basic
        newVm.setUuid(newVmUuid);
        newVm.setName(name);
        if (description != null && !description.isEmpty()){
            newVm.setDescription(description);
        } else {
            newVm.setDescription(VmConstants.DEFAULT_DESCRIPTION);
        }
        newVm.setPassword(VmConstants.DEFAULT_PASSWORD);

        // 1.2. set cpu, mem, diskSize
        // （暂时不考虑由 image 带来的限制，在 2.ResourceOperator 时判定）
        newVm.setCpus(cpus);
        newVm.setMemory(memory);
        if (diskSize != null && diskSize > 0) {
            newVm.setDiskSize(diskSize);
        } else {
            newVm.setDiskSize(VmConstants.DEFAULT_DISK_SIZE);
        }

        // 1.3. set status
        newVm.setStatus(VmStatusEnum.BUILDING);

        // 1.4. set related hostUuid & imageUuid
        // （暂时不考虑 image 的可用性，在 2.ResourceOperator 时考虑）
        // （暂时不考虑 host 的可用性，在 2.ResourceOperator 中考虑）
        newVm.setImageUuid(imageUuid);
        newVm.setHostUuid(hostUUid);

        // 1.5. set createTime
        newVm.setCreateTime(new Timestamp(System.currentTimeMillis()));

        // 1.6. save into DB
        newVm = tableStorage.vmSave(newVm);
        log.info("createVm -- 1. pre create success!");

        return newVm;
    }

    public Vm createResourceOperate(Vm newVm) throws BaseException {
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
}
