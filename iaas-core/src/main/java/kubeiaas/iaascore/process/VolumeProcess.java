package kubeiaas.iaascore.process;

import kubeiaas.common.bean.Vm;
import kubeiaas.common.bean.Volume;
import kubeiaas.common.constants.bean.VolumeConstants;
import kubeiaas.common.enums.vm.VmStatusEnum;
import kubeiaas.common.enums.volume.VolumeStatusEnum;
import kubeiaas.iaascore.config.AgentConfig;
import kubeiaas.iaascore.dao.TableStorage;
import kubeiaas.iaascore.exception.BaseException;
import kubeiaas.iaascore.exception.VmException;
import kubeiaas.iaascore.scheduler.VolumeScheduler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
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

    public void deleteVolume(String vmUuid) throws BaseException {
        log.info("deleteVolume ==== start ====  vmUuid: " + vmUuid);
        List<Volume> volumes = tableStorage.volumeQueryAllByInstanceUuid(vmUuid);
        for (Volume volume : volumes) {
            if (!volumeScheduler.deleteSystemVolume(vmUuid,volume.getUuid(),volume.getProviderLocation())){
                throw new BaseException("ERROR: delete volume:"+volume.getUuid()+" failed!");
            }
        }
        log.info("deleteVolume ==== end ==== vmUuid:" + vmUuid);
    }

}
