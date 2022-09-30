package kubeiaas.iaasagent.service;

import kubeiaas.common.bean.Volume;
import kubeiaas.common.enums.volume.VolumeStatusEnum;
import kubeiaas.common.utils.FileUtils;
import kubeiaas.common.utils.MacUtils;
import kubeiaas.common.utils.PathUtils;
import kubeiaas.common.utils.ShellUtils;
import kubeiaas.iaasagent.config.DhcpConfig;
import kubeiaas.iaasagent.config.VolumeConfig;
import kubeiaas.iaasagent.dao.TableStorage;
import lombok.extern.slf4j.Slf4j;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
public class VolumeService {

    @Resource
    private TableStorage tableStorage;

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

    public boolean deleteVolume(String volumePath) {
        log.info("deleteVolume ==== start ==== volumePath: " + volumePath);
        if (FileUtils.isEmptyString(volumePath)) {
            log.error("Lack of volumePath Params :" + volumePath);
        }
        volumePath = PathUtils.genFullPath(volumePath);
        if (!FileUtils.exists(volumePath)) {
            log.error("Delete volume Error!!! " + volumePath + "is not exists");
            return false;
        }
        log.info("volumePath" + volumePath);
        boolean result = new File(volumePath).delete();
        if (!result) {
            log.error("Delete volume Error!!! " + volumePath);
            return false;
        }
        log.info("deleteVolume ==== end ====");
        return true;
    }
}
