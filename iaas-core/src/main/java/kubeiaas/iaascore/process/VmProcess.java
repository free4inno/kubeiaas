package kubeiaas.iaascore.process;

import kubeiaas.common.bean.*;
import kubeiaas.common.constants.bean.ImageConstants;
import kubeiaas.common.constants.bean.VmConstants;
import kubeiaas.common.enums.image.ImageOSTypeEnum;
import kubeiaas.common.enums.vm.VmBootEnum;
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
import java.util.*;

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
            log.error("ERROR: create vm instance failed!");
            throw new BaseException("ERROR: create vm instance failed!");
        }
        log.debug("createVm -- 5. VM create success!");
    }

    public boolean createVmByXmlOn(Host host, String vmUuid, String xmlDesc) {
        AgentConfig.setSelectedHost(vmUuid, host);
        boolean isSuccess = vmScheduler.createVmInstanceByXml(vmUuid, xmlDesc);
        AgentConfig.clearSelectedHost(vmUuid);
        return isSuccess;
    }

    public void setVmBootOrder(String vmUuid, VmBootEnum bootOrder) throws BaseException {
        String order;
        if (Objects.requireNonNull(bootOrder) == VmBootEnum.CDROM) {
            order = "cdrom";
        } else {
            order = "hd";
        }
        log.info(String.format("-> invoke vmScheduler -- setVmBootOrder, vmUuid: %s, order: %s", vmUuid, order));
        if (!vmScheduler.setVmBootOrder(vmUuid, order)) {
            log.error("ERROR: set vm boot order failed!");
            throw new BaseException("ERROR: set vm boot order failed!");
        }
        log.info("<- invoke vmScheduler -- done");
        AgentConfig.clearSelectedHost(vmUuid);
    }

    public Optional<VmBootEnum> getVmBootOrder(String vmUuid) throws BaseException {
        log.info(String.format("-> invoke vmScheduler -- getVmBootOrder, vmUuid: %s", vmUuid));
        String order = vmScheduler.getVmBootOrder(vmUuid);
        log.info("<- invoke vmScheduler -- done");
        VmBootEnum bootOrder = VmBootEnum.fromString(order);
        if (bootOrder == null) {
            log.error("ERROR: get vm boot order failed!");
            return Optional.empty();
        }
        return Optional.of(bootOrder);
    }

    /**
     * Delete VM
     */
    public void deleteVM(String vmUuid) throws BaseException {
        log.debug("deleteVm --  VM");
        //先停止，否则无法删除正在使用的磁盘文件
        if (!vmScheduler.deleteVmInstance(vmUuid)){
            log.error("ERROR: delete vm instance failed!");
            throw new BaseException("ERROR: delete vm instance failed!");
        }
        log.debug("deleteVm success!");
    }

    public void deleteVmOn(Host host, String vmUuid) throws BaseException {
        AgentConfig.setSelectedHost(vmUuid, host);
        deleteVM(vmUuid);
        AgentConfig.clearSelectedHost(vmUuid);
    }

    public void stopVM(String vmUuid) throws BaseException {
        setVmStatus(vmUuid, VmStatusEnum.STOPPING);
        if (!vmScheduler.stopVmInstance(vmUuid)){
            log.error("ERROR: stop vm instance failed!");
            throw new BaseException("ERROR: stop vm instance failed!");
        }
    }

    public void startVM(String vmUuid) throws BaseException {
        setVmStatus(vmUuid, VmStatusEnum.STARTING);
        if (!vmScheduler.startVmInstance(vmUuid)){
            log.error("ERROR: start vm instance failed!");
            throw new BaseException("ERROR: start vm instance failed!");
        }
    }

    public void rebootVM(String vmUuid) throws BaseException {
        //将虚拟机状态设置为rebooting
        setVmStatus(vmUuid, VmStatusEnum.REBOOTING);
        if (!vmScheduler.rebootVmInstance(vmUuid)){
            log.error("ERROR: reboot vm instance failed!");
            throw new BaseException("ERROR: reboot vm instance failed!");
        }
    }

    public void resumeVM(String vmUuid) throws BaseException {
        //将虚拟机状态设置为resuming
        setVmStatus(vmUuid, VmStatusEnum.RESUMING);
        if (!vmScheduler.resumeVmInstance(vmUuid)){
            log.error("ERROR: resume vm instance failed!");
            throw new BaseException("ERROR: resume vm instance failed!");
        }
    }

    public void suspendVM(String vmUuid) throws BaseException {
        //将虚拟机状态设置为suspending
        setVmStatus(vmUuid, VmStatusEnum.SUSPENDING);
        if (!vmScheduler.suspendVmInstance(vmUuid)){
            log.error("ERROR: suspend vm instance failed!");
            throw new BaseException("ERROR: suspend vm instance failed!");
        }
    }

    /**
     * Delete VM in dataBase
     */
    public void deleteVmInDataBase(String vmUuid){
        log.info(String.format("-> invoke vmScheduler -- deleteVmInDataBase, vmUuid: %s", vmUuid));
        vmScheduler.deleteVmInDataBase(vmUuid);
        log.info("<- invoke vmScheduler -- done");
        AgentConfig.clearSelectedHost(vmUuid);
    }

    /**
     * Save VM Statue in dataBase (ABANDON)
     */
    public void stopVmInDataBase(String vmUuid){
        Vm vm = tableStorage.vmQueryByUuid(vmUuid);
        vm.setStatus(VmStatusEnum.STOPPED);
        log.info(String.format("-> invoke DB -- vmSave, set status %s, uuid: %s", vm.getStatus(), vmUuid));
        tableStorage.vmSave(vm);
        log.info("<- invoke DB -- done");
        AgentConfig.clearSelectedHost(vmUuid);
    }

    /**
     * Modify VM
     */
    public void modifyVM(String vmUuid, Integer cpus, Integer memory) throws BaseException {
        // 1. find VM
        Vm vm = tableStorage.vmQueryByUuid(vmUuid);
        if (vm == null) {
            log.error("ERROR: vm is not found!");
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
        log.info(String.format("CPU or MEM update -- %s", cpuMemFlag));

        if (cpuMemFlag) {
            // 3. iaas-agent do
            log.info(String.format("-> invoke vmScheduler -- modifyVmInstance, cpus: %s, memory: %s", cpus, memory));
            if (!vmScheduler.modifyVmInstance(vmUuid, cpus, memory)) {
                log.error("ERROR: modify vm instance failed!");
                throw new BaseException("ERROR: modify vm instance failed!");
            }
            log.info("<- invoke vmScheduler -- done");
            // 4. update DB
            log.info(String.format("-> invoke DB -- vmUpdate, cpus: %s, memory: %s", cpus, memory));
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
        // 1. find VM
        Vm vm = tableStorage.vmQueryByUuid(vmUuid);
        if (vm == null) {
            log.error("ERROR: vm is not found!");
            throw new BaseException("ERROR: vm is not found!");
        }

        // 2. only STOPPED can be reduced
        if (vm.getStatus() != VmStatusEnum.STOPPED){
            log.error("ERROR: vm is still active");
            throw new BaseException("ERROR: vm is still active");
        }

        // 3. check CPU & MEM
        boolean cpuMemFlag = false;
        if (cpus != null && cpus != 0 && !cpus.equals(vm.getCpus())) {
            // 3.1. must be larger
            if (cpus >= vm.getCpus()) {
                log.error("ERROR: vm is not reducing");
                throw new BaseException("ERROR: vm is not reducing");
            }
            // 3.2. adjust
            vm.setCpus(cpus);
            cpuMemFlag = true;
        }
        if (memory != null && memory != 0 && !memory.equals(vm.getMemory())) {
            // 3.1. must be larger
            if (memory >= vm.getMemory()) {
                log.error("ERROR: vm is not reducing");
                throw new BaseException("ERROR: vm is not reducing");
            }
            // 3.2. adjust
            vm.setMemory(memory);
            cpuMemFlag = true;
        }
        log.info(String.format("CPU or MEM update -- %s", cpuMemFlag));

        if (cpuMemFlag) {
            // 4. iaas-agent do
            log.info(String.format("-> invoke vmScheduler -- modifyVmInstance, cpus: %s, memory: %s", cpus, memory));
            if (!vmScheduler.modifyVmInstance(vmUuid, cpus, memory)) {
                log.error("ERROR: reduce vm instance failed!");
                throw new BaseException("ERROR: reduce vm instance failed!");
            }
            log.info("<- invoke vmScheduler -- done");
            // 5. update DB
            log.info(String.format("-> invoke DB -- vmUpdate, cpus: %s, memory: %s", cpus, memory));
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
        Volume volume = tableStorage.systemVolumeQueryByUuid(vmUuid);
        if (volume == null){
            log.error("ERROR: volume is not found!");
            throw new BaseException("ERROR: volume is not found!");
        }

        // 2. get parentImage
        Image parentImage = tableStorage.imageQueryByUuid(volume.getImageUuid());
        if (parentImage == null){
            log.error("ERROR: image is not found!");
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
        // extraSize < 0 : 发布镜像大小小于源虚拟机系统盘大小，应该增加发布镜像大小
        int extraSize = vdSize - volume.getSize();
        if (extraSize < 0) {
            newImage.setVdSize(volume.getSize());
            extraSize = 0;
        } else {
            newImage.setVdSize(vdSize);
        }
        log.info(String.format("publishImage -- extraSize: %s", extraSize));

        // 4. get new imagePath
        String suffix = ImageUtils.getImageSuffix(newImage.getFormat());
        if (suffix == null) {
            log.error("ERROR: can't get imageSuffix!");
            throw new BaseException("ERROR: can't get imageSuffix!");
        }
        String imagePath = ImageConstants.IMAGE_PATH + newImage.getUuid() + suffix;

        // 5. copy volume file
        log.info("-> invoke vmScheduler -- volumePublishImage");
        vmScheduler.volumePublishImage(vmUuid, imagePath, volume.getProviderLocation(), extraSize);
        log.info("<- invoke vmScheduler -- done");

        // 6. build volume yaml
        if (!imageScheduler.imageCreateYaml(newImage)) {
            log.error("ERROR: build volume yaml failed!");
            throw new BaseException("ERROR: build volume yaml failed!");
        }

        // 7. clear select cache
        AgentConfig.clearSelectedHost(vmUuid);

    }

    public boolean liveMigrateVm(String vmUuid, Host sourceHost, Host targetHost) {
        log.info(String.format("-> invoke vmScheduler -- liveMigrateVm, vmUuid: %s, targetHostUuid: %s", vmUuid, targetHost.getUuid()));
        AgentConfig.setSelectedHost(vmUuid, sourceHost);
        boolean isSuccess = vmScheduler.liveMigrateVm(vmUuid, targetHost.getIp());
        log.info("<- invoke vmScheduler -- done");
        AgentConfig.clearSelectedHost(vmUuid);
        return isSuccess;
    }

    /**
     * Set VM Status in DB
     */
    public void setVmStatus(String vmUuid, VmStatusEnum status) {
        Vm vm = tableStorage.vmQueryByUuid(vmUuid);
        vm.setStatus(status);
        log.info(String.format("-> invoke DB -- vmSave, set status: %s, vmUuid: %s", status, vmUuid));
        tableStorage.vmSave(vm);
        log.info("<- invoke DB -- done");
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
            Optional<Image> imageOpt = Optional.ofNullable(imageMap.get(vm.getImageUuid()));
            Image image = imageOpt.orElse(new Image(vm.getImageUuid(), "NA", "NA"));
            vm.setImage(new Image(image.getUuid(), image.getName(), image.getOsType()));

            // set host
            Optional<Host> hostOpt = Optional.ofNullable(hostMap.get(vm.getHostUuid()));
            Host host = hostOpt.orElse(new Host("NA", "NA"));
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

    /**
     * 获取虚拟机的xml描述
     */
    public String getXmlDesc(Vm vm) throws BaseException {
        Host host = tableStorage.hostQueryByUuid(vm.getHostUuid());
        AgentConfig.setSelectedHost(vm.getUuid(), host);
        log.info("-> invoke vmScheduler -- getXmlDesc");
        String xmlDesc = vmScheduler.getXmlDesc(vm);
        log.info("<- invoke vmScheduler -- done");
        AgentConfig.clearSelectedHost(vm.getUuid());
        return xmlDesc;
    }

    /**
     * 获取状态
     */
    public VmStatusEnum getStatus(Vm vm) throws BaseException {
        Host host = tableStorage.hostQueryByUuid(vm.getHostUuid());
        AgentConfig.setSelectedHost(vm.getUuid(), host);
        log.info("-> invoke vmScheduler -- get real current status");
        VmStatusEnum vmStatusEnum = vmScheduler.getStatus(vm);
        log.info("<- invoke vmScheduler -- done");
        AgentConfig.clearSelectedHost(vm.getUuid());
        return vmStatusEnum;
    }

    /**
     * 修改密码
     */
    public void setPasswd(Vm vm, String user, String passwd) throws BaseException {
        Host host = tableStorage.hostQueryByUuid(vm.getHostUuid());
        AgentConfig.setSelectedHost(vm.getUuid(), host);
        if (!vmScheduler.setPasswd(vm, user, passwd)) {
            log.error("ERROR: failed to set passwd, please check Agent logs for exact reason.");
            throw new BaseException("err: failed to set passwd, please check Agent logs for exact reason.");
        }
        AgentConfig.clearSelectedHost(vm.getUuid());
    }
}
