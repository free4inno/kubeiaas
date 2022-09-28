package kubeiaas.iaascore.process;

import kubeiaas.common.bean.Vm;
import kubeiaas.common.constants.bean.VmConstants;
import kubeiaas.common.enums.vm.VmStatusEnum;
import kubeiaas.common.utils.UuidUtils;
import kubeiaas.iaascore.config.AgentConfig;
import kubeiaas.iaascore.dao.TableStorage;
import kubeiaas.iaascore.exception.BaseException;
import kubeiaas.iaascore.scheduler.VmScheduler;
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
    private VmScheduler vmScheduler;


    /**
     * Create VM.
     * 1. pre create VM
     */
    public Vm preCreateVm(
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


    /**
     * Create VM.
     * 5. VM Controller create
     */
    public void createVM(String newVmUuid) throws BaseException {
        log.info("createVm -- 5. VM");
        if (!vmScheduler.createVmInstance(newVmUuid)) {
            throw new BaseException("ERROR: create vm instance failed!");
        }
        log.info("createVm -- 5. VM create success!");
    }

    public Vm queryVMByUuid(String vmUuid){
        Vm vm = tableStorage.vmQueryByUuid(vmUuid);
        return vm;
    }

    /**
     * Delete VM
     */
    public void deleteVM(String vmUuid) throws BaseException {
        log.info("deleteVm --  VM");
        //先停止，否则无法删除正在使用的磁盘文件
        if (!vmScheduler.deleteVmInstance(vmUuid)){
            throw new BaseException("ERROR: delete vm instance failed!");
        }
        log.info("deleteVm success!");
    }

    /**
     * Delete VM in dataBase
     */
    public void deleteVMInDataBase(String vmUuid){
        log.info("deleteVm --  VM in dataBase");
        vmScheduler.deleteVmInDataBase(vmUuid);
        AgentConfig.clearSelectedHost(vmUuid);
        log.info("deleteVm in dataBase success!");
    }
}
