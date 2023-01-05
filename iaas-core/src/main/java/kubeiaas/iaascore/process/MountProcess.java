package kubeiaas.iaascore.process;

import kubeiaas.common.bean.Image;
import kubeiaas.common.bean.Vm;
import kubeiaas.common.bean.Volume;
import kubeiaas.common.constants.bean.VolumeConstants;
import kubeiaas.common.enums.image.ImageOSTypeEnum;
import kubeiaas.common.enums.volume.VolumeFormatEnum;
import kubeiaas.common.enums.volume.VolumeStatusEnum;
import kubeiaas.common.enums.volume.VolumeUsageEnum;
import kubeiaas.iaascore.dao.TableStorage;
import kubeiaas.iaascore.utils.MountPointUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

@Slf4j
@Service
public class MountProcess {

    @Resource
    private TableStorage tableStorage;

    @Resource
    private MountPointUtils mountPointUtils;

    public boolean attachVolumes(List<Volume> volumeList, Vm vm) {
        log.info("attaching volumes -- start");
        // 为各个 volume 分配盘符
        allocDevMountPoint(volumeList, vm);
        for (Volume volume : volumeList) {
            if (volume.getMountPoint().isEmpty()) {
                log.error("attaching volumes -- mount point alloc error!");
                return false;
            }
            if (volume.getFormatType().equals(VolumeFormatEnum.ISO)) {
                volume.setBus(VolumeConstants.VOLUME_BUS_IDE);
            } else {
                volume.setBus(VolumeConstants.VOLUME_BUS_VIRTIO);
            }
            volume.setInstanceUuid(vm.getUuid());
            volume.setStatus(VolumeStatusEnum.ATTACHED);
            // save into DB
            tableStorage.volumeSave(volume);
        }
        log.info("attaching volumes -- end");
        return true;
    }

    // 有可能一次挂载多个硬盘，所以传递至 MountPointUtil 里的 List<VmVolume> 一定是这一层的，而不是从数据库中获取的
    // 因为需要及时更新 挂载点
    private void allocDevMountPoint(List<Volume> volumeList, Vm vm) {
        Image systemImage = tableStorage.imageQueryByUuid(vm.getImageUuid());
        for (Volume volume : volumeList) {
            if (volume.getMountPoint() == null || volume.getMountPoint().isEmpty()) {
                // 逐个分配挂载点
                String mountPoint;
                if (volume.getUsageType().equals(VolumeUsageEnum.SYSTEM)) {
                    // 1. 挂载系统盘
                    if (systemImage.getOsType().equals(ImageOSTypeEnum.WINDOWS)) {
                        mountPoint = VolumeConstants.WIN_PREFIX + "a";
                    } else {
                        mountPoint = VolumeConstants.DEV_PREFIX + "a";
                    }
                } else {
                    // 2. 挂载数据盘
                    mountPoint = mountPointUtils.getMountPoint(volumeList);
                    // （在 attachVolumes 中处理返回 empty）
                    if (systemImage.getOsType().equals(ImageOSTypeEnum.WINDOWS)) {
                        mountPoint = VolumeConstants.WIN_PREFIX + mountPoint;
                    } else {
                        mountPoint = VolumeConstants.DEV_PREFIX + mountPoint;
                    }
                }
                volume.setMountPoint(mountPoint);
            }
            log.info("volume: " + volume.getUuid() + "'s mount point: " + volume.getMountPoint());
        }
    }
}
