package kubeiaas.iaascore.service;

import kubeiaas.common.bean.Vm;
import kubeiaas.common.bean.Volume;
import kubeiaas.common.constants.LogInjectionConstants;
import kubeiaas.common.constants.ResponseMsgConstants;
import kubeiaas.common.constants.bean.VolumeConstants;
import kubeiaas.common.enums.vm.VmStatusEnum;
import kubeiaas.common.enums.volume.VolumeStatusEnum;
import kubeiaas.iaascore.config.AgentConfig;
import kubeiaas.iaascore.dao.TableStorage;
import kubeiaas.iaascore.exception.BaseException;
import kubeiaas.iaascore.exception.VolumeException;
import kubeiaas.iaascore.process.ResourceProcess;
import kubeiaas.iaascore.process.VolumeProcess;
import kubeiaas.iaascore.response.PageResponse;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class VolumeService {

    @Resource
    VolumeProcess volumeProcess;

    @Resource
    ResourceProcess resourceProcess;

    @Resource
    TableStorage tableStorage;

    public Volume createDataVolume(
            String name,
            Integer diskSize,
            String description,
            String hostUuid) throws VolumeException {
        log.info(String.format("volume info -- name: %s, size: %s, hostUuid: %s", name, diskSize, hostUuid));

        // ---- 1. pre create Volume ----
        // Generate and Set basic info of volume.
        // 数据库预处理：设置基础参数，保存云硬盘信息
        log.info("STEP 1: create volume in DB");
        Volume newVolume= volumeProcess.preCreateVolume(name, description, hostUuid, diskSize);
        MDC.put(LogInjectionConstants.UUID_INJECTION, newVolume.getUuid());

        // ---- 2. check host ----
        // Use Resource Operator to allocate Host
        // 资源调度：分配宿主机
        log.info("STEP 2: select and check host");
        newVolume = resourceProcess.createVolumeOperate(newVolume);

        // ---- 3. create volume ----
        // 创建虚拟机数据盘
        log.info(String.format("STEP 3: create volume on host by agent -- volumeUuid: %s, hostUuid: %s",
                newVolume.getUuid(), newVolume.getHostUuid()));
        volumeProcess.createDataVolume(newVolume);

        return newVolume;
    }

    public String deleteDataVolume(String volumeUuid) throws BaseException {
        log.info(String.format("volume info -- volumeUuid: %s", volumeUuid));
         /* ---- 1. judge volume state ----
        （判断数据盘状态）
         */
        log.info("STEP 1: check volume status");
        log.info(String.format("-> invoke DB -- volumeQueryByUuid -- volumeUuid: %s", volumeUuid));
        Volume volume = tableStorage.volumeQueryByUuid(volumeUuid);
        log.info("<- invoke DB -- done");
        if (volume.getStatus().equals(VolumeStatusEnum.ATTACHED)) {
            return ResponseMsgConstants.FAILED;
        }

        /* -----2. choose host ----
        Select the host where the Volume to be deleted
        */
        log.info("STEP 2: select host");
        resourceProcess.selectHostByVolumeUuid(volumeUuid);

         /* -----3. delete VM ----
        Delete the VM and then delete other information
        */
        log.info("STEP 3: delete volume");
        volumeProcess.deleteDataVolume(volumeUuid);

        /* -----4. clear selected host ----
        Clear the selected host
         */
        log.info("STEP 4: clear selected host");
        AgentConfig.clearSelectedHost(volumeUuid);

        return ResponseMsgConstants.SUCCESS;
    }

    public String resizeSystemVolume(String volumeUuid, Integer diskSize) throws VolumeException, BaseException {
        log.info(String.format("volume info -- volumeUuid: %s, diskSize: %s", volumeUuid, diskSize));
        log.info(String.format("-> invoke DB -- volumeQueryByUuid -- volumeUuid: %s", volumeUuid));
        Volume volume = tableStorage.volumeQueryByUuid(volumeUuid);
        log.info("<- invoke DB -- done");
        /* -----1. check volume and vm status ----
         */
        log.info("STEP 1: check volume and vm status");
        if (!isAttachedVmStopped(volume)) {
            log.error("ERROR: VM is not STOPPED! You can't resize a running VM!");
            return ResponseMsgConstants.FAILED;
        }

        /* -----2. choose host ----
        Select the host where the Volume to be resized
         */
        log.info("STEP 2: select host");
        resourceProcess.selectHostByVolumeUuid(volumeUuid);

        /* -----3. resize data Volume ----
         */
        log.info("STEP 3: resize volume");
        volumeProcess.resizeSystemVolume(volume, diskSize);

        return ResponseMsgConstants.SUCCESS;
    }

    public String resizeDataVolume(String volumeUuid, Integer diskSize) throws VolumeException, BaseException {
        log.info(String.format("volume info -- volumeUuid: %s, diskSize: %s", volumeUuid, diskSize));
        log.info(String.format("-> invoke DB -- volumeQueryByUuid -- volumeUuid: %s", volumeUuid));
        Volume volume = tableStorage.volumeQueryByUuid(volumeUuid);
        log.info("<- invoke DB -- done");
        /* -----1. check volume and vm status ----
        */
        log.info("STEP 1: check volume and vm status");
        if (!isAttachedVmStopped(volume)) {
            log.error("ERROR: VM is not STOPPED! You can't resize a running VM!");
            return ResponseMsgConstants.FAILED;
        }

        /* -----2. choose host ----
        Select the host where the Volume to be resized
         */
        log.info("STEP 2: select host");
        resourceProcess.selectHostByVolumeUuid(volumeUuid);

        /* -----3. resize data Volume ----
        */
        log.info("STEP 3: resize volume");
        volumeProcess.resizeDataVolume(volume, diskSize);

        return ResponseMsgConstants.SUCCESS;
    }

    public String attachDataVolume(String vmUuid, String volumeUuid) throws BaseException {
        log.info(String.format("volume info -- vmUuid: %s, volumeUuid: %s", vmUuid, volumeUuid));
        /* -----1. choose host ----
        Select the host where the VM to be attached
        */
        log.info("STEP 1: select host");
        resourceProcess.selectHostByVmUuid(vmUuid);

        /* -----2. save volume info (CHECK STATUS) ----
        Save instanceUuid into DB
        */
        log.info("STEP 2: save volume info");
        log.info(String.format("-> invoke DB -- volumeQueryByUuid -- volumeUuid: %s", volumeUuid));
        Volume volume = tableStorage.volumeQueryByUuid(volumeUuid);
        log.info("<- invoke DB -- done");
        if (!volume.getStatus().equals(VolumeStatusEnum.AVAILABLE)) {
            log.error(String.format("ERROR: volume status is not AVAILABLE! (uuid: %s)", volumeUuid));
            return ResponseMsgConstants.FAILED;
        }
        volume.setInstanceUuid(vmUuid);
        volume.setStatus(VolumeStatusEnum.USED);
        log.info("-> invoke DB -- volumeSave");
        tableStorage.volumeSave(volume);
        log.info("<- invoke DB -- done");

        /* -----3. attach data Volume ----
        */
        log.info("STEP 3: attach volume");
        volumeProcess.attachDataVolume(vmUuid, volumeUuid);

        return ResponseMsgConstants.SUCCESS;
    }

    public String detachDataVolume(String vmUuid, String volumeUuid) throws BaseException {
        log.info(String.format("volume info -- vmUuid: %s, volumeUuid: %s", vmUuid, volumeUuid));
        /* -----1. choose host ----
        Select the host where the VM to be detached
        */
        log.info("STEP 1: select host");
        resourceProcess.selectHostByVmUuid(vmUuid);

        /* -----2. CHECK STATUS ---- */
        log.info("STEP 2: check status");
        log.info(String.format("-> invoke DB -- volumeQueryByUuid -- volumeUuid: %s", volumeUuid));
        Volume volume = tableStorage.volumeQueryByUuid(volumeUuid);
        log.info("<- invoke DB -- done");
        if (!volume.getStatus().equals(VolumeStatusEnum.ATTACHED)) {
            log.error(String.format("ERROR: volume status is not ATTACHED! (uuid: %s)", volumeUuid));
            return ResponseMsgConstants.FAILED;
        }

        /* -----3. detach data Volume ----
         */
        log.info("STEP 3: detach volume");
        volumeProcess.detachDataVolume(vmUuid, volumeUuid);

        return ResponseMsgConstants.SUCCESS;
    }

    /**
     * 编辑基本信息
     * 支持字段：名称、描述
     */
    public Volume editVolume(String volumeUuid, String name, String description, Integer diskSize) throws BaseException {
        log.info(String.format("volume info -- volumeUuid: %s, name: %s, description: %s, diskSize: %s",
                volumeUuid, name, description, diskSize));
        // 1. find Volume
        log.info("STEP 1: find volume");
        log.info(String.format("-> invoke DB -- volumeQueryByUuid -- volumeUuid: %s", volumeUuid));
        Volume volume = tableStorage.volumeQueryByUuid(volumeUuid);
        log.info("<- invoke DB -- done");
        if (volume == null) {
            log.error("ERROR: volume is not found!");
            throw new BaseException("ERROR: volume is not found!");
        }

        // 2. check is edit changed
        log.info("STEP 2: check is edit changed");
        boolean editFlag = false;
        if (diskSize != null && diskSize > 0 && !diskSize.equals(volume.getSize())) {
            try {
                if (resizeDataVolume(volumeUuid, diskSize).equals(ResponseMsgConstants.FAILED)) {
                    log.error("ERROR: resize volume failed!");
                    return null;
                }
            } catch (VolumeException e) {
                log.error("ERROR: resize volume failed!");
                return null;
            }
            volume.setSize(diskSize);
            editFlag = true;
        }
        if (name != null && !name.isEmpty() && !name.equals(volume.getName())) {
            volume.setName(name);
            editFlag = true;
        }
        if (description != null && !description.isEmpty() && !description.equals(volume.getDescription())) {
            volume.setDescription(description);
            editFlag = true;
        }

        // 3. save into DB
        log.info("STEP 3: save into DB");
        if (editFlag) {
            log.info(String.format("-> invoke DB -- volumeUpdate -- volumeUuid: %s", volumeUuid));
            volume = tableStorage.volumeUpdate(volume);
            log.info("<- invoke DB -- done");
        }

        return volume;
    }

    /**
     * 获取统计表
     */
    public Map<String, Integer> getStatistics() {
        Map<String, Integer> resMap = new HashMap<>();
        log.info("-> invoke DB -- volumeQueryAllDataVolume");
        List<Volume> dataVolumes = tableStorage.volumeQueryAllDataVolume();
        log.info("<- invoke DB -- done");

        // 1. total
        resMap.put(VolumeConstants.TOTAL, dataVolumes.size());

        // 2. used
        long usedNum = dataVolumes.stream()
                .filter((Volume v) -> v.getStatus().equals(VolumeStatusEnum.ATTACHED))
                .count();
        resMap.put(VolumeConstants.USED, (int) usedNum);

        return resMap;
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
     *    - return: VolumePageResponse
     *
     * 3. FUZZY_QUERY 分页模糊查询
     *    - param: String keyWords, VolumeStatusEnum status, Integer pageNum, Integer pageSize
     *    - return: VolumePageResponse
     *
     * 4. QUERY_BY_XXX 特定查询
     *
     */

    public List<Volume> queryAllDataVolume() {
        // 1. get list from DB
        log.info("-> invoke DB -- volumeQueryAllDataVolume");
        List<Volume> volumeList = tableStorage.volumeQueryAllDataVolume();
        log.info("<- invoke DB -- done");
        // 2. build & return
        return volumeProcess.buildVolumeList(volumeList);
    }

    public PageResponse<Volume> pageQueryAllDataVolume(Integer pageNum, Integer pageSize) {
        log.info(String.format("pageQueryAllDataVolume info -- pageNum: %d, pageSize: %d", pageNum, pageSize));
        // 1. get list from DB
        log.info(String.format("-> invoke DB -- volumePageQueryAll -- pageNum: %d, pageSize: %d", pageNum, pageSize));
        PageResponse<Volume> volumePage = tableStorage.volumePageQueryAll(pageNum, pageSize);
        log.info("<- invoke DB -- done");
        // 2. build & return
        List<Volume> volumeList = volumePage.getContent();
        volumePage.setContent(volumeProcess.buildVolumeList(volumeList));
        return volumePage;
    }

    public PageResponse<Volume> fuzzyQueryDataVolume(String keywords, String status, Integer pageNum, Integer pageSize) {
        log.info(String.format("fuzzyQueryDataVolume info -- keywords: %s, status: %s, pageNum: %d, pageSize: %d",
                keywords, status, pageNum, pageSize));
        // 1. get list from DB
        log.info(String.format("-> invoke DB -- volumeFuzzyQueryDataVolume -- keywords: %s, status: %s, pageNum: %d, pageSize: %d",
                keywords, status, pageNum, pageSize));
        PageResponse<Volume> volumePage = tableStorage.volumeFuzzyQueryDataVolume(keywords, status, pageNum, pageSize);
        log.info("<- invoke DB -- done");
        // 2. build & return
        List<Volume> volumeList = volumePage.getContent();
        volumePage.setContent(volumeProcess.buildVolumeList(volumeList));
        return volumePage;
    }

    private boolean isAttachedVmStopped(Volume volume) {
        log.info(String.format("isAttachedVmStopped info -- volumeUuid: %s", volume.getUuid()));
        if (volume.getInstanceUuid() != null && !volume.getInstanceUuid().isEmpty()) {
            log.info(String.format("-> invoke DB -- vmQueryByUuid -- vmUuid: %s", volume.getInstanceUuid()));
            Vm vm = tableStorage.vmQueryByUuid(volume.getInstanceUuid());
            log.info("<- invoke DB -- done");
            return vm.getStatus().equals(VmStatusEnum.STOPPED);
        }
        return true;
    }
}
