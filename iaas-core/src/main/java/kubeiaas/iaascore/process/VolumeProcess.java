package kubeiaas.iaascore.process;

import kubeiaas.common.bean.Vm;
import kubeiaas.common.bean.Volume;
import kubeiaas.common.constants.bean.VolumeConstants;
import kubeiaas.common.enums.vm.VmStatusEnum;
import kubeiaas.common.enums.volume.VolumeFormatEnum;
import kubeiaas.common.enums.volume.VolumeStatusEnum;
import kubeiaas.common.enums.volume.VolumeUsageEnum;
import kubeiaas.common.utils.PathUtils;
import kubeiaas.common.utils.UuidUtils;
import kubeiaas.iaascore.config.AgentConfig;
import kubeiaas.iaascore.dao.TableStorage;
import kubeiaas.iaascore.exception.BaseException;
import kubeiaas.iaascore.exception.VmException;
import kubeiaas.iaascore.exception.VolumeException;
import kubeiaas.iaascore.scheduler.VolumeScheduler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.sql.Timestamp;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class VolumeProcess {

    @Resource
    private TableStorage tableStorage;

    @Resource
    private VolumeScheduler volumeScheduler;

    @Resource
    private VmProcess vmProcess;

    @Resource VncProcess vncProcess;

    @Resource
    ResourceProcess resourceProcess;


    /**
     * Create VM.
     * 2. Create Sys Volume
     */
    public void createVmVolume(Vm newVm) throws VmException {
        log.debug("createVm -- 4. Volume");
        log.info("-> invoke volumeScheduler -- createSystemVolume");
        String volumeUuid = volumeScheduler.createSystemVolume(newVm.getUuid());
        log.info("<- invoke volumeScheduler -- done");
        if (volumeUuid.isEmpty()) {
            log.error("ERROR: create system volume failed! (pre error)");
            throw new VmException(newVm,"ERROR: create system volume failed! (pre error)");
        }
        String newVmUuid = newVm.getUuid();
        // Attention: copying image is long-time operation, so start a new Thread to handle this.
        new Thread(() -> {
            Volume volume = tableStorage.volumeQueryByUuid(volumeUuid);

            int waitLoop = VolumeConstants.CREATING_WAIT_LOOP;
            int backOffTime = VolumeConstants.CREATING_WAIT_TIME;
            try {
                TimeUnit.SECONDS.sleep(backOffTime);
                // when copy is done, volume status will change in database, so query volume status at regular time.
                while (!volume.getStatus().equals(VolumeStatusEnum.AVAILABLE) &&
                        !volume.getStatus().equals(VolumeStatusEnum.ERROR_PREPARE) &&
                        waitLoop > 0) {
                    waitLoop--;
                    backOffTime += VolumeConstants.CREATING_WAIT_TIME;
                    log.info(String.format("... volume not ready, loop back off ({%s} seconds)", backOffTime));
                    TimeUnit.SECONDS.sleep(backOffTime);
                    volume = tableStorage.volumeQueryByUuid(volumeUuid);
                }
                if (waitLoop == 0 || volume.getStatus().equals(VolumeStatusEnum.ERROR_PREPARE)) {
                    // timeout || volume controller set ERROR to DB
                    log.error("ERROR: create system volume failed! (time out)");
                    newVm.setStatus(VmStatusEnum.ERROR);
                    log.info(String.format("-> invoke DB -- vmSave, uuid: %s, set status: %s", newVm.getUuid(), newVm.getStatus()));
                    tableStorage.vmSave(newVm);
                    log.info("<- invoke DB -- done");
                    return;
                }
            } catch (InterruptedException e) {
                log.error("ERROR: create system volume failed! (loop error). ", e);

                newVm.setStatus(VmStatusEnum.ERROR);
                log.info(String.format("-> invoke DB -- vmSave, uuid: %s, set status: %s", newVm.getUuid(), newVm.getStatus()));
                tableStorage.vmSave(newVm);
                log.info("<- invoke DB -- done");
                return;
            }
            log.debug("createVm -- 4. volume create success!");

            // Step 5. Create VM (Generate XML for libvirt & Attach volume)
            try {
                vmProcess.createVM(newVmUuid);
            } catch (BaseException e) {
                newVm.setStatus(VmStatusEnum.ERROR);
                log.error(String.format("ERROR: failed to create VM -- vmUuid: %s, msg: %s", newVmUuid, e.getMsg()));
                log.info("-> invoke DB -- vmSave");
                tableStorage.vmSave(newVm);
                log.info("<- invoke DB -- done");
                return;
            }
            AgentConfig.clearSelectedHost(newVmUuid);

            //Step 6 . Configuring VNC Service.
            vncProcess.addVncToken(newVm);

        }).start();
        log.info("createVm -- newThread begin wait for volume creating...");
    }

    public void createDataVolume(Volume newVolume) throws VolumeException {
        String volumeUuid = newVolume.getUuid();
        if (volumeUuid.isEmpty()) {
            log.error("ERROR: create Data volume failed! error in preCreate");
            throw new VolumeException(newVolume, "ERROR: create Data volume failed! error in preCreate");
        }
        int volumeSize = newVolume.getSize();
        log.info(String.format("-> invoke iaas-agent -- createDataVolume, volumeUuid: %s", volumeUuid));
        if (!volumeScheduler.createDataVolume(newVolume.getProviderLocation(), volumeUuid, volumeSize)){
            log.error(String.format("ERROR: create Data volume failed! error in agent -- volumeUuid: %s", volumeUuid));
            throw new VolumeException(newVolume, "ERROR: create Data volume failed! error in agent");
        }
        log.debug("<- invoke iaas-agent -- done");
        AgentConfig.clearSelectedHost(volumeUuid);
    }

    public void resizeSystemVolume(Volume volume, int diskSize) throws VolumeException {
        if (!volumeScheduler.resizeSystemVolume(volume.getProviderLocation(), volume.getUuid(), diskSize)) {
            log.error(String.format("ERROR: resize system volume: %s failed!", volume.getUuid()));
            throw new VolumeException(volume, "ERROR: resize system volume:" + volume.getUuid() + " failed!");
        }
        AgentConfig.clearSelectedHost(volume.getUuid());
    }

    public void resizeDataVolume(Volume volume, int diskSize) throws VolumeException {
        if (!volumeScheduler.resizeDataVolume(volume.getProviderLocation(), volume.getUuid(), diskSize)) {
            log.error(String.format("ERROR: resize data volume: %s failed!", volume.getUuid()));
            throw new VolumeException(volume, "ERROR: resize data volume:" + volume.getUuid() + " failed!");
        }
        AgentConfig.clearSelectedHost(volume.getUuid());
    }

    public void deleteSystemVolume(String vmUuid) throws BaseException {
        List<Volume> volumes = tableStorage.volumeQueryAllByInstanceUuid(vmUuid);
        for (Volume volume : volumes) {
            // Set host to execute delete operation
            resourceProcess.selectHostByVolume(volume);
            VolumeUsageEnum usage = volume.getUsageType();
            switch (usage) {
                case SYSTEM:
                    if (!volumeScheduler.deleteSystemVolume(vmUuid, volume.getUuid(), volume.getProviderLocation())){
                        log.error(String.format("ERROR: delete sys volume: %s failed!", volume.getUuid()));
                        throw new BaseException("ERROR: delete sys volume:" + volume.getUuid() + " failed!");
                    }
                    break;
                case ISO:
                    if (!volumeScheduler.deleteIsoVolume(vmUuid, volume.getUuid())){
                        log.error(String.format("ERROR: delete iso volume: %s failed!", volume.getUuid()));
                        throw new BaseException("ERROR: delete iso volume:" + volume.getUuid() + " failed!");
                    }
                    break;
                case DATA:
                    if (!volumeScheduler.deleteDataVolume(volume.getUuid(), volume.getProviderLocation())) {
                        log.error(String.format("ERROR: delete data volume: %s failed!", volume.getUuid()));
                        throw new BaseException("ERROR: delete data volume:" + volume.getUuid() + " failed!");
                    }
                    break;
                default:
                    log.warn("WARN: unknown volume type");
                    break;
            }
            // clear selected host
            AgentConfig.clearSelectedHost(volume.getUuid());
        }
    }

    public void deleteDataVolume(String volumeUuid) throws BaseException {
        Volume volume = tableStorage.volumeQueryByUuid(volumeUuid);
        if (!volumeScheduler.deleteDataVolume(volumeUuid, volume.getProviderLocation())) {
            log.error(String.format("ERROR: delete data volume: %s failed!", volume.getUuid()));
            throw new BaseException("ERROR: delete data volume:" + volume.getUuid() + " failed!");
        }
        AgentConfig.clearSelectedHost(volumeUuid);
    }

    public void attachDataVolume(String vmUuid, String volumeUuid) throws BaseException {
        if (!volumeScheduler.attachDataVolume(vmUuid, volumeUuid)) {
            log.error(String.format("ERROR: attach data volume: %s failed!", volumeUuid));
            throw new BaseException("ERROR: attach data volume:" + volumeUuid + " failed!");
        }
        AgentConfig.clearSelectedHost(volumeUuid);
    }

    public void detachDataVolume(String vmUuid, String volumeUuid) throws BaseException {
        if (!volumeScheduler.detachDataVolume(vmUuid, volumeUuid)) {
            log.error(String.format("ERROR: detach data volume: %s failed!", volumeUuid));
            throw new BaseException("ERROR: detach data volume:" + volumeUuid + " failed!");
        }
        Volume volume = tableStorage.volumeQueryByUuid(volumeUuid);
        volume.setInstanceUuid("");
        volume.setMountPoint("");
        volume.setBus("");
        log.info("-> invoke DB -- volumeSave");
        tableStorage.volumeSave(volume);
        log.info("<- invoke DB -- done");
        AgentConfig.clearSelectedHost(volumeUuid);
    }

    /**
     * preCreate Volume.
     */
    public Volume preCreateVolume(
            String name,
            String description,
            String hostUUid,
            Integer diskSize){
        String volumeUuid = UuidUtils.getRandomUuid();
        Volume newVolume = new Volume();
        newVolume.setUuid(volumeUuid);
        log.info(String.format("uuid allocated -- volumeUuid: %s", volumeUuid));

        newVolume.setCreateTime(new Timestamp(System.currentTimeMillis()));
        if (name == null || name.isEmpty()) {
            newVolume.setName(volumeUuid);
        } else {
            newVolume.setName(name);
        }
        newVolume.setDescription(description);
        newVolume.setHostUuid(hostUUid);
        newVolume.setSize(diskSize);
        newVolume.setProviderLocation(PathUtils.genDataVolumeDirectoryByUuid(volumeUuid, null));
        newVolume.setUsageType(VolumeUsageEnum.DATA);
        newVolume.setFormatType(VolumeFormatEnum.QCOW2);
        newVolume.setStatus(VolumeStatusEnum.CREATING);
        log.info(String.format("volume set status -- status: %s", volumeUuid));

        // save into DB
        log.info(String.format("-> invoke DB -- volume create, volumeUuid: %s", volumeUuid));
        newVolume = tableStorage.volumeSave(newVolume);
        log.info("<- invoke DB -- done");

        return newVolume;
    }

    /**
     * build Volume List with full info (append attach Vm info)
     */
    public List<Volume> buildVolumeList(List<Volume> volumeList) {
        for (Volume volume : volumeList) {
            if (volume.getStatus().equals(VolumeStatusEnum.ATTACHED)) {
                Vm vm = tableStorage.vmQueryByUuid(volume.getInstanceUuid());
                volume.setInstanceVm(vm);
            }
        }
        return volumeList;
    }

}
