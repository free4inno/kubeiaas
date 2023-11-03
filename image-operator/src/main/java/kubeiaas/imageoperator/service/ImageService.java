package kubeiaas.imageoperator.service;


import com.alibaba.fastjson.JSON;
import kubeiaas.common.bean.Image;
import kubeiaas.imageoperator.config.ImageConfig;
import kubeiaas.imageoperator.response.PageResponse;
import kubeiaas.imageoperator.utils.ImageUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
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

    public boolean imageDelete(String uuid) {
        // 1. check (uuid, which is just yaml file name)
        if (!getYamlList().contains(uuid)) {
            log.error("ERROR: image not found. uuid: {}", uuid);
            return false;
        }
        String yamlPath = baseDir + uuid;
        Image image = ImageUtils.getImageFromYaml(yamlPath, 0);
        // 2. delete file
        String filePath = baseDir + ImageUtils.getFileNameFromPath(image.getDirectory(), true);
        if (!ImageUtils.deleteFile(filePath)) {
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
}
