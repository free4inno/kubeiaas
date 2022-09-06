package kubeiaas.iaascore.service;

import kubeiaas.common.bean.*;
import kubeiaas.common.constants.HostSelectStrategyConstants;
import kubeiaas.common.constants.bean.VmConstants;
import kubeiaas.common.constants.bean.VolumeConstants;
import kubeiaas.common.enums.image.ImageStatusEnum;
import kubeiaas.common.enums.network.IpAttachEnum;
import kubeiaas.common.enums.network.IpTypeEnum;
import kubeiaas.common.enums.vm.VmStatusEnum;
import kubeiaas.common.enums.volume.VolumeStatusEnum;
import kubeiaas.common.enums.volume.VolumeUsageEnum;
import kubeiaas.common.utils.UuidUtils;
import kubeiaas.iaascore.config.AgentConfig;
import kubeiaas.iaascore.dao.TableStorage;
import kubeiaas.iaascore.dao.feign.VolumeController;
import kubeiaas.iaascore.process.NetworkProcess;
import kubeiaas.iaascore.scheduler.VmScheduler;
import kubeiaas.iaascore.scheduler.VolumeScheduler;
import kubeiaas.iaascore.scheduler.DhcpScheduler;
import kubeiaas.iaascore.scheduler.ResourceScheduler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.sql.Timestamp;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class VmService {

    @Resource
    private TableStorage tableStorage;

    @Resource
    private VolumeScheduler volumeScheduler;

    @Resource
    private VmScheduler vmScheduler;

    @Resource
    private ResourceScheduler resourceScheduler;

    @Resource
    private DhcpScheduler dhcpScheduler;

    @Resource
    private NetworkProcess networkProcess;

    public String createVm(
            String name,
            int cpus,
            int memory,
            String imageUuid,
            int ipSegmentId,
            Integer diskSize,
            String description,
            String hostUUid) {

        // todo: 这些过程分散到各个 process 中，目前方便作为 demo 统一合并在这里

        /* ---- 1. pre create VM ----
        Generate and Set basic info of vm.
        （预处理：设置基础参数，保存虚拟机信息）
         */
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
        tableStorage.vmSave(newVm);

        log.info("createVm -- 1. pre create success!");


        /* ---- 2. Resource Operator ----
        Use Resource Operator to allocate Host and check available
        （资源调度：分配宿主机，检查资源合法性）
         */
        log.info("createVm -- 2. Resource Operator");
        newVm = tableStorage.vmQueryByUuid(newVmUuid);
        // 2.1. check image
        // available
        Image image = tableStorage.imageQueryByUuid(imageUuid);
        if (image == null || image.getStatus() != ImageStatusEnum.AVAILABLE) {
            return "ERROR: image not available!";
        }
        // mem
        if (newVm.getMemory() < image.getMinMem()) {
            return "ERROR: memory too low for image!";
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
        if (hostUUid != null && !hostUUid.isEmpty()) {
            // 指定 host：检查可用性
            selectedHost = resourceScheduler.vmSelectHostByAppoint(newVm.getUuid(), hostUUid);
        } else {
            // 未指定 host：主动选择
            selectedHost = resourceScheduler.vmSelectHostByOperator(newVm.getUuid(), HostSelectStrategyConstants.ROUND_ROBIN);
        }
        if (selectedHost == null) {
            return "ERROR: no available host.";
        }
        log.info("selected host: " + selectedHost.getName());
        // set scheduler of iaas-agent
        AgentConfig.setSelectedHost(selectedHost);
        // save into DB
        newVm.setHostUuid(selectedHost.getUuid());
        tableStorage.vmSave(newVm);

        log.info("createVm -- 2. Resource Operator success!");


        /* ---- 3. Network ----
        Get mac-info ip-info and bind in DHCP-Controller
        （网络信息：分配 mac 与 ip，存储入库，dhcp 绑定）
         */
        log.info("createVm -- 3. Network");

        String newMac = networkProcess.getNewMac(ipSegmentId);
        IpUsed newIpUsed = networkProcess.getNewIp(ipSegmentId);
        if (newIpUsed == null) {
            return "ERROR: ip allocated failed!";
        }
        // already set: ip, ip_segment_id, type.
        newIpUsed.setMac(newMac);
        newIpUsed.setInstanceUuid(newVmUuid);
        newIpUsed.setCreateTime(new Timestamp(System.currentTimeMillis()));
        newIpUsed.setStatus(IpAttachEnum.DETACHED);
        newIpUsed.setType(IpTypeEnum.PRIVATE);

        log.info("new mac: " + newIpUsed.getMac());
        log.info("new ip: " + newIpUsed.getIp());

        // save into DB
        tableStorage.ipUsedSave(newIpUsed);

        // bind in DHCP-Controller
        newIpUsed = tableStorage.ipUsedQueryByIp(newIpUsed.getIp());  // 重新 query 拿到 id
        if (!dhcpScheduler.bindMacAndIp(newIpUsed)) {
            return "ERROR: dhcp bind mac & ip failed!";
        }

        log.info("createVm -- 3. network success!");


        /* ---- 4. Volume ----
        Create system volume.
        （系统盘：使用 image 创建 system volume）
         */
        log.info("createVm -- 4. Volume");

        String volumeUuid = volumeScheduler.createSystemVolume(newVmUuid);
        if (volumeUuid.isEmpty()) {
            return "ERROR: create system volume failed! (pre error)";
        }
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
            if (!vmScheduler.createVmInstance(newVmUuid)) {
                log.error("ERROR: create vm instance failed!");
            }
        }).start();

        log.info("createVm -- newThread begin wait for volume creating...");
        return "success, please wait for creating...";
    }
}
