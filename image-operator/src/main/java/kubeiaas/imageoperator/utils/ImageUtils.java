package kubeiaas.imageoperator.utils;

import com.alibaba.fastjson.JSON;
import kubeiaas.common.bean.Image;
import kubeiaas.common.enums.image.*;
import kubeiaas.imageoperator.config.ImageConfig;
import lombok.extern.slf4j.Slf4j;
import org.yaml.snakeyaml.Yaml;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.Map;

@Slf4j
public class ImageUtils {

    public static Image getImageFromYaml(String filePath, Integer id) {
        Map<String, Object> objectMap;
        try {
            BufferedReader br = new BufferedReader(new FileReader(filePath));
            Yaml yaml = new Yaml();
            objectMap = yaml.load(br);
            log.info("yamlContent: " + JSON.toJSONString(objectMap));

            Map<String, Object> imageMap = (Map<String, Object>) objectMap.get("image");
            String name = (String) imageMap.get("name");
            String uuid = (String) imageMap.get("uuid");
            String description = (String) imageMap.get("description");
            String directory = ImageConfig.HOST_STORAGE_IMAGE_PATH + imageMap.get("filename");

            Map<String, Object> spec = (Map<String, Object>) imageMap.get("spec");
            Float size = ((Double) spec.get("size")).floatValue();

            ImageFormatEnum format = getEnumFromString(ImageFormatEnum.class, (String) spec.get("format"));
            ImageOSTypeEnum osType = getEnumFromString(ImageOSTypeEnum.class, (String) spec.get("os_type"));
            ImageOSModeEnum osMode = getEnumFromString(ImageOSModeEnum.class, (String) spec.get("os_mode"));
            ImageOSArchEnum osArch = getEnumFromString(ImageOSArchEnum.class, (String) spec.get("os_arch"));

            Map<String, Object> config = (Map<String, Object>) imageMap.get("config");
            Integer minDisk = (Integer) config.get("min_disk");
            Integer minMem = (Integer) config.get("min_mem");

            Image image = new Image(id + 1, uuid, name, description, directory, format, size,
                    ImageStatusEnum.AVAILABLE, minMem, minDisk, osType, osArch, osMode, null);

            log.info("image: " + JSON.toJSONString(image));
            return image;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * A common method for all enums since they can't have another base class
     * @param <T> Enum type
     * @param c enum type. All enums must be all caps.
     * @param string case insensitive
     * @return corresponding enum, or null
     */
    public static <T extends Enum<T>> T getEnumFromString(Class<T> c, String string) {
        if( c != null && string != null ) {
            try {
                return Enum.valueOf(c, string.trim().toUpperCase());
            } catch (IllegalArgumentException ignored) {
            }
        }
        return null;
    }
}
