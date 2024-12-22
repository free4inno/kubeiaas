package kubeiaas.imageoperator.process;

import kubeiaas.common.utils.FileUtils;
import kubeiaas.imageoperator.config.ImageConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.File;
import java.net.URL;
import java.util.Optional;

@Slf4j
@Service
public class ImageProcess {

    @Async
    public void asyncDownloadImage(URL url, String tmpDirPath, String imageName) {
        log.info("Downloading image {} from {}", imageName, url);

        try {
            Optional<File> tmpImageFileOpt = FileUtils.fetchFile(url, tmpDirPath, imageName);
            if (tmpImageFileOpt.isPresent()) {
                log.info("Image {} downloaded from {}", imageName, url);
            } else {
                log.error("Error downloading image {} from {}", imageName, url);
                return;
            }
            File tmpImageFile = tmpImageFileOpt.get();
            File imageFile = new File(ImageConfig.CONTAINER_STORAGE_PATH + imageName);
            log.info("Image {} moved to {}", imageName, imageFile.getAbsolutePath());
            FileUtils.move(tmpImageFile, imageFile);
            if (imageFile.exists()) {
                log.info("Image {} saved to {}", imageName, imageFile.getAbsolutePath());
            } else {
                log.error("Error saving image {} to {}", imageName, imageFile.getAbsolutePath());
                return;
            }
            if (tmpImageFile.exists()) {
                log.warn("Image {} deleted from {}", imageName, tmpImageFile.getAbsolutePath());
                tmpImageFile.delete();
            }
        } catch (Exception e) {
            log.error("Error downloading image {} from {}", imageName, url, e);
        }
    }
}
