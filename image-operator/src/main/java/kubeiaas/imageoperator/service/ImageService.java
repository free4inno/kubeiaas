package kubeiaas.imageoperator.service;


import com.alibaba.fastjson.JSON;
import kubeiaas.common.bean.Image;
import kubeiaas.imageoperator.ImageOperatorApplication;
import kubeiaas.imageoperator.config.ImageConfig;
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
        Image image = new Image();
        return image;
    }

    public List<Image> queryAll() {
        List<Image> imageList = new ArrayList<>();
        List<String> yamlList = new ArrayList<>();
        try (Stream<Path> paths = Files.walk(Paths.get(ImageConfig.STORAGE_BASE_DIR + ImageConfig.STORAGE_IMAGE_PATH), 1)) {
            paths.map(Path::toString).filter(f -> f.endsWith(".yaml"))
                    .forEach(yamlList::add);
        } catch (Exception e) {
            e.printStackTrace();
        }
        log.info("yamlList: " + JSON.toJSONString(yamlList));

        Map<String, Object> objectMap = new HashMap<>();
        try {
            BufferedReader br = new BufferedReader(new FileReader(yamlList.get(0)));
            Yaml yaml = new Yaml();
            objectMap = yaml.load(br);
        } catch (Exception e) {
            e.printStackTrace();
        }

        log.info("yamlContent: " + JSON.toJSONString(objectMap));
        return imageList;
    }

}
