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
        log.info(String.format("createSystemVolume info -- imagePath: %s, volumePath: %s, volumeUuid: %s, extraSize: %d",
                imagePath, volumePath, volumeUuid, extraSize));

        log.info("STEP 1: get volume and paths of volume and image");
        log.info(String.format("-> invoke DB -- volumeQueryByUuid. volumeUuid: %s", volumeUuid));
        Volume volume = tableStorage.volumeQueryByUuid(volumeUuid);
        log.info("<- invoke DB -- done");

        // 1. ------------ getFullPath ------------
        String imageImageFullPath = PathUtils.genFullPath(imagePath);
        String volumeImageFullPath = PathUtils.genFullPath(volumePath);

        // 2. ------------ copy file ------------
        log.info("STEP 2: copy image file and create system volume");
        if (!FileUtils.createDirIfNotExist(volumeImageFullPath)) {
            log.error("ERROR: Create new image file path Error!");
            volume.setStatus(VolumeStatusEnum.ERROR_PREPARE);
            log.info(String.format("-> invoke DB -- volumeSave. volumeUuid: %s", volumeUuid));
            tableStorage.volumeSave(volume);
            log.info("<- invoke DB -- done");
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
                        log.error(String.format("ERROR: resize system volume size failure, command is: %s", command));
                    }
                }
            } catch (IOException e) {
                log.error("ERROR: CopySystemVolume Error!", e);
                volume.setStatus(VolumeStatusEnum.ERROR_PREPARE);
            } finally {
                // save into DB
                log.info(String.format("-> invoke DB -- volumeSave. volumeUuid: %s", volumeUuid));
                tableStorage.volumeSave(volume);
                log.info("<- invoke DB -- done");
            }
        }).start();
        return true;
    }

    public boolean createDataVolume(String volumePath, String volumeUuid, int volumeSize){
        log.info(String.format("createDataVolume info -- volumePath: %s, volumeUuid: %s, extraSize: %d",
                volumePath, volumeUuid, volumeSize));

        log.info("STEP 1: get volume and check file path");
        log.info("-> invoke DB -- volumeQueryByUuid");
        Volume volume = tableStorage.volumeQueryByUuid(volumeUuid);
        log.info("<- invoke DB -- done");

        // 1. ------------ getFullPath ------------
        String volumeFullPath = PathUtils.genFullPath(volumePath);

        //在 /srv/nfs4/ 检查是否有重名文件 volumes/z/p/xx.img ，如果重名，有可能会覆盖
        if (FileUtils.exists(volumeFullPath)) {
            log.error(String.format("ERROR: volume file %s already exists", volumeFullPath));
            volume.setStatus(VolumeStatusEnum.ERROR_PREPARE);
            log.info("-> invoke DB -- volumeSave");
            tableStorage.volumeSave(volume);
            log.info("<- invoke DB -- done");
            return false;
        }

        if (!FileUtils.createDirIfNotExist(volumeFullPath)) {
            log.error(String.format("ERROR: Create new image file path Error! path is: %s", volumeFullPath));
            volume.setStatus(VolumeStatusEnum.ERROR_PREPARE);
            log.info("-> invoke DB -- volumeSave");
            tableStorage.volumeSave(volume);
            log.info("<- invoke DB -- done");
            return false;
        }

        if (volumeSize <= 0) {
            log.warn(String.format("WARN: extraSize is %d, set to default size %d", volumeSize, VmConstants.DEFAULT_DISK_SIZE));
            volumeSize = VmConstants.DEFAULT_DISK_SIZE;
        }

        log.info("STEP 2: create data volume");
        String format = VolumeConfig.CREATE_VOLUME_DISK_CMD;
        // String format = "qemu-img create -f %s %s %sG";
        // String command = "qemu-img create -f qcow2 /srv/nfs4/volumes/z/p/xx.img 4.5G
        String command = String.format(format,
                VolumeConstants.DEFAULT_DISK_TYPE,
                volumeFullPath,
                volumeSize);
        String res = ShellUtils.getCmd(command);
        setVolumeStatus(volumeUuid, VolumeStatusEnum.AVAILABLE);
        return true;
    }

    public boolean resizeVolume(String volumePath, String volumeUuid, int newSize) {
        log.info(String.format("resizeVolume info -- volumePath: %s, volumeUuid: %s, newSize: %d",
                volumePath, volumeUuid, newSize));
        log.info("STEP 1: get volume object and its path");
        Volume volume = tableStorage.volumeQueryByUuid(volumeUuid);
        if (newSize <= 0) {
            return false;
        }
        int sizeDelta = newSize - volume.getSize();
        String volumeFullPath = PathUtils.genFullPath(volumePath);

        if (FileUtils.isEmptyString(volumePath)) {
            log.error(String.format("ERROR: Lack of volumePath Params : %s", volumePath));
            return false;
        }
        if (!FileUtils.exists(volumeFullPath)) {
            log.error(String.format("ERROR: file %s is not exists", volumeFullPath));
            volume.setStatus(VolumeStatusEnum.DELETED);
            log.info("-> invoke DB -- volumeSave");
            tableStorage.volumeSave(volume);
            log.info("<- invoke DB -- done");
            return false;
        }

        // 3. Resize volume
        log.info("STEP 2: resize volume");
        String command = String.format(
                VolumeConfig.RESIZE_VOLUME_CMD,
                sizeDelta > 0 ? "": VolumeConstants.SHRINK_OPTION,
                volumeFullPath,
                newSize
        );
        if (!ShellUtils.run(command)) {
            log.error(String.format("ERROR: resize volume failure, command is: %s", command));
            return false;
        }

        // 4. Verify volume size
        log.info("STEP 3: verify volume size");
        String volumeSize = ShellUtils.getCmd(String.format(VolumeConfig.QUERY_VOLUME_VIRTUAL_SIZE, volumeFullPath));
        // Since getCmd returns not null value, we can use equals directly
        if (!volumeSize.equals(String.valueOf(newSize))) {
            log.error("ERROR: volume size not changed, resize volume failed");
            return false;
        }

        // 5. Update volume size
        log.info("STEP 4: update volume size in DB");
        volume.setSize(newSize);
        log.info("-> invoke DB -- volumeSave");
        tableStorage.volumeSave(volume);
        log.info("<- invoke DB -- done");
        return true;
    }

    public boolean deleteVolume(String volumeUuid, String volumePath) {
        log.info(String.format("deleteVolume info -- volumeUuid: %s, volumePath: %s", volumeUuid, volumePath));
        log.info("STEP 1: get volume object and its path");

        log.info("-> invoke DB -- volumeQueryByUuid");
        Volume volume = tableStorage.volumeQueryByUuid(volumeUuid);
        log.info("<- invoke DB -- done");

        if (FileUtils.isEmptyString(volumePath)) {
            log.error(String.format("ERROR: Lack of volumePath Params : %s", volumePath));
            return false;
        }
        volumePath = PathUtils.genFullPath(volumePath);
        if (!FileUtils.exists(volumePath)) {
            if (volume.getStatus().equals(VolumeStatusEnum.CREATING)
                    || volume.getStatus().equals(VolumeStatusEnum.ERROR_PREPARE)
                    || volume.getStatus().equals(VolumeStatusEnum.ERROR)) {
                log.debug("no file need to delete");
            } else {
                log.error(String.format("ERROR: file %s is not exists", volumePath));
            }
            return true;
        }

        // 3. delete
        log.info("STEP 2: delete volume");
        boolean result = new File(volumePath).delete();
        if (!result) {
            log.error(String.format("ERROR: delete volume failure, path is: %s", volumePath));
            return false;
        }
        return true;
    }

    public Boolean attachVolume(Vm vm, Volume volume) {
        log.info(String.format("attachVolume info -- vm: %s, volume: %s", vm.toString(), volume.toString()));
        String volumeXml = xmlConfig.getVolumeDevice(volume);
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

                log.info("-> invoke DB -- volumeSave");
                tableStorage.volumeSave(volume);
                log.info("<- invoke DB -- done");
                setVolumeStatus(volumeUuid, VolumeStatusEnum.AVAILABLE);
                log.error(String.format("ERROR: attach volume failure, volumeUuid is: %s", volumeUuid), e);
                return false;
            }
        } catch (LibvirtException e) {
            setVolumeStatus(volumeUuid, VolumeStatusEnum.ERROR);
            log.error("ERROR: attach volume failed", e);
            return false;
        }
        setVolumeStatus(volumeUuid, VolumeStatusEnum.ATTACHED);
        return true;
    }

    public Boolean detachVolume(Vm vm, Volume volume) {
        log.info(String.format("detachVolume info -- vm: %s, volume: %s", vm.toString(), volume.toString()));
        String volumeXml = xmlConfig.getVolumeDevice(volume);
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
                log.error("ERROR: detach volume failure", e);
                return false;
            }
        } catch (LibvirtException e) {
            setVolumeStatus(volumeUuid, VolumeStatusEnum.ERROR);
            log.error("ERROR: detach volume failed", e);
            return false;
        }
        setVolumeStatus(volumeUuid, VolumeStatusEnum.AVAILABLE);
        return true;
    }

    public boolean volumeToImage(String volumePath, String imagePath, int extraSize) {
        log.info(String.format("volumeToImage info -- volumePath: %s, imagePath: %s, extraSize: %d",
                volumePath, imagePath, extraSize));

        // 1. ------------ getFullPath ------------
        String volumeImageFullPath = PathUtils.genFullPath(volumePath);
        String imageImageFullPath = PathUtils.genFullPath(imagePath);

        // 2. ------------ copy file ------------
        if (!FileUtils.createDirIfNotExist(imageImageFullPath)) {
            log.error("ERROR: Create new image file path Error!");
            return false;
        }
        new Thread(() -> {
            try {
                // copy image
                FileUtils.copy(volumeImageFullPath, imageImageFullPath);
                // adjust size
                if (extraSize > 0) {
                    String cmd = String.format(VolumeConfig.RESIZE_VOLUME_WITH_BLOCK_SIZE_CMD,
                            imageImageFullPath,
                            extraSize);
                    String res = ShellUtils.getCmd(cmd);
                    log.debug(String.format("resize result: %s", res));
                }
            } catch (IOException e) {
                log.error("ERROR: file copy Error!", e);
            }
        }).start();

        return true;
    }

    public Map<String, String> getVolStorage(String directory) {
        log.info(String.format("getVolStorage info -- directory: %s", directory));
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
        log.info(String.format("setVolumeStatus info -- volumeUuid: %s, status: %s", volumeUuid, status));
        log.info("-> invoke DB -- volumeQueryByUuid");
        Volume volume = tableStorage.volumeQueryByUuid(volumeUuid);
        log.info("<- invoke DB -- done");
        volume.setStatus(status);
        log.info("-> invoke DB -- volumeSave");
        tableStorage.volumeSave(volume);
        log.info("<- invoke DB -- done");
    }
}
