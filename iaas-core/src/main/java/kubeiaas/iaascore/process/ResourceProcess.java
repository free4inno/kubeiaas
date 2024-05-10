package kubeiaas.iaascore.process;

import kubeiaas.common.bean.*;
import kubeiaas.common.constants.HostSelectStrategyConstants;
import kubeiaas.common.enums.image.ImageFormatEnum;
import kubeiaas.common.enums.image.ImageStatusEnum;
import kubeiaas.common.enums.network.IpTypeEnum;
import kubeiaas.common.enums.vm.VmInstallationMethodEnum;
import kubeiaas.iaascore.config.AgentConfig;
import kubeiaas.iaascore.dao.TableStorage;
import kubeiaas.iaascore.exception.BaseException;
import kubeiaas.iaascore.exception.VmException;
import kubeiaas.iaascore.exception.VolumeException;
import kubeiaas.iaascore.scheduler.ResourceScheduler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Objects;
import java.util.Optional;

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
     * @param newVm 经过 preCreate 的 newVm
     * @param privateIpSegId 私网网段id（-1代表未指定）
     * @param needPublicIp 是否需要公网IP
     * @param publicIpSegId 公网网段id（-1代表未指定）
     * @return Vm
     * @throws VmException VmException
     */
    public Vm createVmOperate(Vm newVm, int privateIpSegId, boolean needPublicIp, int publicIpSegId) throws VmException {
        log.info(String.format("createVmOperate -- vm: %s, privateIpSegId: %d, needPublicIp: %b, publicIpSegId: %d",
                newVm.getUuid(), privateIpSegId, needPublicIp, publicIpSegId));

        // 2.1. select host
        Host selectedHost;
        String hostUUid = newVm.getHostUuid();

        if (hostUUid != null && !hostUUid.isEmpty()) {
            // 指定 host：检查可用性
            log.info("-> invoke resourceScheduler -- vmSelectHostByAppoint");
            selectedHost = resourceScheduler.vmSelectHostByAppoint(newVm.getUuid(), hostUUid);
            log.info("<- invoke resourceScheduler -- done");
        } else {
            // 未指定 host：主动选择
            log.info("-> invoke resourceScheduler -- vmSelectHostByOperator");
            selectedHost = resourceScheduler.vmSelectHostByOperator(newVm.getUuid(), HostSelectStrategyConstants.ROUND_ROBIN);
            log.info("<- invoke resourceScheduler -- done");
        }
        if (selectedHost == null) {
            log.error("ERROR: no available host.");
            throw new VmException(newVm, "ERROR: no available host.");
        }
        log.info(String.format("selected host: %s", selectedHost.getName()));

        // 2.3. check IP segment
        IpSegment privateIpSeg;
        if (privateIpSegId == -1) {
            // no select private ip
            // TODO: call resourceScheduler to alloc
            log.error("ERROR: no available privateIpSeg.");
            throw new VmException(newVm, "ERROR: no available privateIpSeg.");
        } else {
            // check private host
            privateIpSeg = tableStorage.ipSegmentQueryById(privateIpSegId);
            if (!privateIpSeg.getHostUuid().equals(selectedHost.getUuid())
                    || !privateIpSeg.getType().equals(IpTypeEnum.PRIVATE)) {
                log.error("ERROR: not available privateIpSeg.");
                throw new VmException(newVm, "ERROR: not available privateIpSeg.");
            }
        }

        IpSegment publicIpSeg;
        if (publicIpSegId == -1) {
            if (needPublicIp) {
                // no select public ip
                // TODO: call resourceScheduler to alloc
                log.error("ERROR: no available publicIpSeg.");
                throw new VmException(newVm, "ERROR: no available publicIpSeg.");
            } else {
                log.debug("-- no need public ip");
            }
        } else {
            // check public host
            publicIpSeg = tableStorage.ipSegmentQueryById(publicIpSegId);
            if (!privateIpSeg.getHostUuid().equals(selectedHost.getUuid())
                    || !publicIpSeg.getType().equals(IpTypeEnum.PUBLIC)) {
                log.error("ERROR: not available publicIpSeg.");
                throw new VmException(newVm, "ERROR: not available publicIpSeg.");
            }
        }

        // set scheduler of iaas-agent
        AgentConfig.setSelectedHost(newVm.getUuid(), selectedHost);
        // save into DB
        newVm.setHostUuid(selectedHost.getUuid());
        log.info("-> invoke DB -- vmSave");
        tableStorage.vmSave(newVm);
        log.info("<- invoke DB -- done");

        log.debug("createVm -- 2. Resource Operator success!");
        return newVm;
    }

    public Volume createVolumeOperate(Volume newVolume) throws VolumeException {
        Host selectedHost;
        String hostUUid = newVolume.getHostUuid();

        if (hostUUid != null && !hostUUid.isEmpty()) {
            // 指定 host：检查可用性
            log.info("-> invoke resourceScheduler -- vmSelectHostByHostUuid");
            selectedHost = resourceScheduler.vmSelectHostByHostUuid(hostUUid);
            log.info("<- invoke resourceScheduler -- done");
        } else {
            // 未指定 host：主动选择
            log.info("-> invoke resourceScheduler -- selectHostByHostOperator");
            selectedHost = resourceScheduler.selectHostByHostOperator(HostSelectStrategyConstants.ROUND_ROBIN);
            log.info("<- invoke resourceScheduler -- done");
        }

        if (selectedHost == null) {
            log.error("ERROR: resource operator failed to select host.");
            throw new VolumeException(newVolume, "ERROR: no available host.");
        }
        log.info(String.format("volume host selected -- hostName: %s", selectedHost.getName()));

        // set scheduler of iaas-agent
        AgentConfig.setSelectedHost(newVolume.getUuid(), selectedHost);

        // save into DB
        newVolume.setHostUuid(selectedHost.getUuid());
        log.info("-> invoke DB -- update volume host info");
        newVolume = tableStorage.volumeSave(newVolume);
        log.info("<- invoke DB -- done");

        return newVolume;
    }

    public Vm updateVmImage(Vm vm) throws VmException {
        log.info(String.format("-> invoke DB -- imageQueryByUuid: %s", vm.getImageUuid()));
        Image image = tableStorage.imageQueryByUuid(vm.getImageUuid());
        if (image == null) {
            log.error("ERROR: image not found!");
            throw new VmException(vm, "ERROR: image not found!");
        }
        if (image.getFormat() == ImageFormatEnum.ISO) {
            log.info("ISO image, set installation method to ISO");
            vm.setInstallationMethod(VmInstallationMethodEnum.ISO);
        } else {
            log.info("Snapshot image, set installation method to SNAPSHOT");
            vm.setInstallationMethod(VmInstallationMethodEnum.SNAPSHOT);
            log.info(String.format("<- invoke DB -- done -- image: %s", image.toString()));
            if (image.getStatus() != ImageStatusEnum.AVAILABLE) {
                log.error("ERROR: image not available!");
                throw new VmException(vm, "ERROR: image not available!");
            }

            // size
            int imageMinDisk = image.getVdSize();
            int newVmDiskSize = vm.getDiskSize();
            if (newVmDiskSize < imageMinDisk) {
                // 强制修改 diskSize，不终止
                vm.setDiskSize(imageMinDisk);
            }

        }
        return vm;
    }

    /**
    * SelectHost by VmUuid
     */
    public void selectHostByVmUuid(String VmUuid) throws BaseException {
        // get Vm By VmUuid
        Vm vm = tableStorage.vmQueryByUuid(VmUuid);

        // SelectHost
        Host selectedHost;
        String hostUUid = vm.getHostUuid();
        if (hostUUid != null && !hostUUid.isEmpty()) {
            // 获取Vm所在的Host
            log.info("-> invoke resourceScheduler -- vmSelectHostByAppoint");
            selectedHost = resourceScheduler.vmSelectHostByAppoint(vm.getUuid(), hostUUid);
            log.info("<- invoke resourceScheduler -- done");
        } else {
            // 若不存在，抛出服务器异常错误
            log.error("ERROR: HostUuid error!");
            throw new BaseException("Error: HostUuid error!");
        }
        if (selectedHost == null) {
            log.error("ERROR: no available host.");
            throw new BaseException("ERROR: no available host.");
        }

        log.info(String.format("selected host: %S", selectedHost.getName()));
        AgentConfig.setSelectedHost(vm.getUuid(), selectedHost);
    }

    /**
     * SelectHost by VolumeUuid
     */
    public void selectHostByVolumeUuid(String volumeUuid) throws BaseException {
        // get Volume By volumeUuid
        log.info("-> invoke DB -- volumeQueryByUuid");
        Volume volume = tableStorage.volumeQueryByUuid(volumeUuid);
        log.info("<- invoke DB -- done");

        selectHostByVolume(volume);
    }

    public void selectHostByVolume(Volume volume) throws BaseException {
        Host selectedHost;
        String hostUUid = volume.getHostUuid();
        if (hostUUid != null && !hostUUid.isEmpty()) {
            // 获取Volume所在的Host
            log.info("-> invoke resourceScheduler -- vmSelectHostByHostUuid");
            selectedHost = resourceScheduler.vmSelectHostByHostUuid(hostUUid);
            log.info("<- invoke resourceScheduler -- done");
        } else {
            // 若不存在，抛出服务器异常错误
            log.error("ERROR: HostUuid error!");
            throw new BaseException("Error: HostUuid error!");
        }
        if (selectedHost == null) {
            log.error("ERROR: no available host.");
            throw new BaseException("ERROR: no available host.");
        }

        log.info(String.format("selected host: %s", selectedHost.getName()));
        AgentConfig.setSelectedHost(volume.getUuid(), selectedHost);
    }

    public Optional<Host> selectOptimalHost() {
        return Optional.ofNullable(resourceScheduler.selectHostByHostOperator(HostSelectStrategyConstants.ROUND_ROBIN));
    }
}
