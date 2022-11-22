package kubeiaas.iaascore.service;
import kubeiaas.common.bean.Vm;
import kubeiaas.common.bean.Volume;
import kubeiaas.common.constants.ResponseMsgConstants;
import kubeiaas.common.enums.volume.VolumeStatusEnum;
import kubeiaas.iaascore.dao.TableStorage;
import kubeiaas.iaascore.exception.BaseException;
import kubeiaas.iaascore.exception.VolumeException;
import kubeiaas.iaascore.process.ResourceProcess;
import kubeiaas.iaascore.process.VolumeProcess;
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

    public List<Volume> queryAllDataVolume() {
        List<Volume> volumeList = tableStorage.volumeQueryAllDataVolume();
        for (Volume volume : volumeList) {
            if (volume.getStatus().equals(VolumeStatusEnum.ATTACHED)) {
                Vm vm = tableStorage.vmQueryByUuid(volume.getInstanceUuid());
                volume.setInstanceVm(vm);
            }
        }
        return volumeList;
    }

}
