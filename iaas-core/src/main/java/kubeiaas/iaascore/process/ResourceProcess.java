package kubeiaas.iaascore.process;

import kubeiaas.common.bean.*;
import kubeiaas.common.constants.HostSelectStrategyConstants;
import kubeiaas.common.enums.image.ImageStatusEnum;
import kubeiaas.common.enums.network.IpTypeEnum;
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
        log.info("createVm -- 2. Resource Operator");

        // 2.1. check image
        // available
        Image image = tableStorage.imageQueryByUuid(newVm.getImageUuid());
        if (image == null || image.getStatus() != ImageStatusEnum.AVAILABLE) {
            throw new VmException(newVm,"ERROR: image not available!");
        }
        // mem
        /*
        if (newVm.getMemory() < image.getMinMem()) {
            throw new VmException(newVm,"ERROR: memory too low for image!");
        }
         */
        // size
        int imageMinDisk = image.getVdSize();
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
            throw new VmException(newVm, "ERROR: no available host.");
        }
        log.info("selected host: " + selectedHost.getName());

        // 2.3. check IP segment
        IpSegment privateIpSeg;
        if (privateIpSegId == -1) {
            // no select private ip
            // TODO: call resourceScheduler to alloc
            throw new VmException(newVm, "ERROR: no available privateIpSeg.");
        } else {
            // check private host
            privateIpSeg = tableStorage.ipSegmentQueryById(privateIpSegId);
            if (!privateIpSeg.getHostUuid().equals(selectedHost.getUuid())
                    || !privateIpSeg.getType().equals(IpTypeEnum.PRIVATE)) {
                throw new VmException(newVm, "ERROR: not available privateIpSeg.");
            }
        }

        IpSegment publicIpSeg;
        if (publicIpSegId == -1) {
            if (needPublicIp) {
                // no select public ip
                // TODO: call resourceScheduler to alloc
                throw new VmException(newVm, "ERROR: no available publicIpSeg.");
            } else {
                log.info("-- no need public ip");
            }
        } else {
            // check public host
            publicIpSeg = tableStorage.ipSegmentQueryById(publicIpSegId);
            if (!privateIpSeg.getHostUuid().equals(selectedHost.getUuid())
                    || !publicIpSeg.getType().equals(IpTypeEnum.PUBLIC)) {
                throw new VmException(newVm, "ERROR: not available publicIpSeg.");
            }
        }

        // set scheduler of iaas-agent
        AgentConfig.setSelectedHost(newVm.getUuid(), selectedHost);
        // save into DB
        newVm.setHostUuid(selectedHost.getUuid());
        tableStorage.vmSave(newVm);

        log.info("createVm -- 2. Resource Operator success!");
        return newVm;
    }

    public Volume createVolumeOperate(Volume newVolume) throws VolumeException {
        Host selectedHost;
        String hostUUid = newVolume.getHostUuid();

        if (hostUUid != null && !hostUUid.isEmpty()) {
            // 指定 host：检查可用性
            selectedHost = resourceScheduler.vmSelectHostByHostUuid(hostUUid);
        } else {
            // 未指定 host：主动选择
            selectedHost = resourceScheduler.selectHostByHostOperator(HostSelectStrategyConstants.ROUND_ROBIN);
        }
        if (selectedHost == null) {
            throw new VolumeException(newVolume,"ERROR: no available host.");
        }
        log.info("selected host: " + selectedHost.getName());
        // set scheduler of iaas-agent
        AgentConfig.setSelectedHost(newVolume.getUuid(), selectedHost);
        // save into DB
        newVolume.setHostUuid(selectedHost.getUuid());
        newVolume = tableStorage.volumeSave(newVolume);

        log.info("createVolume -- Resource Operator success!");
        return newVolume;
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
            selectedHost = resourceScheduler.vmSelectHostByAppoint(vm.getUuid(), hostUUid);
        } else {
            // 若不存在，抛出服务器异常错误
            throw new BaseException("Error: HostUuid error!");
        }
        if (selectedHost == null) {
            throw new BaseException("ERROR: no available host.");
        }

        log.info("selected host: " + selectedHost.getName());
        AgentConfig.setSelectedHost(vm.getUuid(), selectedHost);
    }

    /**
     * SelectHost by VolumeUuid
     */
    public void selectHostByVolumeUuid(String volumeUuid) throws BaseException {

        // get Volume By volumeUuid
        Volume volume = tableStorage.volumeQueryByUuid(volumeUuid);

        // SelectHost
        Host selectedHost;
        String hostUUid = volume.getHostUuid();
        if (hostUUid != null && !hostUUid.isEmpty()) {
            // 获取Volume所在的Host
            selectedHost = resourceScheduler.vmSelectHostByHostUuid(hostUUid);
        } else {
            // 若不存在，抛出服务器异常错误
            throw new BaseException("Error: HostUuid error!");
        }
        if (selectedHost == null) {
            throw new BaseException("ERROR: no available host.");
        }

        log.info("selected host: " + selectedHost.getName());
        AgentConfig.setSelectedHost(volume.getUuid(), selectedHost);
    }

}
