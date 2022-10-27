package kubeiaas.imageoperator.service;


import com.alibaba.fastjson.JSON;
import kubeiaas.common.bean.Image;
import kubeiaas.common.enums.image.*;
import kubeiaas.imageoperator.ImageOperatorApplication;
import kubeiaas.imageoperator.config.ImageConfig;
import kubeiaas.imageoperator.utils.ImageUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.yaml.snakeyaml.Yaml;

import javax.annotation.Resource;
import java.io.BufferedReader;
import java.io.FileReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

@Slf4j
@Service
public class ImageService {

    @Resource
    private ImageConfig imageConfig;

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
            imageList.add(image);
        }
        log.info("imageList: " + JSON.toJSONString(imageList));

        return imageList;
    }

    private List<String> getYamlList() {
        List<String> yamlList = new ArrayList<>();
        // test: "C:\\Users\\74723\\Desktop\\kubeiaas-镜像管理"
        // prod: ImageConfig.STORAGE_BASE_DIR + ImageConfig.STORAGE_IMAGE_PATH
        try (Stream<Path> paths = Files.walk(Paths.get(ImageConfig.STORAGE_BASE_DIR + ImageConfig.STORAGE_IMAGE_PATH), 1)) {
            paths.map(Path::toString).filter(f -> f.endsWith(".yaml"))
                    .forEach(yamlList::add);
        } catch (Exception e) {
            e.printStackTrace();
        }
        log.info("yamlList: " + JSON.toJSONString(yamlList));
        return yamlList;
    }
}
