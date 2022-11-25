package kubeiaas.iaascore.process;

import kubeiaas.common.bean.*;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
            String hostUUid) {
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

    public void stopVM(String vmUuid) throws BaseException {
        log.info("stopVm ==== start ====  vmUuid: " + vmUuid);
        setVmStatus(vmUuid, VmStatusEnum.STOPPING);
        if (!vmScheduler.stopVmInstance(vmUuid)){
            throw new BaseException("ERROR: stop vm instance failed!");
        }
        log.info("stopVm ==== end ==== ");
    }

    public void startVM(String vmUuid) throws BaseException {
        log.info("startVm ==== start ====  vmUuid: " + vmUuid);
        setVmStatus(vmUuid, VmStatusEnum.STARTING);
        if (!vmScheduler.startVmInstance(vmUuid)){
            throw new BaseException("ERROR: start vm instance failed!");
        }
        log.info("stopVm ==== end ==== ");
    }

    public void rebootVM(String vmUuid) throws BaseException {
        log.info("rebootVm ==== start ====  vmUuid: " + vmUuid);
        //将虚拟机状态设置为rebooting
        setVmStatus(vmUuid, VmStatusEnum.REBOOTING);
        if (!vmScheduler.rebootVmInstance(vmUuid)){
            throw new BaseException("ERROR: reboot vm instance failed!");
        }
        log.info("rebootVm ==== end ==== ");
    }

    public void resumeVM(String vmUuid) throws BaseException {
        log.info("resumeVM ==== start ====  vmUuid: " + vmUuid);
        //将虚拟机状态设置为resuming
        setVmStatus(vmUuid, VmStatusEnum.RESUMING);
        if (!vmScheduler.resumeVmInstance(vmUuid)){
            throw new BaseException("ERROR: resume vm instance failed!");
        }
        log.info("resumeVM ==== end ==== ");
    }

    public void suspendVM(String vmUuid) throws BaseException {
        log.info("suspendVM ==== start ====  vmUuid: " + vmUuid);
        //将虚拟机状态设置为suspending
        setVmStatus(vmUuid, VmStatusEnum.SUSPENDING);
        if (!vmScheduler.suspendVmInstance(vmUuid)){
            throw new BaseException("ERROR: suspend vm instance failed!");
        }
        log.info("suspendVM ==== end ==== ");
    }

    /**
     * Delete VM in dataBase
     */
    public void deleteVmInDataBase(String vmUuid){
        log.info("deleteVm --  VM in dataBase");
        vmScheduler.deleteVmInDataBase(vmUuid);
        AgentConfig.clearSelectedHost(vmUuid);
        log.info("deleteVm in dataBase success!");
    }

    /**
     * Save VM Statue in dataBase (ABANDON)
     */
    public void stopVmInDataBase(String vmUuid){
        log.info("stopVm --  VM in dataBase");
        Vm vm = tableStorage.vmQueryByUuid(vmUuid);
        vm.setStatus(VmStatusEnum.STOPPED);
        tableStorage.vmSave(vm);
        AgentConfig.clearSelectedHost(vmUuid);
        log.info("stopVm in dataBase success!");
    }

    /**
     * Modify VM
     */
    public void modifyVM(String vmUuid, Integer cpus, Integer memory) throws BaseException {
        log.info("modifyVm --  VM");

        // 1. find VM
        Vm vm = tableStorage.vmQueryByUuid(vmUuid);
        if (vm == null) {
            throw new BaseException("ERROR: vm is not found!");
        }

        // 2. check CPU & MEM
        boolean cpuMemFlag = false;
        if (cpus != null && cpus != 0 && !cpus.equals(vm.getCpus())) {
            vm.setCpus(cpus);
            cpuMemFlag = true;
        }
        if (memory != null && memory != 0 && !memory.equals(vm.getMemory())) {
            vm.setMemory(memory);
            cpuMemFlag = true;
        }
        log.info("cpuMemFlag --" + cpuMemFlag);

        if (cpuMemFlag) {
            // 3. iaas-agent do
            if (!vmScheduler.modifyVmInstance(vmUuid, cpus, memory)) {
                throw new BaseException("ERROR: modify vm instance failed!");
            }
            // 4. update DB
            vm = tableStorage.vmUpdate(vm);
        }

        // 5. clear select cache
        AgentConfig.clearSelectedHost(vmUuid);
    }

    /**
     * Modify  VM
     */
    public void reduceVM(String vmUuid, Integer cpus, Integer memory) throws BaseException {
        log.info("reduce --  VM");

        // 1. find VM
        Vm vm = tableStorage.vmQueryByUuid(vmUuid);
        if (vm == null) {
            throw new BaseException("ERROR: vm is not found!");
        }

        // 2. only STOPPED can be reduced
        if (vm.getStatus() != VmStatusEnum.STOPPED){
            throw new BaseException("ERROR: vm is still active");
        }

        // 3. check CPU & MEM
        boolean cpuMemFlag = false;
        if (cpus != null && cpus != 0 && !cpus.equals(vm.getCpus())) {
            // 3.1. must be larger
            if (cpus >= vm.getCpus()) {
                throw new BaseException("ERROR: vm is not reducing");
            }
            // 3.2. adjust
            vm.setCpus(cpus);
            cpuMemFlag = true;
        }
        if (memory != null && memory != 0 && !memory.equals(vm.getMemory())) {
            // 3.1. must be larger
            if (memory >= vm.getMemory()) {
                throw new BaseException("ERROR: vm is not reducing");
            }
            // 3.2. adjust
            vm.setMemory(memory);
            cpuMemFlag = true;
        }
        log.info("cpuMemFlag --" + cpuMemFlag);

        if (cpuMemFlag) {
            // 4. iaas-agent do
            if (!vmScheduler.modifyVmInstance(vmUuid, cpus, memory)) {
                throw new BaseException("ERROR: reduce vm instance failed!");
            }
            // 5. update DB
            vm = tableStorage.vmUpdate(vm);
        }

        // 6. clear select cache
        AgentConfig.clearSelectedHost(vmUuid);
    }

    /**
     * Set VM Status in DB
     */
    public void setVmStatus(String VmUuid, VmStatusEnum status) {
        Vm vm = tableStorage.vmQueryByUuid(VmUuid);
        vm.setStatus(status);
        tableStorage.vmSave(vm);
    }


    /**
     * Build VM list with full info
     * Used By QUERY_ALL / PAGE_QUERY_ALL / QUERY / PAGE_QUERY
     */
    public List<Vm> buildVmList(List<Vm> vmList) {
        // 1.1. 构造 imageMap，根据 uuid 索引
        List<Image> imageList = tableStorage.imageQueryAll();
        Map<String, Image> imageMap = new HashMap<>();
        for (Image image : imageList) {
            imageMap.put(image.getUuid(), image);
        }

        // 1.2. 构造 hostMap，根据 uuid 索引
        List<Host> hostList = tableStorage.hostQueryAll();
        Map<String, Host> hostMap = new HashMap<>();
        for (Host host : hostList) {
            hostMap.put(host.getUuid(), host);
        }

        // 3. 逐个处理 vm，填入 ips & image
        for (Vm vm : vmList) {
            List<IpUsed> ipUsedList = tableStorage.ipUsedQueryAllByInstanceUuid(vm.getUuid());
            vm.setIps(ipUsedList);

            // set image
            // (use new Variable to avoid Pointer)
            Image image = imageMap.get(vm.getImageUuid());
            vm.setImage(new Image(image.getUuid(), image.getName(), image.getOsType()));

            // set host
            Host host = hostMap.get(vm.getHostUuid());
            vm.setHost(new Host(host.getName(), host.getIp()));

            // set volume
            List<Volume> volumeList = tableStorage.volumeQueryAllByInstanceUuid(vm.getUuid());
            vm.setVolumes(volumeList);

            // remove useless/sensitive info
            vm.setPassword(null);
            vm.setVncPassword(null);
            vm.setVncPort(null);
        }
        return vmList;
    }
}
