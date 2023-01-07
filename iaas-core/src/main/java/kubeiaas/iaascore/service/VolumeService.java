package kubeiaas.iaascore.service;

import kubeiaas.common.bean.Volume;
import kubeiaas.common.constants.ResponseMsgConstants;
import kubeiaas.common.constants.bean.VolumeConstants;
import kubeiaas.common.enums.volume.VolumeStatusEnum;
import kubeiaas.iaascore.dao.TableStorage;
import kubeiaas.iaascore.exception.BaseException;
import kubeiaas.iaascore.exception.VolumeException;
import kubeiaas.iaascore.process.ResourceProcess;
import kubeiaas.iaascore.process.VolumeProcess;
import kubeiaas.iaascore.response.PageResponse;
import lombok.extern.slf4j.Slf4j;
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
            String hostUUid) throws VolumeException {
        /* ---- 1. pre create Volume ----
        Generate and Set basic info of volume.
        （预处理：设置基础参数，保存云硬盘信息）
         */
        Volume newVolume= volumeProcess.preCreateVolume(name, description, hostUUid, diskSize);

        /* ---- 2. check host ----
        Use Resource Operator to allocate Host
        （资源调度：分配宿主机）
         */
        newVolume = resourceProcess.createVolumeOperate(newVolume);

        /* ---- 3. create volume ----
        （创建虚拟机数据盘）
         */
        volumeProcess.createDataVolume(newVolume);

        return newVolume;
    }

    public String deleteDataVolume(String volumeUuid) throws BaseException {
         /* ---- 1. judge volume state ----
        （判断数据盘状态）
         */
        Volume volume = tableStorage.volumeQueryByUuid(volumeUuid);
        if (volume.getStatus().equals(VolumeStatusEnum.ATTACHED)) {
            return ResponseMsgConstants.FAILED;
        }

        /* -----2. choose host ----
        Select the host where the Volume to be deleted
        */
        resourceProcess.selectHostByVolumeUuid(volumeUuid);

         /* -----3. delete VM ----
        Delete the VM and then delete other information
        */
        volumeProcess.deleteDataVolume(volumeUuid);

        return ResponseMsgConstants.SUCCESS;
    }

    public String attachDataVolume(String vmUuid, String volumeUuid) throws BaseException {
        /* -----1. choose host ----
        Select the host where the VM to be attached
        */
        resourceProcess.selectHostByVmUuid(vmUuid);

        /* -----2. save volume info (CHECK STATUS) ----
        Save instanceUuid into DB
        */
        Volume volume = tableStorage.volumeQueryByUuid(volumeUuid);
        if (!volume.getStatus().equals(VolumeStatusEnum.AVAILABLE)) {
            log.error("ERR: DataVolume not AVAILABLE! (uuid: " + vmUuid + ")");
            return ResponseMsgConstants.FAILED;
        }
        volume.setInstanceUuid(vmUuid);
        volume.setStatus(VolumeStatusEnum.USED);
        tableStorage.volumeSave(volume);

        /* -----3. attach data Volume ----
        */
        volumeProcess.attachDataVolume(vmUuid, volumeUuid);

        return ResponseMsgConstants.SUCCESS;
    }

    public String detachDataVolume(String vmUuid, String volumeUuid) throws BaseException {
        /* -----1. choose host ----
        Select the host where the VM to be detached
        */
        resourceProcess.selectHostByVmUuid(vmUuid);

        /* -----2. CHECK STATUS ---- */
        Volume volume = tableStorage.volumeQueryByUuid(volumeUuid);
        if (!volume.getStatus().equals(VolumeStatusEnum.ATTACHED)) {
            log.error("ERR: DataVolume not ATTACHED! (uuid: " + vmUuid + ")");
            return ResponseMsgConstants.FAILED;
        }

        /* -----2. detach data Volume ----
         */
        volumeProcess.detachDataVolume(vmUuid, volumeUuid);

        return ResponseMsgConstants.SUCCESS;
    }

    /**
     * 编辑基本信息
     * 支持字段：名称、描述
     */
    public Volume editVolume(String volumeUuid, String name, String description) throws BaseException {
        // 1. find Volume
        Volume volume = tableStorage.volumeQueryByUuid(volumeUuid);
        if (volume == null) {
            throw new BaseException("ERROR: volume is not found!");
        }

        // 2. check is edit changed
        boolean editFlag = false;
        if (name != null && !name.isEmpty() && !name.equals(volume.getName())) {
            volume.setName(name);
            editFlag = true;
        }
        if (description != null && !description.isEmpty() && !description.equals(volume.getDescription())) {
            volume.setDescription(description);
            editFlag = true;
        }

        // 3. save into DB
        if (editFlag) {
            volume = tableStorage.volumeUpdate(volume);
        }

        return volume;
    }

    /**
     * 获取统计表
     */
    public Map<String, Integer> getStatistics() {
        Map<String, Integer> resMap = new HashMap<>();
        List<Volume> dataVolumes = tableStorage.volumeQueryAllDataVolume();

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
        List<Volume> volumeList = tableStorage.volumeQueryAllDataVolume();
        // 2. build & return
        return volumeProcess.buildVolumeList(volumeList);
    }

    public PageResponse<Volume> pageQueryAllDataVolume(Integer pageNum, Integer pageSize) {
        // 1. get list from DB
        PageResponse<Volume> volumePage = tableStorage.volumePageQueryAll(pageNum, pageSize);
        // 2. build & return
        List<Volume> volumeList = volumePage.getContent();
        volumePage.setContent(volumeProcess.buildVolumeList(volumeList));
        return volumePage;
    }

    public PageResponse<Volume> fuzzyQueryDataVolume(String keywords, String status, Integer pageNum, Integer pageSize) {
        // 1. get list from DB
        PageResponse<Volume> volumePage = tableStorage.volumeFuzzyQueryDataVolume(keywords, status, pageNum, pageSize);
        // 2. build & return
        List<Volume> volumeList = volumePage.getContent();
        volumePage.setContent(volumeProcess.buildVolumeList(volumeList));
        return volumePage;
    }

}
