package kubeiaas.iaascore.process;

import kubeiaas.common.bean.Vm;
import kubeiaas.common.bean.Volume;
import kubeiaas.common.constants.bean.VolumeConstants;
import kubeiaas.common.enums.vm.VmStatusEnum;
import kubeiaas.common.enums.volume.VolumeFormatEnum;
import kubeiaas.common.enums.volume.VolumeStatusEnum;
import kubeiaas.common.enums.volume.VolumeUsageEnum;
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


    /**
     * Create VM.
     * 2. Create Sys Volume
     */
    public void createVmVolume(Vm newVm) throws VmException {
        log.info("createVm -- 4. Volume");

        String volumeUuid = volumeScheduler.createSystemVolume(newVm.getUuid());
        if (volumeUuid.isEmpty()) {
            throw new VmException(newVm,"ERROR: create system volume failed! (pre error)");
        }
        String newVmUuid = newVm.getUuid();
        // Attention: copying image is long-time operation, so start a new Thread to handle this.
        new Thread(() -> {
            Volume volume = tableStorage.volumeQueryByUuid(volumeUuid);

            int waitLoop = VolumeConstants.CREATING_WAIT_LOOP;
            try {   // when copy is done, volume status will change in database, so query volume status at regular time.
                while (!volume.getStatus().equals(VolumeStatusEnum.AVAILABLE) &&
                        !volume.getStatus().equals(VolumeStatusEnum.ERROR_PREPARE) &&
                        waitLoop > 0) {
                    waitLoop--;
                    TimeUnit.SECONDS.sleep(VolumeConstants.CREATING_WAIT_TIME);
                    volume = tableStorage.volumeQueryByUuid(volumeUuid);
                }
                if (waitLoop == 0 || volume.getStatus().equals(VolumeStatusEnum.ERROR_PREPARE)) {
                    // timeout || volume controller set ERROR to DB
                    log.error("ERROR: create system volume failed! (time out)");
                    newVm.setStatus(VmStatusEnum.ERROR);
                    tableStorage.vmSave(newVm);
                    return;
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
                newVm.setStatus(VmStatusEnum.ERROR);
                tableStorage.vmSave(newVm);
                log.error("ERROR: create system volume failed! (loop error)");
                return;
            }
            log.info("createVm -- 4. volume create success!");

            // Step 5. Create VM (Generate XML for libvirt & Attach volume)
            try {
                vmProcess.createVM(newVmUuid);
            } catch (BaseException e) {
                newVm.setStatus(VmStatusEnum.ERROR);
                tableStorage.vmSave(newVm);
                log.error(e.getMsg());
                return;
            }
            AgentConfig.clearSelectedHost(newVmUuid);

            //Step 6 . Configuring VNC Service.
            vncProcess.addVncToken(newVm);

        }).start();
        log.info("createVm -- newThread begin wait for volume creating...");
    }

    public void createDataVolume(Volume newVolume) throws VolumeException {
        log.info("createVm -- Volume");

        String volumeUuid = newVolume.getUuid();
        if (volumeUuid.isEmpty()) {
            throw new VolumeException(newVolume,"ERROR: create Data volume failed! (pre error)");
        }
        int diskSize = newVolume.getSize();
        if (!volumeScheduler.createDataVolume(newVolume.getProviderLocation(), volumeUuid, diskSize)){
            throw new VolumeException(newVolume, "ERROR create Data volume:"+newVolume.getUuid()+" failed!");
        }
        AgentConfig.clearVolumeSelectedHost(volumeUuid);
    }

    public void deleteSystemVolume(String vmUuid) throws BaseException {
        log.info("deleteVolume ==== start ==== vmUuid: " + vmUuid);
        List<Volume> volumes = tableStorage.volumeQueryAllByInstanceUuid(vmUuid);
        for (Volume volume : volumes) {
            VolumeUsageEnum usage = volume.getUsageType();
            switch (usage) {
                case SYSTEM:
                    if (!volumeScheduler.deleteSystemVolume(vmUuid, volume.getUuid(), volume.getProviderLocation())){
                        throw new BaseException("ERROR: delete sys volume:" + volume.getUuid() + " failed!");
                    }
                    break;
                case ISO:
                    if (!volumeScheduler.deleteIsoVolume(vmUuid, volume.getUuid())){
                        throw new BaseException("ERROR: delete iso volume:" + volume.getUuid() + " failed!");
                    }
                    break;
                case DATA:
                    // todo: data volume delete
                    break;
                default:
                    log.warn("unknown volume type");
                    break;
            }
        }
        log.info("deleteVolume ==== end ==== vmUuid:" + vmUuid);
    }

    public void deleteDataVolume(String volumeUuid) throws BaseException {
        log.info("deleteDataVolume ==== start ====  volumeUuid: " + volumeUuid);
        Volume volume = tableStorage.volumeQueryByUuid(volumeUuid);
        if (!volumeScheduler.deleteDataVolume(volumeUuid,volume.getProviderLocation())){
            throw new BaseException("ERROR: delete data volume:"+volume.getUuid()+" failed!");
        }
        log.info("deleteDataVolume ==== end ==== volumeUuid:" + volumeUuid);
        AgentConfig.clearVolumeSelectedHost(volumeUuid);
    }

    public void attachDataVolume(String vmUuid, String volumeUuid) throws BaseException {
        log.info("attachDataVolume ==== start ====  volumeUuid: " + volumeUuid+"vmUuid:"+vmUuid);
        if (!volumeScheduler.attachDataVolume(vmUuid, volumeUuid)){
            throw new BaseException("ERROR: attach data volume:"+volumeUuid+" failed!");
        }
        log.info("attachDataVolume ==== end ==== volumeUuid:" + volumeUuid);
        AgentConfig.clearVolumeSelectedHost(volumeUuid);
    }

    public void detachDataVolume(String vmUuid, String volumeUuid) throws BaseException {
        log.info("detachDataVolume ==== start ====  volumeUuid: " + volumeUuid+"vmUuid:"+vmUuid);
        if (!volumeScheduler.detachDataVolume(vmUuid, volumeUuid)){
            throw new BaseException("ERROR: detach data volume:"+volumeUuid+" failed!");
        }
        Volume volume = tableStorage.volumeQueryByUuid(volumeUuid);
        volume.setInstanceUuid("");
        volume.setMountPoint("");
        volume.setBus("");
        tableStorage.volumeSave(volume);
        log.info("detachDataVolume ==== end ==== volumeUuid:" + volumeUuid);
        AgentConfig.clearVolumeSelectedHost(volumeUuid);
    }

    /**
     * preCreate Volume.
     */
    public Volume preCreateVolume(
            String name,
            String description,
            String hostUUid,
            Integer diskSize
    ){
        String volumeUuid = UuidUtils.getRandomUuid();
        Volume newVolume = new Volume();
        newVolume.setUuid(volumeUuid);
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

        // save into DB
        newVolume = tableStorage.volumeSave(newVolume);
        return newVolume;
    }



}
