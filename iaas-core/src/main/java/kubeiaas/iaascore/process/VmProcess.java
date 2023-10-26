package kubeiaas.iaascore.process;

import kubeiaas.common.bean.*;
import kubeiaas.common.constants.bean.ImageConstants;
import kubeiaas.common.constants.bean.VmConstants;
import kubeiaas.common.enums.vm.VmStatusEnum;
import kubeiaas.common.utils.UuidUtils;
import kubeiaas.iaascore.config.AgentConfig;
import kubeiaas.iaascore.dao.TableStorage;
import kubeiaas.iaascore.exception.BaseException;
import kubeiaas.iaascore.scheduler.ImageScheduler;
import kubeiaas.iaascore.scheduler.VmScheduler;
import kubeiaas.iaascore.utils.ImageUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Service
public class VmProcess {

    @Resource
    private TableStorage tableStorage;

    @Resource
    private VmScheduler vmScheduler;

    @Resource
    private ImageScheduler imageScheduler;


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
        log.debug("createVm -- 1. pre create VM");
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
        log.info("-> invoke DB -- vmSave");
        newVm = tableStorage.vmSave(newVm);
        log.info("<- invoke DB -- done");
        log.debug("createVm -- 1. pre create success!");

        return newVm;
    }


    /**
     * Create VM.
     * 5. VM Controller create
     */
    public void createVM(String newVmUuid) throws BaseException {
        log.debug("createVm -- 5. VM");
        if (!vmScheduler.createVmInstance(newVmUuid)) {
            log.error(String.format("ERROR: create vm instance failed!"));
            throw new BaseException("ERROR: create vm instance failed!");
        }
        log.debug("createVm -- 5. VM create success!");
    }


    /**
     * Delete VM
     */
    public void deleteVM(String vmUuid) throws BaseException {
        log.debug("deleteVm --  VM");
        //先停止，否则无法删除正在使用的磁盘文件
        if (!vmScheduler.deleteVmInstance(vmUuid)){
            log.error(String.format("ERROR: delete vm instance failed!"));
            throw new BaseException("ERROR: delete vm instance failed!");
        }
        log.debug("deleteVm success!");
    }

    public void stopVM(String vmUuid) throws BaseException {
        setVmStatus(vmUuid, VmStatusEnum.STOPPING);
        if (!vmScheduler.stopVmInstance(vmUuid)){
            log.error(String.format("ERROR: stop vm instance failed!"));
            throw new BaseException("ERROR: stop vm instance failed!");
        }
    }

    public void startVM(String vmUuid) throws BaseException {
        setVmStatus(vmUuid, VmStatusEnum.STARTING);
        if (!vmScheduler.startVmInstance(vmUuid)){
            log.error(String.format("ERROR: start vm instance failed!"));
            throw new BaseException("ERROR: start vm instance failed!");
        }
    }

    public void rebootVM(String vmUuid) throws BaseException {
        //将虚拟机状态设置为rebooting
        setVmStatus(vmUuid, VmStatusEnum.REBOOTING);
        if (!vmScheduler.rebootVmInstance(vmUuid)){
            log.error(String.format("ERROR: reboot vm instance failed!"));
            throw new BaseException("ERROR: reboot vm instance failed!");
        }
    }

    public void resumeVM(String vmUuid) throws BaseException {
        //将虚拟机状态设置为resuming
        setVmStatus(vmUuid, VmStatusEnum.RESUMING);
        if (!vmScheduler.resumeVmInstance(vmUuid)){
            log.error(String.format("ERROR: resume vm instance failed!"));
            throw new BaseException("ERROR: resume vm instance failed!");
        }
    }

    public void suspendVM(String vmUuid) throws BaseException {
        //将虚拟机状态设置为suspending
        setVmStatus(vmUuid, VmStatusEnum.SUSPENDING);
        if (!vmScheduler.suspendVmInstance(vmUuid)){
            log.error(String.format("ERROR: suspend vm instance failed!"));
            throw new BaseException("ERROR: suspend vm instance failed!");
        }
    }

    /**
     * Delete VM in dataBase
     */
    public void deleteVmInDataBase(String vmUuid){
        log.debug("deleteVm --  VM in dataBase");
        log.info("-> invoke vmScheduler -- deleteVmInDataBase");
        vmScheduler.deleteVmInDataBase(vmUuid);
        log.info("<- invoke vmScheduler -- done");
        AgentConfig.clearSelectedHost(vmUuid);
        log.debug("deleteVm in dataBase success!");
    }

    /**
     * Save VM Statue in dataBase (ABANDON)
     */
    public void stopVmInDataBase(String vmUuid){
        log.debug("stopVm --  VM in dataBase");
        log.info("-> invoke DB -- vmQueryByUuid");
        Vm vm = tableStorage.vmQueryByUuid(vmUuid);
        log.info("<- invoke DB -- done");
        vm.setStatus(VmStatusEnum.STOPPED);
        log.info("-> invoke DB -- vmSave");
        tableStorage.vmSave(vm);
        log.info("<- invoke DB -- done");
        AgentConfig.clearSelectedHost(vmUuid);
        log.debug("stopVm in dataBase success!");
    }

    /**
     * Modify VM
     */
    public void modifyVM(String vmUuid, Integer cpus, Integer memory) throws BaseException {
        log.debug("modifyVm --  VM");

        // 1. find VM
        log.info("-> invoke DB -- vmQueryByUuid");
        Vm vm = tableStorage.vmQueryByUuid(vmUuid);
        log.info("<- invoke DB -- done");
        if (vm == null) {
            log.error(String.format("ERROR: vm is not found!"));
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
        log.info(String.format("cpuMemFlag -- %s", cpuMemFlag));

        if (cpuMemFlag) {
            // 3. iaas-agent do
            if (!vmScheduler.modifyVmInstance(vmUuid, cpus, memory)) {
                log.error(String.format("ERROR: modify vm instance failed!"));
                throw new BaseException("ERROR: modify vm instance failed!");
            }
            // 4. update DB
            log.info("-> invoke DB -- vmUpdate");
            vm = tableStorage.vmUpdate(vm);
            log.info("<- invoke DB -- done");
        }

        // 5. clear select cache
        AgentConfig.clearSelectedHost(vmUuid);
    }

    /**
     * Modify  VM
     */
    public void reduceVM(String vmUuid, Integer cpus, Integer memory) throws BaseException {
        log.debug("reduce --  VM");

        // 1. find VM
        log.info("-> invoke DB -- vmQueryByUuid");
        Vm vm = tableStorage.vmQueryByUuid(vmUuid);
        log.info("<- invoke DB -- done");
        if (vm == null) {
            log.error(String.format("ERROR: vm is not found!"));
            throw new BaseException("ERROR: vm is not found!");
        }

        // 2. only STOPPED can be reduced
        if (vm.getStatus() != VmStatusEnum.STOPPED){
            log.error(String.format("ERROR: vm is still active"));
            throw new BaseException("ERROR: vm is still active");
        }

        // 3. check CPU & MEM
        boolean cpuMemFlag = false;
        if (cpus != null && cpus != 0 && !cpus.equals(vm.getCpus())) {
            // 3.1. must be larger
            if (cpus >= vm.getCpus()) {
                log.error(String.format("ERROR: vm is not reducing"));
                throw new BaseException("ERROR: vm is not reducing");
            }
            // 3.2. adjust
            vm.setCpus(cpus);
            cpuMemFlag = true;
        }
        if (memory != null && memory != 0 && !memory.equals(vm.getMemory())) {
            // 3.1. must be larger
            if (memory >= vm.getMemory()) {
                log.error(String.format("ERROR: vm is not reducing"));
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
                log.error(String.format("ERROR: reduce vm instance failed!"));
                throw new BaseException("ERROR: reduce vm instance failed!");
            }
            // 5. update DB
            log.info("-> invoke DB -- vmUpdate");
            vm = tableStorage.vmUpdate(vm);
            log.info("<- invoke DB -- done");
        }

        // 6. clear select cache
        AgentConfig.clearSelectedHost(vmUuid);
    }

    /**
     * Publish Volume to Image(Select host by vmUuid)
     */
    public void publishImage(String vmUuid, String name, String description, Integer vdSize) throws BaseException {

        // 1. find systemVolume
        log.info("-> invoke DB -- systemVolumeQueryByUuid");
        Volume volume = tableStorage.systemVolumeQueryByUuid(vmUuid);
        log.info("<- invoke DB -- done");
        if (volume == null){
            log.error(String.format("ERROR: volume is not found!"));
            throw new BaseException("ERROR: volume is not found!");
        }

        // 2. get parentImage
        log.info("-> invoke DB -- imageQueryByUuid");
        Image parentImage = tableStorage.imageQueryByUuid(volume.getImageUuid());
        log.info("<- invoke DB -- done");
        if (parentImage == null){
            log.error(String.format("ERROR: image is not found!"));
            throw new BaseException("ERROR: image is not found!");
        }

        // 3. create new image
        Image newImage = new Image();
        newImage.setUuid(UuidUtils.getRandomUuid());

        newImage.setName(name);
        newImage.setDescription(description);

        newImage.setFormat(parentImage.getFormat());
        newImage.setOsArch(parentImage.getOsArch());
        newImage.setOsType(parentImage.getOsType());
        newImage.setOsMode(parentImage.getOsMode());

        // *Calculate extraSize
        int extraSize = vdSize - parentImage.getVdSize();
        if (extraSize < 0) {
            newImage.setVdSize(parentImage.getVdSize());
            extraSize = 0;
        } else {
            newImage.setVdSize(vdSize);
        }
        log.info(String.format("publishImage -- extraSize: %s", extraSize));

        // 4. get new imagePath
        String suffix = ImageUtils.getImageSuffix(newImage.getFormat());
        if (suffix == null) {
            log.error(String.format("ERROR: can't get imageSuffix!"));
            throw new BaseException("ERROR: can't get imageSuffix!");
        }
        String imagePath = ImageConstants.IMAGE_PATH + newImage.getUuid() + suffix;

        // 5. copy volume file
        log.info("-> invoke vmScheduler -- volumePublishImage");
        vmScheduler.volumePublishImage(vmUuid, imagePath, volume.getProviderLocation(), extraSize);
        log.info("<- invoke vmScheduler -- done");

        // 6. build volume yaml
        if (!imageScheduler.imageCreateYaml(newImage)) {
            log.error(String.format("ERROR: build volume yaml failed!"));
            throw new BaseException("ERROR: build volume yaml failed!");
        }

        // 7. clear select cache
        AgentConfig.clearSelectedHost(vmUuid);

    }

    /**
     * Set VM Status in DB
     */
    public void setVmStatus(String VmUuid, VmStatusEnum status) {
        log.info("-> invoke DB -- vmQueryByUuid");
        Vm vm = tableStorage.vmQueryByUuid(VmUuid);
        log.info("<- invoke DB -- done");
        vm.setStatus(status);
        log.info("-> invoke DB -- vmSave");
        tableStorage.vmSave(vm);
        log.info("<- invoke DB -- done");
    }


    /**
     * Build VM list with full info
     * Used By QUERY_ALL / PAGE_QUERY_ALL / QUERY / PAGE_QUERY
     */
    public List<Vm> buildVmList(List<Vm> vmList) {
        // 1.1. 构造 imageMap，根据 uuid 索引
        log.info("-> invoke DB -- imageQueryAll");
        List<Image> imageList = tableStorage.imageQueryAll();
        log.info("<- invoke DB -- done");
        Map<String, Image> imageMap = new HashMap<>();
        for (Image image : imageList) {
            imageMap.put(image.getUuid(), image);
        }

        // 1.2. 构造 hostMap，根据 uuid 索引
        log.info("-> invoke DB -- hostQueryAll");
        List<Host> hostList = tableStorage.hostQueryAll();
        log.info("<- invoke DB -- done");
        Map<String, Host> hostMap = new HashMap<>();
        for (Host host : hostList) {
            hostMap.put(host.getUuid(), host);
        }

        // 3. 逐个处理 vm，填入 ips & image
        for (Vm vm : vmList) {
            log.info("-> invoke DB -- ipUsedQueryAllByInstanceUuid");
            List<IpUsed> ipUsedList = tableStorage.ipUsedQueryAllByInstanceUuid(vm.getUuid());
            log.info("<- invoke DB -- done");
            vm.setIps(ipUsedList);

            // set image
            // (use new Variable to avoid Pointer)
            Optional<Image> imageOpt = Optional.ofNullable(imageMap.get(vm.getImageUuid()));
            Image image = imageOpt.orElse(new Image(vm.getImageUuid(), "NA", "NA"));
            vm.setImage(new Image(image.getUuid(), image.getName(), image.getOsType()));

            // set host
            Optional<Host> hostOpt = Optional.ofNullable(hostMap.get(vm.getHostUuid()));
            Host host = hostOpt.orElse(new Host("NA", "NA"));
            vm.setHost(new Host(host.getName(), host.getIp()));

            // set volume
            log.info("-> invoke DB -- volumeQueryAllByInstanceUuid");
            List<Volume> volumeList = tableStorage.volumeQueryAllByInstanceUuid(vm.getUuid());
            log.info("<- invoke DB -- done");
            vm.setVolumes(volumeList);

            // remove useless/sensitive info
            vm.setPassword(null);
            vm.setVncPassword(null);
            vm.setVncPort(null);
        }
        return vmList;
    }

    /**
     * 获取状态
     */
    public VmStatusEnum getStatus(Vm vm) throws BaseException {
        log.info("-> invoke DB -- hostQueryByUuid");
        Host host = tableStorage.hostQueryByUuid(vm.getHostUuid());
        log.info("<- invoke DB -- done");
        AgentConfig.setSelectedHost(vm.getUuid(), host);
        log.info("-> invoke vmScheduler -- getStatus");
        VmStatusEnum vmStatusEnum = vmScheduler.getStatus(vm);
        log.info("<- invoke vmScheduler -- done");
        AgentConfig.clearSelectedHost(vm.getUuid());
        return vmStatusEnum;
    }

    /**
     * 修改密码
     */
    public void setPasswd(Vm vm, String user, String passwd) throws BaseException {
        log.info("-> invoke DB -- hostQueryByUuid");
        Host host = tableStorage.hostQueryByUuid(vm.getHostUuid());
        log.info("<- invoke DB -- done");
        AgentConfig.setSelectedHost(vm.getUuid(), host);
        if (!vmScheduler.setPasswd(vm, user, passwd)) {
            log.error(String.format("ERROR: failed to set passwd, please check Agent logs for exact reason."));
            throw new BaseException("err: failed to set passwd, please check Agent logs for exact reason.");
        }
        AgentConfig.clearSelectedHost(vm.getUuid());
    }
}
