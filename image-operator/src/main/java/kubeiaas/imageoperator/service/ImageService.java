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

    public Image queryByUuid(String uuid) {
        // 1. get all yaml files under image storage path
        List<String> yamlList = getYamlList();

        // 2. solve each yaml files into Image Object
        for (int id = 0; id < yamlList.size(); id++) {
            // 3. deal by uuid
            Image image = ImageUtils.getImageFromYaml(yamlList.get(id), id);
            if (image != null && image.getUuid().equals(uuid)) {
                log.info("Image Found! (name: " + image.getName() + ")");
                return image;
            }
        }
        log.warn("Image Not Found (uuid: " + uuid + ")");
        return null;
    }

    public List<Image> queryAll() {
        // 1. get all yaml files under image storage path
        List<String> yamlList = getYamlList();

        // 2. solve each yaml files into Image Object
        List<Image> imageList = new ArrayList<>();
        for (int id = 0; id < yamlList.size(); id++) {
            Image image = ImageUtils.getImageFromYaml(yamlList.get(id), id);
            if (image != null) {
                imageList.add(image);
            }
        }
        log.info("imageList: " + JSON.toJSONString(imageList));

        return imageList;
    }

    public PageResponse<Image> pageQueryAll(Integer pageNum, Integer pageSize) {
        // 1. get all yaml files under image storage path
        List<String> yamlList = getYamlList();

        // 2. calculate basic number
        Integer totalElements = yamlList.size();
        int totalPages = (totalElements % pageSize == 0) ? (totalElements / pageSize) : (totalElements / pageSize) + 1;

        // 3. get content
        List<Image> imageList = new ArrayList<>();
        for (int id = 0; id < yamlList.size(); id++) {
            if (id >= (pageNum - 1) * pageSize && id < pageNum * pageSize) {
                Image image = ImageUtils.getImageFromYaml(yamlList.get(id), id);
                if (image != null) {
                    imageList.add(image);
                } else {
                    imageList.add(new Image(null, "UNKNOWN", "Error parsing yaml file: " + yamlList.get(id)));
                }
            }
        }

        return new PageResponse<>(imageList, totalPages, totalElements.longValue());
    }

    public boolean imageCreateYaml(Image image) {
        try {
            image.setSize(0f);
            ImageUtils.createImageYaml(image);
            return true;
        } catch (IOException e) {
            log.error("imageCreateYaml -- create imageYaml Error!");
            e.printStackTrace();
            return false;
        }
    }

    private List<String> getYamlList() {
        List<String> yamlList = new ArrayList<>();
        /*
         - env: test
           filesWalkPath: "C:\\Users\\74723\\Desktop\\kubeiaas-镜像管理"
         - env: prod
           filesWalkPath: ImageConfig.STORAGE_BASE_DIR + ImageConfig.STORAGE_IMAGE_PATH
         - env: prod-container
           filesWalkPath: ImageConfig.CONTAINER_STORAGE_PATH
         */
        try (Stream<Path> paths = Files.walk(Paths.get(ImageConfig.CONTAINER_STORAGE_PATH), 1)) {
            paths.map(Path::toString).filter(f -> f.endsWith(".yaml") || f.endsWith(".yml")).forEach(yamlList::add);
        } catch (Exception e) {
            e.printStackTrace();
        }
        log.info("yamlList: " + JSON.toJSONString(yamlList));
        return yamlList;
    }
}
