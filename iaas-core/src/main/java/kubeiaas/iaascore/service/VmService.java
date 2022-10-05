package kubeiaas.iaascore.service;

import kubeiaas.common.bean.*;
import kubeiaas.common.constants.ResponseMsgConstants;
import kubeiaas.common.enums.vm.VmStatusEnum;
import kubeiaas.iaascore.exception.BaseException;
import kubeiaas.iaascore.process.*;
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

    @Resource
    private VncProcess vncProcess;

    public Vm createVm(
            String name,
            int cpus,
            int memory,
            String imageUuid,
            int ipSegmentId,
            Integer diskSize,
            String description,
            String hostUUid) throws BaseException {

        /* ---- 1. pre create VM ----
        Generate and Set basic info of vm.
        （预处理：设置基础参数，保存虚拟机信息）
         */
        Vm newVm = vmProcess.preCreateVm(name, cpus, memory, imageUuid, diskSize, description, hostUUid);

        /* ---- 2. Resource Operator ----
        Use Resource Operator to allocate Host and check available
        （资源调度：分配宿主机，检查资源合法性）
         */
        newVm = resourceProcess.createVmOperate(newVm);

        /* ---- 3. Network ----
        Get mac-info ip-info and bind in DHCP-Controller
        （网络信息：分配 mac 与 ip，存储入库，dhcp 绑定）
         */
        IpUsed newIpUsed = networkProcess.createVmNetwork(newVm, ipSegmentId);
        // set into newVm
        List<IpUsed> newIpUsedList = new ArrayList<>();
        newIpUsedList.add(newIpUsed);
        newVm.setIps(newIpUsedList);

        /* ---- 4. Volume ----
        Create system volume.
        （系统盘：使用 image 创建 system volume）
         */
        volumeProcess.createVmVolume(newVm);

        return newVm;
    }

    public String deleteVM(String vmUuid) throws BaseException {
        /* -----judge status ----
        Check the VM status
        */
        Vm vm = vmProcess.queryVMByUuid(vmUuid);
        if (vm.getStatus().equals(VmStatusEnum.ACTIVE)){
            return ResponseMsgConstants.FAILED;
        }else {

        /* -----1. choose host ----
        Select the host where the VM to be deleted resides
        */
            resourceProcess.selectHostByVmUuid(vmUuid);

        /* -----2. delete VM ----
        Delete the VM and then delete other information
        */
            vmProcess.deleteVM(vmUuid);
        /* -----3. Delete Volume ----
        Delete disks, including Linux files and database information
        */
            volumeProcess.deleteVolume(vmUuid);
        /* -----4. Delete Ip ----
        Delete Ip information
        */
            networkProcess.deleteIps(vmUuid);

        /* -----5. delete in database ----
        Delete VM records from the database
        */
            vmProcess.deleteVMInDataBase(vmUuid);

        /* -----6. delete vnc ----
        delete vnc in token.config
        */
            vncProcess.deleteVncToken(vmUuid);

            return ResponseMsgConstants.SUCCESS;
        }
    }

    public String forceDeleteVm(String vmUuid) throws BaseException {

        /* -----1. choose host ----
        Select the host where the VM to be deleted resides
        */
        resourceProcess.selectHostByVmUuid(vmUuid);

        /* -----2. delete VM ----
        Delete the VM and then delete other information
        */
        vmProcess.deleteVM(vmUuid);
        /* -----3. Delete Volume ----
        Delete disks, including Linux files and database information
        */
        volumeProcess.deleteVolume(vmUuid);
        /* -----4. Delete Ip ----
        Delete Ip information
        */
        networkProcess.deleteIps(vmUuid);

        /* -----5. delete in database ----
        Delete VM records from the database
        */
        vmProcess.deleteVMInDataBase(vmUuid);

        /* -----6. delete vnc ----
        delete vnc in token.config
        */
        vncProcess.deleteVncToken(vmUuid);

        return ResponseMsgConstants.SUCCESS;
    }

    public String modifyVm(String vmUuid, Integer cpus, Integer memory) throws BaseException {
        /* -----1. choose host ----
        Select the host where the VM to be modified
        */
        resourceProcess.selectHostByVmUuid(vmUuid);

        /* -----2. modify VM ----
        Modify cpu and memory
        */
        vmProcess.modifyVM(vmUuid, cpus, memory);

        return ResponseMsgConstants.SUCCESS;
    }

}
