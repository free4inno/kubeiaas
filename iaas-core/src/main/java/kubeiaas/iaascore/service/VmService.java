package kubeiaas.iaascore.service;

import kubeiaas.common.bean.Host;
import kubeiaas.common.bean.Image;
import kubeiaas.common.bean.IpUsed;
import kubeiaas.common.bean.Vm;
import kubeiaas.common.constants.HostSelectStrategyConstants;
import kubeiaas.common.constants.bean.VmConstants;
import kubeiaas.common.enums.image.ImageStatusEnum;
import kubeiaas.common.enums.vm.VmStatusEnum;
import kubeiaas.common.utils.UuidUtils;
import kubeiaas.iaascore.dao.TableStorage;
import kubeiaas.iaascore.process.NetworkProcess;
import kubeiaas.iaascore.scheduler.AgentScheduler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.sql.Timestamp;

@Slf4j
@Service
public class VmService {

    @Resource
    private TableStorage tableStorage;

    @Resource
    private AgentScheduler agentScheduler;

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

        /* ---- 1. pre create VM ----
        Generate and Set basic info of vm.
        （预处理：设置基础参数，保存虚拟机信息）
         */
        Vm newVm = new Vm();

        // 1.1. set basic
        String uuid = UuidUtils.getRandomUuid();
        newVm.setUuid(uuid);
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


        /* ---- 2. Resource Operator ----
        Use Resource Operator to allocate Host and check available
        （资源调度：分配宿主机，检查资源合法性）
         */
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
            selectedHost = tableStorage.vmSelectHostByAppoint(newVm.getUuid(), hostUUid);
        } else {
            // 未指定 host：主动选择
            selectedHost = tableStorage.vmSelectHostByOperator(newVm.getUuid(), HostSelectStrategyConstants.ROUND_ROBIN);
        }
        if (selectedHost == null) {
            return "ERROR: no available host.";
        }
        agentScheduler.setTargetHost(selectedHost);


        /* ---- 3. Network ----
        Get mac-info ip-info and bind in DHCP-Controller
        （网络信息：分配 mac 与 ip，存储入库，dhcp 绑定）
         */
        String newMac = networkProcess.getNewMac(ipSegmentId);
        IpUsed newIpUsed = networkProcess.getNewIp(ipSegmentId);
        if (newIpUsed == null) {
            return "ERROR: ip allocated failed!";
        }
        // already set: ip, ip_segment_id, type.
        newIpUsed.setMac(newMac);
        newIpUsed.setInstanceUuid(newVm.getUuid());
        newIpUsed.setCreateTime(new Timestamp(System.currentTimeMillis()));
        // save into DB
        tableStorage.ipUsedSave(newIpUsed);
        // bind in DHCP-Controller


        /* ---- 4. Volume ----
        Create system volume.
        （系统盘：使用 image 创建 system volume）
         */


        /* ---- 5. create VM ----
        Generate XML for libvirt & Attach volume
        （实际创建：生成 xml，挂载系统盘，启动）
         */


        return "success";
    }
}
