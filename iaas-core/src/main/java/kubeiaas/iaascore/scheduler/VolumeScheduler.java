package kubeiaas.iaascore.scheduler;

import kubeiaas.common.bean.Host;
import kubeiaas.common.bean.Image;
import kubeiaas.common.bean.Vm;
import kubeiaas.common.bean.Volume;
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
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;

import javax.annotation.Resource;
import java.net.URI;
import java.net.URISyntaxException;
import java.sql.Timestamp;
import java.util.List;

@Slf4j
@Configuration
public class VolumeScheduler {

    @Resource
    private TableStorage tableStorage;

    @Resource
    private VolumeController volumeController;

    public String createSystemVolume(String vmUuid) {
        Vm vm = tableStorage.vmQueryByUuid(vmUuid);
        Image image = tableStorage.imageQueryByUuid(vm.getImageUuid());

        // 统一创建系统盘
        String volumeUuid = this.newSystemVolume(image, vm);
        if (volumeUuid.isEmpty()) {
            log.error("createSystemVolume -- create failed!");
            return volumeUuid;
        }

        // 特判 windows 镜像
        if (image.getOsType().equals(ImageOSTypeEnum.WINDOWS)) {
            log.info("createSystemVolume -- WINDOWS");
            // -- 获取子镜像，并 newIsoVolume()
            List<Image> childImages = image.getChildImages();
            for (Image child : childImages) {
                newIsoVolume(child, vm);
            }
        } else {
            log.info("createSystemVolume -- not WINDOWS");
        }

        log.info("createSystemVolume -- done");
        return volumeUuid;
    }

    /**
     * new a volume at host's /sys-volumes
     */
    private String newSystemVolume(Image image, Vm vm) {
        // 检查镜像
        if (!image.getStatus().equals(ImageStatusEnum.AVAILABLE)) {
            log.error("origin image status is" + image.getStatus());
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
        if (newVolume.getSize() > image.getMinDisk()) {
            extraSize = newVolume.getSize() - image.getMinDisk();
        }

        // save into DB
        tableStorage.volumeSave(newVolume);

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
        tableStorage.volumeSave(newVolume);
    }

    public boolean deleteSystemVolume(String vmUuid, String volumeUuid, String volumePath){
        log.info("deleteSysVolume ==== start ====  volumeUuid: " + volumeUuid);
        //删除Linux主机中的物理硬盘
        if (volumeController.deleteSystemVolume(getSelectedUri(vmUuid), volumeUuid, volumePath)
                .equals(ResponseMsgConstants.SUCCESS)){
            tableStorage.volumeDelete(volumeUuid);
            log.info("deleteSysVolume ==== end ====  volumeUuid: " + volumeUuid);
            return true;
        } else {
            log.info("deleteSysVolume ==== error ====  volumeUuid: " + volumeUuid);
            return false;
        }
    }

    public boolean deleteIsoVolume(String vmUuid, String volumeUuid){
        log.info("deleteIsoVolume ==== start ====  volumeUuid: " + volumeUuid);
        tableStorage.volumeDelete(volumeUuid);
        log.info("deleteIsoVolume ==== end ====  volumeUuid: " + volumeUuid);
        return true;
    }


    private URI getSelectedUri(String vmUuid) {
        try {
            return new URI(AgentConfig.getSelectedUri(vmUuid));
        } catch (URISyntaxException e) {
            e.printStackTrace();
            log.error("build URI failed!");
            return null;
        }
    }

}
