package kubeiaas.iaascore.service;
import kubeiaas.common.bean.Volume;
import kubeiaas.common.constants.ResponseMsgConstants;
import kubeiaas.common.enums.vm.VmStatusEnum;
import kubeiaas.common.enums.volume.VolumeStatusEnum;
import kubeiaas.iaascore.dao.TableStorage;
import kubeiaas.iaascore.exception.BaseException;
import kubeiaas.iaascore.exception.VolumeException;
import kubeiaas.iaascore.process.ResourceProcess;
import kubeiaas.iaascore.process.VolumeProcess;
import kubeiaas.iaascore.response.VolumePageResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

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

        /* -----2. save volume info ----
        Save instanceUuid into DB
        */
        Volume volume = tableStorage.volumeQueryByUuid(volumeUuid);
        volume.setInstanceUuid(vmUuid);
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

        /* -----2. detach data Volume ----
         */
        volumeProcess.detachDataVolume(vmUuid, volumeUuid);

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
     *    - return: VolumePageResponse
     *
     * 3. QUERY_FUZZY 模糊查询
     *    - param: String keyWords, VmStatusEnum status
     *    - return: List
     *
     * 4. PAGE_QUERY_FUZZY 分页模糊查询
     *    - param: String keyWords, VolumeStatusEnum status, Integer pageNum, Integer pageSize
     *    - return: VolumePageResponse
     *
     * 5. QUERY_BY_XXX 特定查询
     *
     */

    public List<Volume> queryAllDataVolume() {
        // 1. get list from DB
        List<Volume> volumeList = tableStorage.volumeQueryAllDataVolume();
        // 2. build & return
        return volumeProcess.buildVolumeList(volumeList);
    }

    public List<Volume> FuzzyQueryDataVolume(String keyWords, String status){
        // 1. get list from DB
        // String volumeStatus = status.toString();
        log.info(keyWords + "--------------------" + status);
        List<Volume> volumeList = tableStorage.fuzzyQueryDataVolume(keyWords, status);
        // 2. build & return
        return volumeProcess.buildVolumeList(volumeList);
    }

    public VolumePageResponse pageFuzzyQueryDataVolume(String keyWords, String status, Integer pageNum, Integer pageSize) {
        // 1. get list from DB
        VolumePageResponse volumePage = tableStorage.pageFuzzyQueryDataVolume(keyWords, status, pageNum, pageSize);
        // 2. build & return
        List<Volume> volumeList = volumePage.getContent();
        volumePage.setContent(volumeProcess.buildVolumeList(volumeList));
        return volumePage;
    }


    public VolumePageResponse pageQueryAllDataVolume(Integer pageNum, Integer pageSize) {
        // 1. get list from DB
        VolumePageResponse volumePage = tableStorage.volumePageQueryAll(pageNum, pageSize);
        // 2. build & return
        List<Volume> volumeList = volumePage.getContent();
        volumePage.setContent(volumeProcess.buildVolumeList(volumeList));
        return volumePage;
    }

}
