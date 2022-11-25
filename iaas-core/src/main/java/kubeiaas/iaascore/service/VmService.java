package kubeiaas.iaascore.service;

import kubeiaas.common.bean.*;
import kubeiaas.common.constants.ResponseMsgConstants;
import kubeiaas.common.constants.bean.VmConstants;
import kubeiaas.common.enums.vm.VmOperateEnum;
import kubeiaas.common.enums.vm.VmStatusEnum;
import kubeiaas.iaascore.config.AgentConfig;
import kubeiaas.iaascore.dao.TableStorage;
import kubeiaas.iaascore.exception.BaseException;
import kubeiaas.iaascore.exception.VmException;
import kubeiaas.iaascore.process.*;
import kubeiaas.iaascore.response.VmPageResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class VmService {

    @Resource
    private TableStorage tableStorage;

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

    /**
     * 创建虚拟机
     */
    public Vm createVm(
            String name,
            int cpus,
            int memory,
            String imageUuid,
            int ipSegmentId,
            Integer diskSize,
            String description,
            String hostUUid) throws VmException {

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

    /**
     * 删除虚拟机
     */
    public String deleteVM(String vmUuid, boolean isForce) throws BaseException, VmException {
        // ----- check if exist -----
        Vm vm = tableStorage.vmQueryByUuid(vmUuid);
        if (vm == null) {
            throw new BaseException("ERR: vm not found! (uuid: " + vmUuid + ")");
        }

        /* ----- judge status ----
        Check the VM status
        */
        if (!isForce) {
            if (vm.getStatus().equals(VmStatusEnum.ACTIVE)) {
                return ResponseMsgConstants.FAILED;
            }
        }

        try {
            /* -----0. set status -----
            Set deleting status
             */
            vmProcess.setVmStatus(vmUuid, VmStatusEnum.DELETING);

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
            volumeProcess.deleteSystemVolume(vmUuid);

            /* -----4. Delete Ip ----
            Delete Ip information
            */
            networkProcess.deleteIps(vmUuid);

            /* -----5. delete in database ----
            Delete VM records from the database
            */
            vmProcess.deleteVmInDataBase(vmUuid);

            /* -----6. delete vnc ----
            delete vnc in token.config
            */
            vncProcess.deleteVncToken(vmUuid);

        } catch (Exception e) {
            e.printStackTrace();
            throw new VmException(vm, "ERR: error while delete! (uuid: " + vmUuid + ")");
        }

        return ResponseMsgConstants.SUCCESS;
    }

    /**
     * ====== 修改系列 ======
     * 支持：CPU、内存
     */
    public String modifyVm(String vmUuid, Integer cpus, Integer memory, boolean isReduce) throws BaseException {
        /* -----1. choose host ----
        Select the host where the VM to be modified
        */
        resourceProcess.selectHostByVmUuid(vmUuid);

        /* -----2. modify VM ----
        Modify cpu and memory
        */
        if (isReduce) {
            vmProcess.reduceVM(vmUuid, cpus, memory);
        } else {
            vmProcess.modifyVM(vmUuid, cpus, memory);
        }

        return ResponseMsgConstants.SUCCESS;
    }

    /**
     * ====== 操作系列 ======
     * 支持：STOP、START、REBOOT、RESUME、SUSPEND
     */
    public String operateVm(String vmUuid, VmOperateEnum operation) throws BaseException {
        /* -----1. choose host ----
        Select the host where the VM to be modified
            > AgentConfig.setSelectedHost();
        */
        resourceProcess.selectHostByVmUuid(vmUuid);

        /* -----2. operate VM ----*/
        switch (operation) {
            case STOP:
                // Stop VM ----
                vmProcess.stopVM(vmUuid);
                break;

            case START:
                // start VM ----
                vmProcess.startVM(vmUuid);
                // flush vnc ----
                vncProcess.flushVncToken(vmUuid);
                break;

            case REBOOT:
                // reboot VM ----
                vmProcess.rebootVM(vmUuid);
                // flush vnc ----
                vncProcess.flushVncToken(vmUuid);
                break;

            case RESUME:
                // resume VM ----
                vmProcess.resumeVM(vmUuid);
                // flush vnc ----
                vncProcess.flushVncToken(vmUuid);
                break;

            case SUSPEND:
                // suspend VM ----
                vmProcess.suspendVM(vmUuid);
                break;

            default:
                AgentConfig.clearSelectedHost(vmUuid);
                throw new BaseException("ERR: unknown vm operation!");
        }

        AgentConfig.clearSelectedHost(vmUuid);

        return ResponseMsgConstants.SUCCESS;
    }


    /**
     * ============ QUERY 查询  ============
     *
     * 1. QUERY_ALL 查询全部
     *    - param:
     *    - return: List
     *
     * 2. PAGE_QUERY_ALL 分页查询全部
     *    - param: Integer pageNum, Integer pageSize
     *    - return: VmPageResponse
     *
     * 3. QUERY_FUZZY 模糊查询
     *    - param: String keyWords, VmStatusEnum status, String hostUuid, String imageUuid
     *    - return: List
     *
     * 4. PAGE_QUERY_FUZZY 分页模糊查询
     *    - param: String keyWords, VmStatusEnum status, String hostUuid, String imageUuid, Integer pageNum, Integer pageSize
     *    - return: VmPageResponse
     *
     * 5. QUERY_BY_XXX 特定查询
     *
     */

    public List<Vm> queryAll() {
        // 1. get from DB
        List<Vm> vmList = tableStorage.vmQueryAll();
        // 2. build & return
        return vmProcess.buildVmList(vmList);
    }

    public VmPageResponse pageQueryAll(Integer pageNum, Integer pageSize) {
        // 1. get from DB
        VmPageResponse vmPage = tableStorage.vmPageQueryAll(pageNum, pageSize);
        // 2. build & return
        List<Vm> vmList = vmPage.getContent();
        vmPage.setContent(vmProcess.buildVmList(vmList));
        return vmPage;
    }

    public Vm queryByUuid(String uuid) {
        Vm vm = tableStorage.vmQueryByUuid(uuid);

        // get ips
        List<IpUsed> ipUsedList = tableStorage.ipUsedQueryAllByInstanceUuid(vm.getUuid());
        vm.setIps(ipUsedList);

        // get images
        Image image = tableStorage.imageQueryByUuid(vm.getImageUuid());
        vm.setImage(image);

        // get volumes
        List<Volume> volumeList = tableStorage.volumeQueryAllByInstanceUuid(vm.getUuid());
        vm.setVolumes(volumeList);

        // get hosts
        Host host = tableStorage.hostQueryByUuid(vm.getHostUuid());
        vm.setHost(host);

        return vm;
    }

    /**
     * 获取 Vnc 链接
     * 1. 根据数据库中 host IP
     * 2. 根据配置中获取 Domain 域名模板
     */
    public String getVncUrl(String vmUuid) {
        // 1. Analyze `vnc` host from DB hostRoles.
        // Host host = tableStorage.hostQueryByRole(HostConstants.ROLE_VNC);
        // return String.format(VmConstants.VNC_URL_TEMPLATE, host.getIp(), vmUuid);

        // 2. Analyze `vnc` host from Domain Name.
        return String.format(VmConstants.VNC_URL_TEMPLATE, vmUuid);
    }

    /**
     * 编辑基本信息
     * 支持字段：名称、描述
     */
    public Vm editVm(String vmUuid, String name, String description) throws BaseException {
        // 1. find VM
        Vm vm = tableStorage.vmQueryByUuid(vmUuid);
        if (vm == null) {
            throw new BaseException("ERROR: vm is not found!");
        }

        // 2. check is edit changed
        boolean editFlag = false;
        if (name != null && !name.isEmpty() && !name.equals(vm.getName())) {
            vm.setName(name);
            editFlag = true;
        }
        if (description != null && !description.isEmpty() && !description.equals(vm.getDescription())) {
            vm.setDescription(description);
            editFlag = true;
        }

        // 3. save into DB
        if (editFlag) {
            vm = tableStorage.vmUpdate(vm);
        }

        return vm;
    }

}
