package kubeiaas.iaasagent.service;

import kubeiaas.common.bean.Vm;
import kubeiaas.common.bean.Volume;
import kubeiaas.common.constants.bean.VmConstants;
import kubeiaas.common.constants.bean.VolumeConstants;
import kubeiaas.common.enums.vm.VmStatusEnum;
import kubeiaas.common.enums.volume.VolumeStatusEnum;
import kubeiaas.common.utils.*;
import kubeiaas.iaasagent.config.LibvirtConfig;
import kubeiaas.iaasagent.config.VolumeConfig;
import kubeiaas.iaasagent.config.XmlConfig;
import kubeiaas.iaasagent.dao.TableStorage;
import lombok.extern.slf4j.Slf4j;
import org.libvirt.Connect;
import org.libvirt.Domain;
import org.libvirt.LibvirtException;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
public class VolumeService {

    @Resource
    private TableStorage tableStorage;

    @Resource
    private XmlConfig xmlConfig;

    public boolean createSystemVolume(String imagePath, String volumePath, String volumeUuid, int extraSize) {

        Volume volume = tableStorage.volumeQueryByUuid(volumeUuid);

        // 1. ------------ getFullPath ------------
        String imageImageFullPath = PathUtils.genFullPath(imagePath);
        String volumeImageFullPath = PathUtils.genFullPath(volumePath);

        // 2. ------------ copy file ------------
        if (!FileUtils.createDirIfNotExist(volumeImageFullPath)) {
            log.error("Create new image file path Error!!!");
            volume.setStatus(VolumeStatusEnum.ERROR_PREPARE);
            tableStorage.volumeSave(volume);
            return false;
        }
        new Thread(() -> {
            try {
                FileUtils.copy(imageImageFullPath, volumeImageFullPath);
                volume.setStatus(VolumeStatusEnum.AVAILABLE);
                // resize volume
                if (extraSize > 0) {
                    String command = String.format(VolumeConfig.RESIZE_VOLUME_WITH_BLOCK_SIZE_CMD, volumeImageFullPath, extraSize);
                    if (!ShellUtils.run(command)) {
                        log.error("resize system volume size failure, command is: " + command);
                    }
                }
            } catch (IOException e) {
                log.error("CopySystemVolume Error!!!");
                volume.setStatus(VolumeStatusEnum.ERROR_PREPARE);
                e.printStackTrace();
            } finally {
                // save into DB
                tableStorage.volumeSave(volume);
            }
        }).start();
        return true;
    }

    public boolean createDataVolume(String volumePath, String volumeUuid, int extraSize){

        Volume volume = tableStorage.volumeQueryByUuid(volumeUuid);
        log.info("test:" +volume);

        // 1. ------------ getFullPath ------------
        String volumeFullPath = PathUtils.genFullPath(volumePath);

        //在 /srv/nfs4/ 检查是否有重名文件 volumes/z/p/xx.img ，如果重名，有可能会覆盖
        if (FileUtils.exists(volumeFullPath)) {
            log.error("outPutImage "
                    + volumeFullPath
                    + " exist. In order to prevent cover, please change its uuid!");
            volume.setStatus(VolumeStatusEnum.ERROR_PREPARE);
            tableStorage.volumeSave(volume);
            return false;
        }

        if (!FileUtils.createDirIfNotExist(volumeFullPath)) {
            log.error("create outPut Volume "
                    + volumeFullPath
                    + " Error. ");
            volume.setStatus(VolumeStatusEnum.ERROR_PREPARE);
            tableStorage.volumeSave(volume);
            return false;
        }

        if (extraSize <= 0) {
            log.warn("blockSize:parameter invalid," + extraSize + " use default disk size");
            extraSize = VmConstants.DEFAULT_DISK_SIZE;
        }

        String format = VolumeConfig.CREATE_VOLUME_DISK_CMD;
        //String format = "qemu-img create -f %s %s %sG";
        //String command = "qemu-img create -f qcow2 /srv/nfs4/volumes/z/p/xx.img 4.5G
        String command = String.format(format,
                VolumeConstants.DEFAULT_DISK_TYPE,
                volumeFullPath,
                extraSize);
        log.info("cmd" + command);
        String res = ShellUtils.getCmd(command);
        log.info("Create Volume result: " + res);
        log.info(" volume: " + volume);
        setVolumeStatus(volumeUuid, VolumeStatusEnum.AVAILABLE);
        log.info("createDataVolume ==== end ====");
        return true;
    }

    public boolean deleteVolume(String volumeUuid, String volumePath) {
        log.info("deleteVolume ==== start ==== volumePath: " + volumePath);
        Volume volume = tableStorage.volumeQueryByUuid(volumeUuid);

        // 1. check path not empty
        if (FileUtils.isEmptyString(volumePath)) {
            log.error("Lack of volumePath Params :" + volumePath);
            return false;
        }

        // 2. check file exist
        volumePath = PathUtils.genFullPath(volumePath);
        if (!FileUtils.exists(volumePath)) {
            if (volume.getStatus().equals(VolumeStatusEnum.CREATING)
                    || volume.getStatus().equals(VolumeStatusEnum.ERROR_PREPARE)
                    || volume.getStatus().equals(VolumeStatusEnum.ERROR)) {
                log.info("no file need to delete");
            } else {
                log.error("file `" + volumePath + "` is not exists");
            }
            return true;
        }

        // 3. delete
        log.info("volumePath" + volumePath);
        boolean result = new File(volumePath).delete();
        if (!result) {
            log.error("Delete volume Error!!! " + volumePath);
            return false;
        }

        log.info("deleteVolume ==== end ====");
        return true;
    }

    public Boolean attachVolume(Vm vm, Volume volume) {
        log.info("attachVolume ---- start ----");
        log.info("vm: " + vm.toString() + " volume: " + volume.toString());
        String volumeXml = xmlConfig.getVolumeDevice(volume);
        log.info("attach volume info: " + volumeXml);
        String vmUuid = vm.getUuid();
        String volumeUuid = volume.getUuid();
        try {
            Connect virtCon = LibvirtConfig.getVirtCon();
            Domain domain = virtCon.domainLookupByUUIDString(vmUuid);
            try {
                /**
                 * Attach a virtual device to a domain, using the flags parameter to control how the device is attached.
                 * - 0000: VIR_DOMAIN_AFFECT_CURRENT specifies that the device allocation is made based on current domain state.
                 * - 0001: VIR_DOMAIN_AFFECT_LIVE specifies that the device shall be allocated to the active domain instance only and is not added to the persisted domain configuration.
                 * - 0010: VIR_DOMAIN_AFFECT_CONFIG specifies that the device shall be allocated to the persisted domain configuration only. Note that the target hypervisor must return an error if unable to satisfy flags.
                 * - 0100: FORCE
                 * Use | to combine those configs, we got 3 as (VIR_DOMAIN_AFFECT_LIVE | VIR_DOMAIN_AFFECT_CONFIG)
                 */
                int virtFlag = vm.getStatus().equals(VmStatusEnum.ACTIVE) ? (0b0001 | 0b0010) : (0b0010);
                domain.attachDeviceFlags(volumeXml, virtFlag);
            } catch (Exception e) {
                volume.setInstanceUuid("");
                volume.setMountPoint("");
                volume.setBus("");
                tableStorage.volumeSave(volume);
                setVolumeStatus(volumeUuid, VolumeStatusEnum.AVAILABLE);
                e.printStackTrace();
                return false;
            }
        } catch (LibvirtException e) {
            setVolumeStatus(volumeUuid, VolumeStatusEnum.ERROR);
            e.printStackTrace();
            return false;
        }
        setVolumeStatus(volumeUuid, VolumeStatusEnum.ATTACHED);
        log.info("attachVolume ---- end ----");
        return true;
    }

    public Boolean detachVolume(Vm vm, Volume volume) {
        log.info("detachVolume ---- start ----");
        log.info("vm: " + vm.toString() + " volume: " + volume.toString());
        String volumeXml = xmlConfig.getVolumeDevice(volume);
        log.info("detach volume info: " + volumeXml);
        String instanceUuid = vm.getUuid();
        String volumeUuid = volume.getUuid();
        try {
            Connect virtCon = LibvirtConfig.getVirtCon();
            Domain domain = virtCon.domainLookupByUUIDString(instanceUuid);
            try {
                int virtFlag = vm.getStatus().equals(VmStatusEnum.ACTIVE) ? (0b0001 | 0b0010) : (0b0010);
                domain.detachDeviceFlags(volumeXml, virtFlag);
            } catch (Exception e) {
                setVolumeStatus(volumeUuid, VolumeStatusEnum.ATTACHED);
                e.printStackTrace();
                return false;
            }
        } catch (LibvirtException e) {
            setVolumeStatus(volumeUuid, VolumeStatusEnum.ERROR);
            e.printStackTrace();
            return false;
        }
        setVolumeStatus(volumeUuid, VolumeStatusEnum.AVAILABLE);
        log.info("detachVolume ---- end ----");
        return true;
    }

    public boolean volumeToImage(String volumePath, String imagePath) {
        log.info("volumeToImage ==== start ====  volumePath: " + volumePath + " imagePath: " + imagePath);

        // 1. ------------ getFullPath ------------
        String volumeImageFullPath = PathUtils.genFullPath(volumePath);
        String imageImageFullPath = PathUtils.genFullPath(imagePath);

        // 2. ------------ copy file ------------
        if (!FileUtils.createDirIfNotExist(imageImageFullPath)) {
            log.error("Create new image file path Error!!!");
            return false;
        }
        new Thread(() -> {
            try {
                FileUtils.copy(volumeImageFullPath, imageImageFullPath);       //拷贝镜像
            } catch (IOException e) {
                log.error("File copy Error!!!");
                e.printStackTrace();
            }
        }).start();

        return true;
    }

    public Map<String, String> getVolStorage(String directory) {
        String command;
        Map<String, String> resMap = new HashMap<>();

        command = String.format(VolumeConfig.QUERY_NFS_MOUNT_FS, directory);
        String mountFS = ShellUtils.getCmd(command);
        command = String.format(VolumeConfig.QUERY_VOLUME_TOTAL, directory);
        String total = ShellUtils.getCmd(command);
        command = String.format(VolumeConfig.QUERY_VOLUME_USED, directory);
        String used = ShellUtils.getCmd(command);

        resMap.put(VolumeConstants.MNT_DIR, directory);
        resMap.put(VolumeConstants.MNT_FS, mountFS);
        resMap.put(VolumeConstants.TOTAL, total);
        resMap.put(VolumeConstants.USED, used);

        return resMap;
    }

    // ---------------------------------------------------------------------------------------------------------------

    private void setVolumeStatus(String volumeUuid, VolumeStatusEnum status){
        Volume volume = tableStorage.volumeQueryByUuid(volumeUuid);
        volume.setStatus(status);
        tableStorage.volumeSave(volume);
    }
}
