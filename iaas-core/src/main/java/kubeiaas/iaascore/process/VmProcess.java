package kubeiaas.iaascore.process;

import com.alibaba.fastjson.JSON;
import kubeiaas.common.bean.Host;
import kubeiaas.common.bean.Image;
import kubeiaas.common.bean.Vm;
import kubeiaas.common.bean.Volume;
import kubeiaas.common.constants.HostSelectStrategyConstants;
import kubeiaas.common.constants.bean.VmConstants;
import kubeiaas.common.enums.image.ImageStatusEnum;
import kubeiaas.common.enums.vm.VmStatusEnum;
import kubeiaas.common.enums.volume.VolumeStatusEnum;
import kubeiaas.common.utils.UuidUtils;
import kubeiaas.iaascore.config.AgentConfig;
import kubeiaas.iaascore.dao.TableStorage;
import kubeiaas.iaascore.exception.BaseException;
import kubeiaas.iaascore.scheduler.ResourceScheduler;
import kubeiaas.iaascore.scheduler.VmScheduler;
import kubeiaas.iaascore.scheduler.VolumeScheduler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.sql.Timestamp;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class VmProcess {

    @Resource
    private TableStorage tableStorage;

    @Resource
    private ResourceScheduler resourceScheduler;

    @Resource
    private VmScheduler vmScheduler;

    @Resource
    private VolumeScheduler volumeScheduler;

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

    public void createVolume(Vm newVm) throws BaseException {
        log.info("createVm -- 4. Volume");

        String volumeUuid = volumeScheduler.createSystemVolume(newVm.getUuid());
        if (volumeUuid.isEmpty()) {
            throw new BaseException("ERROR: create system volume failed! (pre error)");
        }
        String newVmUuid = newVm.getUuid();
        // Attention: copying image is long-time operation, so start a new Thread to handle this.
        new Thread(() -> {
            Volume volume = tableStorage.volumeQueryByUuid(volumeUuid);

            // TODO: Constants
            // int waitLoop = VolumeConstants.CREATING_WAIT_LOOP;
            int waitLoop = 60;

            try {   // when copy is done, volume status will change in database, so query volume status at regular time.
                while (!volume.getStatus().equals(VolumeStatusEnum.AVAILABLE) &&
                        !volume.getStatus().equals(VolumeStatusEnum.ERROR_PREPARE) &&
                        waitLoop > 0) {
                    waitLoop--;

                    // TODO: Constants
                    // TimeUnit.SECONDS.sleep(VolumeConstants.CREATING_WAIT_TIME);
                    TimeUnit.SECONDS.sleep(5);

                    volume = tableStorage.volumeQueryByUuid(volumeUuid);
                }
                if (waitLoop == 0 || volume.getStatus().equals(VolumeStatusEnum.ERROR_PREPARE)) {
                    // 规定时间内未复制完成或者复制失败
                    log.error("ERROR: create system volume failed! (time out)");
                    return;
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
                log.error("ERROR: create system volume failed! (loop error)");
                return;
            }
            log.info("createVm -- 4. volume create success!");

            /* ---- 5. create VM ----
            Generate XML for libvirt & Attach volume
            （实际创建：生成 xml，挂载系统盘，启动）
             */
            try {
                createVM(newVmUuid);
            }catch (BaseException e){
                log.error(e.getMsg());
                return;
            }
            AgentConfig.clearSelectedHost(newVmUuid);

        }).start();
        log.info("createVm -- newThread begin wait for volume creating...");
    }

    public void createVM(String newVmUuid) throws BaseException {
        log.info("createVm -- 5. VM");
        if (!vmScheduler.createVmInstance(newVmUuid)) {
            throw new BaseException("ERROR: create vm instance failed!");
        }
        log.info("createVm -- 5. VM create success!");
    }

}
