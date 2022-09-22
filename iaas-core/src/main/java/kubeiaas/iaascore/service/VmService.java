package kubeiaas.iaascore.service;

import com.alibaba.fastjson.JSON;
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
import kubeiaas.iaascore.exception.BaseException;
import kubeiaas.iaascore.process.NetworkProcess;
import kubeiaas.iaascore.process.VmProcess;
import kubeiaas.iaascore.response.BaseResponse;
import kubeiaas.iaascore.response.ResponseEnum;
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

    @Resource
    private VmProcess vmProcess;

    public BaseResponse createVm(
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
        Vm newVm = vmProcess.preCreate(name, cpus, memory, imageUuid, diskSize, description, hostUUid);

        /* ---- 2. Resource Operator ----
        Use Resource Operator to allocate Host and check available
        （资源调度：分配宿主机，检查资源合法性）
         */
        try{
            newVm = vmProcess.createResourceOperate(newVm);
        } catch (BaseException e){
            log.error(e.getMsg());
            return BaseResponse.error(ResponseEnum.ERROR);
        }


        /* ---- 3. Network ----
        // TODO: 异步执行可能导致冲突，考虑分散到 process后通过 synchronized保证同步
        Get mac-info ip-info and bind in DHCP-Controller
        （网络信息：分配 mac 与 ip，存储入库，dhcp 绑定）
         */
        try {
            IpUsed newIpUsed = networkProcess.createNetwork(newVm, ipSegmentId);
        }catch (BaseException e){
            log.error(e.getMsg());
            return BaseResponse.error(ResponseEnum.ERROR);
        }


        /* ---- 4. Volume ----
        Create system volume.
        （系统盘：使用 image 创建 system volume）
         */
        try {
            vmProcess.createVolume(newVm);
        }catch (BaseException e){
            log.error(e.getMsg());
            return BaseResponse.error(ResponseEnum.ERROR);
        }

        return BaseResponse.success("success, please wait for creating...");
//        return "success, please wait for creating...";
    }
}
