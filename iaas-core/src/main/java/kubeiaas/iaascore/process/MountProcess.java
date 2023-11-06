package kubeiaas.iaascore.process;

import kubeiaas.common.bean.*;
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
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Slf4j
@Service
public class MountProcess {

    @Resource
    private TableStorage tableStorage;

    @Resource
    private MountPointUtils mountPointUtils;

    // 此处逻辑挂载新硬盘时已经不再使用，由于创建vm流程尚不理解，暂时保留作为创建vm时使用。
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
                    // 获取第一个未挂载的挂载点
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

    private static Set<Character> findMissingElements(List<String> list, char start, char end) {
        Set<Character> thirdChars = new HashSet<>();
        for (String s : list) {
            if (s.length() >= 3) {
                thirdChars.add(s.charAt(2));
            }
        }

        Set<Character> rangeSet = IntStream.rangeClosed(start, end).mapToObj(i -> (char) i).collect(Collectors.toSet());

        rangeSet.removeAll(thirdChars);
        return rangeSet;
    }

    public List<String> getAllocatedMountPoints(Vm vm) {
        List<String> usedMountPoints = new ArrayList<>();

        // 1. Get all volumes of this vm
        List<Volume> volumeList = tableStorage.volumeQueryAllByInstanceUuid(vm.getUuid());
        for (Volume volume : volumeList) {
            if (volume.getStatus().equals(VolumeStatusEnum.ATTACHED)
                    && volume.getMountPoint() != null
                    && !volume.getMountPoint().isEmpty()) {
                usedMountPoints.add(volume.getMountPoint());
            }
        }
        // 2. Get all iso mount points of this vm
        usedMountPoints.addAll(tableStorage.queryAllMountPointsByVmUuid(vm.getUuid()));

        return usedMountPoints;
    }

    public String getMountPointPrefix(ImageOSTypeEnum osType, String device) {
        if (osType.equals(ImageOSTypeEnum.WINDOWS)) {
            return VolumeConstants.WIN_PREFIX;
        } else {
            if (device.equals(VolumeConstants.VOLUME_DEVICE_DISK)) {
                return VolumeConstants.DEV_PREFIX;
            } else if (device.equals(VolumeConstants.VOLUME_DEVICE_CDROM)) {
                return VolumeConstants.ISO_PREFIX;
            } else {
                // default
                return VolumeConstants.DEV_PREFIX;
            }
        }
    }

    public Optional<String> getScsiFirstAvailableMountPoint(Vm vm) {
        List<String> allocatedMountPoints = tableStorage.queryAllMountPointsByVmUuid(vm.getUuid());
        Image image = tableStorage.imageQueryByUuid(vm.getImageUuid());
        String prefix = getMountPointPrefix(image.getOsType(), VolumeConstants.VOLUME_DEVICE_CDROM);
        Set<Character> availableMountPoints = findMissingElements(allocatedMountPoints, '0', '1');
        if (availableMountPoints.isEmpty()) {
            return Optional.empty();
        } else {
            return Optional.of(prefix + availableMountPoints.iterator().next());
        }
    }

    public Optional<String> getVirtioFirstAvailableMountPoint(Vm vm) {
        List<String> allocatedMountPoints = getAllocatedMountPoints(vm);
        Image image = tableStorage.imageQueryByUuid(vm.getImageUuid());
        String prefix = getMountPointPrefix(image.getOsType(), VolumeConstants.VOLUME_DEVICE_DISK);
        Set<Character> availableMountPoints = findMissingElements(allocatedMountPoints, 'a', 'z');
        if (availableMountPoints.isEmpty()) {
            return Optional.empty();
        } else {
            return Optional.of(prefix + availableMountPoints.iterator().next());
        }
    }

    public Optional<String> getVirtioFirstAvailableMountPoint(String vmUuid) {
        Vm vm = tableStorage.vmQueryByUuid(vmUuid);
        if (vm == null) {
            return Optional.empty();
        }
        return getVirtioFirstAvailableMountPoint(vm);
    }

    public List<String> getVirtioAlLAvailableMountPoints(Vm vm) {
        List<String> allocatedMountPoints = getAllocatedMountPoints(vm);
        Image image = tableStorage.imageQueryByUuid(vm.getImageUuid());
        String prefix = getMountPointPrefix(image.getOsType(), VolumeConstants.VOLUME_DEVICE_DISK);
        Set<Character> availableMountPoints = findMissingElements(allocatedMountPoints, 'a', 'z');
        if (availableMountPoints.isEmpty()) {
            return new ArrayList<>();
        } else {
            return availableMountPoints.stream().map(c -> prefix + c).collect(Collectors.toList());
        }
    }

    public Optional<Mount> processMountForVirtio(Vm vm, Iso iso, Boolean readonly) {
        Mount mount = new Mount();
        mount.setVmUuid(vm.getUuid());
        mount.setIsoUuid(iso.getUuid());
        mount.setType(VolumeConstants.STORAGE_TYPE);
        mount.setDevice(VolumeConstants.VOLUME_DEVICE_DISK);
        mount.setDriverType(VolumeConstants.VOLUME_DRIVER_TYPE_RAW);
        mount.setSource(iso.getAbsolutePath());
        Optional<String> optMountPoint = getVirtioFirstAvailableMountPoint(vm);
        if (optMountPoint.isPresent()) {
            mount.setMountPoint(optMountPoint.get());
        } else {
            log.error("processMountForVirtio: get mount point failed, vmUuid: {}, isoUuid: {}", vm.getUuid(), iso.getUuid());
            return Optional.empty();
        }
        mount.setBus(VolumeConstants.VOLUME_BUS_VIRTIO);
        mount.setReadonly(readonly);

        return Optional.of(mount);
    }

    public Optional<Mount> processMountForScsi(Vm vm, Iso iso, Boolean readonly) {
        Mount mount = new Mount();
        mount.setVmUuid(vm.getUuid());
        mount.setIsoUuid(iso.getUuid());
        mount.setType(VolumeConstants.STORAGE_TYPE);
        mount.setDevice(VolumeConstants.VOLUME_DEVICE_CDROM);
        mount.setDriverType(VolumeConstants.VOLUME_DRIVER_TYPE_RAW);
        mount.setSource(iso.getAbsolutePath());
        // TODO: 2023/9/25 临时解决方案，分配最后一个挂载点，然后libvirt自动分配。不影响virtio方式挂载，待修改
        List<String> mountPoints = getVirtioAlLAvailableMountPoints(vm);
        if (!mountPoints.isEmpty()) {
            mount.setMountPoint(mountPoints.get(mountPoints.size() - 1));
        } else {
            log.error("processMountForScsi: get mount point failed, vmUuid: {}, isoUuid: {}", vm.getUuid(), iso.getUuid());
            return Optional.empty();
        }
        mount.setBus(VolumeConstants.VOLUME_BUS_IDE);
        mount.setReadonly(readonly);

        return Optional.of(mount);
    }
}
