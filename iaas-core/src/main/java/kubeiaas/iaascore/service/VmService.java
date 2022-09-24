package kubeiaas.iaascore.service;

import kubeiaas.common.bean.*;
import kubeiaas.iaascore.exception.BaseException;
import kubeiaas.iaascore.process.NetworkProcess;
import kubeiaas.iaascore.process.ResourceProcess;
import kubeiaas.iaascore.process.VmProcess;
import kubeiaas.iaascore.process.VolumeProcess;
import kubeiaas.iaascore.response.BaseResponse;
import kubeiaas.iaascore.response.ResponseEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class VmService {

    @Resource
    private NetworkProcess networkProcess;

    @Resource
    private VmProcess vmProcess;

    @Resource
    private ResourceProcess resourceProcess;

    @Resource
    private VolumeProcess volumeProcess;

    public BaseResponse createVm(
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
        Vm newVm = vmProcess.preCreateVm(name, cpus, memory, imageUuid, diskSize, description, hostUUid);

        /* ---- 2. Resource Operator ----
        Use Resource Operator to allocate Host and check available
        （资源调度：分配宿主机，检查资源合法性）
         */
        try {
            newVm = resourceProcess.createVmOperate(newVm);
        } catch (BaseException e) {
            log.error(e.getMsg());
            return BaseResponse.error(ResponseEnum.ERROR);
        }

        /* ---- 3. Network ----
        Get mac-info ip-info and bind in DHCP-Controller
        （网络信息：分配 mac 与 ip，存储入库，dhcp 绑定）
         */
        try {
            IpUsed newIpUsed = networkProcess.createVmNetwork(newVm, ipSegmentId);
            // set into newVm
            List<IpUsed> newIpUsedList = new ArrayList<>();
            newIpUsedList.add(newIpUsed);
            newVm.setIps(newIpUsedList);
        } catch (BaseException e) {
            log.error(e.getMsg());
            return BaseResponse.error(ResponseEnum.ERROR);
        }

        /* ---- 4. Volume ----
        Create system volume.
        （系统盘：使用 image 创建 system volume）
         */
        try {
            volumeProcess.createVmVolume(newVm);
        } catch (BaseException e) {
            log.error(e.getMsg());
            return BaseResponse.error(ResponseEnum.ERROR);
        }

        return BaseResponse.success(newVm);
    }
}
