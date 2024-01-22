package kubeiaas.imageoperator.service;


import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import kubeiaas.common.bean.Image;
import kubeiaas.common.constants.HufuServiceConstants;
import kubeiaas.common.constants.bean.VolumeConstants;
import kubeiaas.common.enums.image.ImageUploadingStatusEnum;
import kubeiaas.common.utils.FileUtils;
import kubeiaas.common.utils.UuidUtils;
import kubeiaas.common.utils.ZhiUtils;
import kubeiaas.imageoperator.config.ImageConfig;
import kubeiaas.imageoperator.process.ImageProcess;
import kubeiaas.imageoperator.response.PageResponse;
import kubeiaas.imageoperator.utils.ImageUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

@Slf4j
@Service
public class ImageService {

    /**
     * - env: test
     *   filesWalkPath: "C:\\Users\\74723\\Desktop\\kubeiaas-镜像管理\\"
     * - env: prod
     *   filesWalkPath: ImageConfig.STORAGE_BASE_DIR + ImageConfig.STORAGE_IMAGE_PATH
     * - env: prod-container
     *   filesWalkPath: ImageConfig.CONTAINER_STORAGE_PATH
     */
    private static final String baseDir = ImageConfig.CONTAINER_STORAGE_PATH;

    @Resource
    private final ImageProcess imageProcess;

    public ImageService(ImageProcess imageProcess) {
        this.imageProcess = imageProcess;
    }

    public Image queryByUuid(String uuid) {
        log.info("image queryByUuid -- uuid: {}", uuid);
        // 1. get all yaml files under image storage path
        List<String> yamlList = getYamlList();

        // 2. solve each yaml files into Image Object
        for (int id = 0; id < yamlList.size(); id++) {
            // 3. deal by uuid (which is just yaml file name)
            if (yamlList.get(id).endsWith(uuid)) {
                Image image = ImageUtils.getImageFromYaml(yamlList.get(id), id);
                log.info("image found -- name: {}", image.getName());
                return image;
            }
        }
        log.warn("WARN: Image Not Found -- uuid: {}", uuid);
        return null;
    }

    public String queryRawByUuid(String uuid) {
        // uuid (which is just yaml file name)
        String imageRaw = ImageUtils.getRawFromYaml(baseDir + uuid);
        log.info("query rawFileString by uuid -- uuid: {}, raw: {}", uuid, imageRaw);
        return imageRaw;
    }

    public List<Image> queryAll() {
        // 1. get all yaml files under image storage path
        List<String> yamlList = getYamlList();

        // 2. solve each yaml files into Image Object
        List<Image> imageList = new ArrayList<>();
        for (int id = 0; id < yamlList.size(); id++) {
            Image image = ImageUtils.getImageFromYaml(yamlList.get(id), id);
            imageList.add(image);
        }
        log.debug("imageList: {}", JSON.toJSONString(imageList));

        return imageList;
    }

    public PageResponse<Image> pageQueryAll(Integer pageNum, Integer pageSize) {
        log.info("pageQueryAll -- pageNum: {}, pageSize: {}", pageNum, pageSize);
        // 1. get all yaml files under image storage path
        List<String> yamlList = getYamlList();

        // 2. parse page response & return
        return parseImagePage(yamlList, pageNum, pageSize);
    }

    public PageResponse<Image> fuzzyQuery(String keywords, Integer pageNum, Integer pageSize) {
        log.info("pageQueryAll -- keywords:{}, pageNum: {}, pageSize: {}", keywords, pageNum, pageSize);
        // 1. get all yaml files under image storage path
        List<String> yamlList = getYamlList();

        // 2. fuzzy check raw content
        List<String> fuzzyList = new ArrayList<>();
        for (String yamlPath : yamlList) {
            String raw = ImageUtils.getRawFromYaml(yamlPath);
            if (raw.contains(keywords)) {
                fuzzyList.add(yamlPath);
            }
        }

        // 3. parse page response & return
        return parseImagePage(fuzzyList, pageNum, pageSize);
    }

    public boolean imageCreateYaml(Image image) {
        try {
            ImageUtils.createImageYaml(image);
            return true;
        } catch (IOException e) {
            log.error("ERROR: imageCreateYaml failed -- IOException", e);
            return false;
        }
    }

    public boolean imageSaveYaml(String uuid, String content) {
        // uuid (which is just yaml file name)
        String filePath = baseDir + uuid;
        try {
            ImageUtils.saveImageYaml(filePath, content);
            return true;
        } catch (IOException e) {
            log.error("ERROR: imageSaveYaml failed -- IOException", e);
            return false;
        }
    }

    public boolean imageUpload(int id) {
        // 1. get attached iso and yaml file url
        Optional<JSONArray> attachmentsOpt = ZhiUtils.getAttachments(id);
        if (!attachmentsOpt.isPresent()) {
            log.error(String.format("ERROR: get attachments failed. id: %d", id));
            return false;
        }
        JSONArray attachments = attachmentsOpt.get();
        String isoUrl = null, yamlUrl = null;
        for (int i = 0; i < attachments.size(); i++) {
            JSONObject attachment = attachments.getJSONObject(i);
            String name = attachment.getString("name");
            String url = String.format("%s%s&name=%s", HufuServiceConstants.TEMPLATE_CENTER_URL, attachment.getString("url"), name);
            if (name.endsWith(".iso")) {
                isoUrl = url;
            } else if (name.endsWith(".yaml") || name.endsWith(".yml")) {
                yamlUrl = url;
            }
        }
        if (isoUrl == null) {
            log.error("ERROR: lack of iso file");
            return false;
        }
        if (yamlUrl == null) {
            log.error("ERROR: lack of yaml file");
            return false;
        }

        // 2. Generate a random uuid as the name of the yaml and iso
        String uuid = UuidUtils.getRandomUuid();
        while(isImageExist(uuid)) {
            uuid = UuidUtils.getRandomUuid();
        }

        // 3. download iso and yaml file
        // 3.1 download yaml file
        try {
            URL url = new URL(yamlUrl);
            Optional<File> tmpYamlFileOpt = FileUtils.fetchFile(url, ImageConfig.DESCRIPTION_DOWNLOADING_PATH, uuid + ".yaml.tmp");
            if (!tmpYamlFileOpt.isPresent()) {
                log.error("ERROR: download yaml file failed");
                return false;
            }
            File tmpYamlFile = tmpYamlFileOpt.get();
            String yamlPath = baseDir + uuid + ".yaml";
            if (!ImageUtils.processIsoImage(tmpYamlFile.getAbsolutePath(), yamlPath, uuid + ".iso", uuid)) {
                File yamlFile = new File(yamlPath);
                if (yamlFile.delete()) {
                    log.info("delete yaml file success");
                } else {
                    log.error("ERROR: delete yaml file failed");
                }
                log.error("ERROR: process yaml file failed");
                return false;
            }
            if(tmpYamlFile.delete()) {
                log.info("delete tmp yaml file success");
            } else {
                log.error("delete tmp yaml file failed");
            }
        } catch (IOException e) {
            log.error(String.format("ERROR: processing yaml file failed, error: %s", e.getMessage()));
            return false;
        }

        // 3.2 download iso file
//        try {
//            URL url = new URL(isoUrl);
//            Optional<File> tmpIsoFileOpt = FileUtils.fetchFile(url, ImageConfig.IMAGE_DOWNLOADING_PATH, uuid + ".iso");
//            if (!tmpIsoFileOpt.isPresent()) {
//                log.error("ERROR: download iso file failed");
//                return false;
//            }
//            File tmpIsoFile = tmpIsoFileOpt.get();
//            File isoFile = new File(baseDir + uuid + ".iso");
//            FileUtils.move(tmpIsoFile, isoFile);
//            if (!isoFile.exists()) {
//                log.error("ERROR: move iso file failed");
//                return false;
//            }
//        } catch (IOException e) {
//            log.error("ERROR: imageUpload failed -- IOException", e);
//            return false;
//        }
        try {
            URL url = new URL(isoUrl);
            imageProcess.asyncDownloadImage(url, ImageConfig.IMAGE_DOWNLOADING_PATH, uuid + ".iso");
        } catch (Exception e) {
            log.error("ERROR: error image url format");
            return false;
        }

        return true;
    }

    public boolean imageDelete(String uuid) {
        // 1. check (uuid, which is just yaml file name)
        String yamlPath = baseDir + uuid;
        if (!getYamlList().contains(yamlPath)) {
            log.error("ERROR: image not found. uuid: {}", uuid);
            return false;
        }
        Image image = ImageUtils.getImageFromYaml(yamlPath, 0);
        // 2. delete file
        String filePath = baseDir + ImageUtils.getFileNameFromPath(image.getDirectory(), true);
        File imageFile = new File(filePath);
        if (imageFile.exists() && !ImageUtils.deleteFile(filePath)) {
            log.error("ERROR: <image file> delete error! file: {}", filePath);
            return false;
        }
        // 3. delete child
        List<Image> childImages = image.getChildImages();
        for (Image child : childImages) {
            filePath = baseDir + ImageUtils.getFileNameFromPath(child.getDirectory(), true);
            if (!ImageUtils.deleteFile(filePath)) {
                log.error("ERROR: <child image file> delete error! file: {}", filePath);
                return false;
            }
        }
        // 4. delete yaml
        if (!ImageUtils.deleteFile(yamlPath)) {
            log.error("ERROR: <image yaml file> delete error! file: {}", filePath);
            return false;
        }
        log.info("success -- imageDelete");
        return true;
    }

    public Optional<ImageUploadingStatusEnum> queryUploadStatus(String uuid) {
        uuid = uuid.replaceAll("\\.yaml$", "")
                   .replaceAll("\\.yml$", "");
        File yamlFile = new File(baseDir + uuid + ".yaml");
        if (!yamlFile.exists()) {
            log.error("ERROR: yaml file not exist. uuid: {}", uuid);
            return Optional.empty();
        }
        Image image = ImageUtils.getImageFromYaml(yamlFile.getAbsolutePath(), 0);
        return Optional.of(image.getUploadingStatus());
    }

    public Integer getTotalNum() {
        return getYamlList().size();
    }

    // -----------------------------------------------------------------------------------------------------------------

    private PageResponse<Image> parseImagePage(List<String> fileList, Integer pageNum, Integer pageSize) {
        // 1. calculate basic number
        Integer totalElements = fileList.size();
        int totalPages = (totalElements % pageSize == 0) ? (totalElements / pageSize) : (totalElements / pageSize) + 1;

        // 2. get content
        List<Image> imageList = new ArrayList<>();
        for (int id = 0; id < fileList.size(); id++) {
            if (id >= (pageNum - 1) * pageSize && id < pageNum * pageSize) {
                Image image = ImageUtils.getImageFromYaml(fileList.get(id), id);
                imageList.add(image);
            }
        }

        // 3. return
        return new PageResponse<>(imageList, totalPages, totalElements.longValue());
    }

    private List<String> getYamlList() {
        List<String> yamlList = new ArrayList<>();
        try (Stream<Path> paths = Files.walk(Paths.get(baseDir), 1)) {
            paths.map(Path::toString).filter(f -> f.endsWith(".yaml") || f.endsWith(".yml")).forEach(yamlList::add);
        } catch (Exception e) {
            log.error("ERROR: failed to get yamlFileList.", e);
        }
        log.debug("yamlList: " + JSON.toJSONString(yamlList));
        return yamlList;
    }

    private boolean isImageExist(String uuid) {
        String isoPath = VolumeConstants.DEFAULT_NFS_SRV_PATH + VolumeConstants.IMAGE_PATH + uuid + ".iso";
        if (FileUtils.exists(isoPath)) {
            return true;
        }
        String yamlPath = VolumeConstants.DEFAULT_NFS_SRV_PATH + VolumeConstants.IMAGE_PATH + uuid + ".yaml";
        if (FileUtils.exists(yamlPath)) {
            return true;
        }
        yamlPath = VolumeConstants.DEFAULT_NFS_SRV_PATH + VolumeConstants.IMAGE_PATH + uuid + ".yml";
        return FileUtils.exists(yamlPath);
    }
}
