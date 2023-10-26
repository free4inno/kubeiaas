package kubeiaas.iaascore.scheduler;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import kubeiaas.common.bean.Host;
import kubeiaas.common.bean.Image;
import kubeiaas.common.bean.Vm;
import kubeiaas.common.bean.Volume;
import kubeiaas.common.constants.RequestMappingConstants;
import kubeiaas.common.constants.ResponseMsgConstants;
import kubeiaas.common.enums.image.ImageOSTypeEnum;
import kubeiaas.common.enums.image.ImageStatusEnum;
import kubeiaas.common.enums.volume.VolumeFormatEnum;
import kubeiaas.common.enums.volume.VolumeStatusEnum;
import kubeiaas.common.enums.volume.VolumeUsageEnum;
import kubeiaas.common.utils.PathUtils;
import kubeiaas.common.utils.UuidUtils;
import kubeiaas.iaascore.config.AgentConfig;
import kubeiaas.iaascore.dao.TableStorage;
import kubeiaas.iaascore.dao.feign.VolumeController;
import kubeiaas.iaascore.process.MountProcess;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.net.URI;
import java.net.URISyntaxException;
import java.sql.Timestamp;
import java.util.List;
import java.util.Map;

@Slf4j
@Configuration
public class VolumeScheduler {

    @Resource
    private TableStorage tableStorage;

    @Resource
    private VolumeController volumeController;

    @Resource
    private MountProcess mountProcess;

    public String createSystemVolume(String vmUuid) {
        log.info("-> invoke DB -- vmQueryByUuid");
        Vm vm = tableStorage.vmQueryByUuid(vmUuid);
        log.info("<- invoke DB -- done");
        log.info("-> invoke DB -- imageQueryByUuid");
        Image image = tableStorage.imageQueryByUuid(vm.getImageUuid());
        log.info("<- invoke DB -- done");

        // 统一创建系统盘
        String volumeUuid = this.newSystemVolume(image, vm);
        if (volumeUuid.isEmpty()) {
            log.error("createSystemVolume -- create failed!");
            return volumeUuid;
        }

        // -- 获取子镜像，并 newIsoVolume()
        List<Image> childImages = image.getChildImages();
        if (!CollectionUtils.isEmpty(childImages)) {
            for (Image child : childImages) {
                this.newIsoVolume(child, vm);
            }
        }

        log.debug("createSystemVolume -- done");
        return volumeUuid;
    }

    /**
     * new a volume at host's /sys-volumes
     */
    private String newSystemVolume(Image image, Vm vm) {
        // 检查镜像
        if (!image.getStatus().equals(ImageStatusEnum.AVAILABLE)) {
            log.error(String.format("origin image status is &s", image.getStatus()));
            return "";
        }

        // 创建 Volume 实例
        String volumeUuid = UuidUtils.getRandomUuid();
        Volume newVolume = new Volume();

        newVolume.setUuid(volumeUuid);
        newVolume.setCreateTime(new Timestamp(System.currentTimeMillis()));
        if (vm.getName() == null || vm.getName().isEmpty()) {
            newVolume.setName(volumeUuid);
        } else {
            newVolume.setName(vm.getName());
        }
        newVolume.setDescription(vm.getDescription());
        newVolume.setInstanceUuid(vm.getUuid());
        newVolume.setHostUuid(vm.getHostUuid());
        newVolume.setImageUuid(vm.getImageUuid());
        newVolume.setSize(vm.getDiskSize());
        newVolume.setProviderLocation(PathUtils.genVolumeDirectoryByUuid(volumeUuid, image));

        newVolume.setUsageType(VolumeUsageEnum.SYSTEM);
        newVolume.setFormatType(VolumeFormatEnum.QCOW2);
        newVolume.setStatus(VolumeStatusEnum.CREATING);

        // 计算系统盘镜像额外存储空间
        // （经过先前创建过程处理，vm 中的 size 一定满足创建条件，但是需要计算和 image 原大小之间的 extraSize，调整增大）
        int extraSize = 0;
        if (newVolume.getSize() > image.getVdSize()) {
            extraSize = newVolume.getSize() - image.getVdSize();
        }

        // save into DB
        log.info("-> invoke DB -- volumeSave");
        tableStorage.volumeSave(newVolume);
        log.info("<- invoke DB -- done");

        // call Volume Controller
        String res = volumeController.createSystemVolume(getSelectedUri(vm.getUuid()), image.getDirectory(), newVolume.getProviderLocation(), volumeUuid, extraSize);
        if (res.equals(ResponseMsgConstants.FAILED)) {
            return "";
        }

        return volumeUuid;
    }

    private void newIsoVolume(Image image, Vm vm) {
        String volumeUuid = UuidUtils.getRandomUuid();
        Volume newVolume = new Volume();
        // 1. basic
        newVolume.setUuid(volumeUuid);
        newVolume.setDescription(vm.getDescription());
        newVolume.setCreateTime(new Timestamp(System.currentTimeMillis()));
        if (vm.getName() == null || vm.getName().isEmpty()) {
            newVolume.setName(volumeUuid);
        } else {
            newVolume.setName(vm.getName());
        }
        // 2. info
        newVolume.setInstanceUuid(vm.getUuid());
        newVolume.setHostUuid(vm.getHostUuid());
        newVolume.setImageUuid(image.getUuid());
        // 3. spec
        newVolume.setProviderLocation(image.getDirectory());
        newVolume.setSize(vm.getDiskSize());
        newVolume.setUsageType(VolumeUsageEnum.ISO);
        newVolume.setFormatType(VolumeFormatEnum.ISO);
        newVolume.setStatus(VolumeStatusEnum.CREATING);

        // save into DB
        log.info("-> invoke DB -- volumeSave");
        tableStorage.volumeSave(newVolume);
        log.info("<- invoke DB -- done");
    }

    public boolean createDataVolume(String volumePath, String volumeUuid, int extraSize){
        return volumeController.createDataVolume(getSelectedUri(volumeUuid), volumePath,
                volumeUuid, extraSize).equals(ResponseMsgConstants.SUCCESS);
    }

    public boolean resizeSystemVolume(String volumePath, String volumeUuid, int diskSize){
        if (volumeController.resizeSystemVolume(getSelectedUri(volumeUuid), volumePath,
                volumeUuid, diskSize).equals(ResponseMsgConstants.SUCCESS)){
            return true;
        }else {
            log.info(String.format("resizeSystemVolume ==== error ====  volumeUuid: %s", volumeUuid));
            return false;
        }
    }

    public boolean resizeDataVolume(String volumePath, String volumeUuid, int diskSize){
        if (volumeController.resizeDataVolume(getSelectedUri(volumeUuid), volumePath,
                volumeUuid, diskSize).equals(ResponseMsgConstants.SUCCESS)){
            return true;
        }else {
            log.info(String.format("resizeDataVolume ==== error ====  volumeUuid: %s", volumeUuid));
            return false;
        }
    }

    public boolean deleteSystemVolume(String vmUuid, String volumeUuid, String volumePath){
        //删除Linux主机中的物理硬盘
        if (volumeController.deleteSystemVolume(getSelectedUri(vmUuid), volumeUuid, volumePath)
                .equals(ResponseMsgConstants.SUCCESS)){
            log.info("-> invoke DB -- volumeDelete");
            tableStorage.volumeDelete(volumeUuid);
            log.info("<- invoke DB -- done");
            return true;
        } else {
            log.info(String.format("deleteSysVolume ==== error ====  volumeUuid: %s", volumeUuid));
            return false;
        }
    }

    public boolean deleteIsoVolume(String vmUuid, String volumeUuid){
        log.info("-> invoke DB -- volumeDelete");
        tableStorage.volumeDelete(volumeUuid);
        log.info("<- invoke DB -- done");
        return true;
    }

    public boolean deleteDataVolume(String volumeUuid,String volumePath){
        log.info("-> invoke DB -- volumeQueryByUuid");
        Volume volume = tableStorage.volumeQueryByUuid(volumeUuid);
        log.info("<- invoke DB -- done");
        //删除Linux主机中的物理硬盘
        if (volumeController.deleteDataVolume(getSelectedUri(volumeUuid), volumeUuid, volumePath)
                .equals(ResponseMsgConstants.SUCCESS)){
            log.info("-> invoke DB -- volumeDelete");
            tableStorage.volumeDelete(volumeUuid);
            log.info("<- invoke DB -- done");
            return true;
        } else {
            log.info(String.format("deleteDataVolume ==== error ====  volumeUuid: %s", volumeUuid));
            return false;
        }
    }

    public boolean attachDataVolume(String vmUuid, String volumeUuid){
        //获取已经挂载的volumes
        log.info("-> invoke DB -- volumeQueryAllByInstanceUuid");
        List<Volume> volumeList = tableStorage.volumeQueryAllByInstanceUuid(vmUuid);
        log.info("<- invoke DB -- done");
        log.info("attachVolume ====  volumeList: " + volumeList);
        log.info("-> invoke DB -- vmQueryByUuid");
        Vm vm = tableStorage.vmQueryByUuid(vmUuid);
        log.info("<- invoke DB -- done");
        //设置新volume的盘符等信息
        if (!mountProcess.attachVolumes(volumeList, vm)) {
            return false;
        }
        log.info("-> invoke DB -- volumeQueryByUuid");
        Volume volume = tableStorage.volumeQueryByUuid(volumeUuid);
        log.info("<- invoke DB -- done");
        String vmObjectStr = JSON.toJSONString(vm);
        String volumeObjectStr = JSON.toJSONString(volume);
        if (volumeController.attachDataVolume(getSelectedUri(vm.getUuid()), vmObjectStr, volumeObjectStr)
                .equals(ResponseMsgConstants.SUCCESS)){
            return true;
        } else {
            log.info(String.format("attachDataVolume ==== error ====  volumeUuid: %s", volumeUuid));
            return false;
        }
    }

    public boolean detachDataVolume(String vmUuid, String volumeUuid){
        log.info("-> invoke DB -- vmQueryByUuid");
        Vm vm = tableStorage.vmQueryByUuid(vmUuid);
        log.info("<- invoke DB -- done");
        log.info("-> invoke DB -- volumeQueryByUuid");
        Volume volume = tableStorage.volumeQueryByUuid(volumeUuid);
        log.info("<- invoke DB -- done");
        String vmObjectStr = JSON.toJSONString(vm);
        String volumeObjectStr = JSON.toJSONString(volume);
        if (volumeController.detachDataVolume(getSelectedUri(vmUuid), vmObjectStr, volumeObjectStr)
                .equals(ResponseMsgConstants.SUCCESS)){
            return true;
        } else {
            log.info(String.format("detachDataVolume ==== error ====  volumeUuid: %s", volumeUuid));
            return false;
        }
    }

    /**
     * 获取云硬盘存储统计数据
     */
    public Map<String, String> getDataVolStorageInfo() {
        String jsonObjectStr = volumeController.getDataVolStorage(getSelectedUri(RequestMappingConstants.DATA_VOLUME_STORAGE));
        return JSON.parseObject(jsonObjectStr, new TypeReference<Map<String, String>>(){});
    }

    /**
     * 获取云镜像存储统计数据
     */
    public Map<String, String> getImgVolStorageInfo() {
        String jsonObjectStr = volumeController.getImgVolStorage(getSelectedUri(RequestMappingConstants.IMG_VOLUME_STORAGE));
        return JSON.parseObject(jsonObjectStr, new TypeReference<Map<String, String>>(){});
    }

    // -----------------------------------------------------------------------------------------------------------------

    private URI getSelectedUri(String vmUuid) {
        try {
            return new URI(AgentConfig.getSelectedUri(vmUuid));
        } catch (URISyntaxException e) {
            e.printStackTrace();
            log.error(String.format("ERROR: build URI failed!"));
            return null;
        }
    }

}
