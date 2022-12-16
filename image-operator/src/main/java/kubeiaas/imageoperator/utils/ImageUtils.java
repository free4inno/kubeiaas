package kubeiaas.imageoperator.utils;

import com.alibaba.fastjson.JSON;
import kubeiaas.common.bean.Image;
import kubeiaas.common.constants.bean.ImageConstants;
import kubeiaas.common.constants.bean.VolumeConstants;
import kubeiaas.common.enums.image.*;
import kubeiaas.common.utils.EnumUtils;
import kubeiaas.imageoperator.config.ImageConfig;
import lombok.extern.slf4j.Slf4j;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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
            // Float size = ((Double) spec.get("size")).floatValue();
            Integer vdSize = (Integer) spec.get("vd_size");

            ImageFormatEnum format = EnumUtils.getEnumFromString(ImageFormatEnum.class, (String) spec.get("format"));
            ImageOSTypeEnum osType = EnumUtils.getEnumFromString(ImageOSTypeEnum.class, (String) spec.get("os_type"));
            ImageOSModeEnum osMode = EnumUtils.getEnumFromString(ImageOSModeEnum.class, (String) spec.get("os_mode"));
            ImageOSArchEnum osArch = EnumUtils.getEnumFromString(ImageOSArchEnum.class, (String) spec.get("os_arch"));

            /*
            Map<String, Object> config = (Map<String, Object>) imageMap.get("config");
            Integer minDisk = (Integer) config.get("min_disk");
            Integer minMem = (Integer) config.get("min_mem");
             */

            List<Image> childImageList = new ArrayList<>();
            List<Object> childImages = (List<Object>) objectMap.get("child_images");
            if (childImages != null) {
                for (Object child : childImages) {
                    Map<String, String> childImageMap = (Map<String, String>) child;
                    String childUuid = childImageMap.get("uuid");
                    String childFilename = childImageMap.get("filename");
                    childImageList.add(new Image(childUuid, ImageConfig.HOST_STORAGE_IMAGE_PATH + childFilename));
                }
            }

            Image image = new Image(id + 1, uuid, name, description, directory, format, null, vdSize,
                    ImageStatusEnum.AVAILABLE, null, null, osType, osArch, osMode, null, childImageList);

            log.info("image: " + JSON.toJSONString(image));
            return image;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static void createImageYaml(Image image) throws IOException {
        // 设置yml格式
        DumperOptions dumperOptions = new DumperOptions();
        dumperOptions.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);

        // 创建FileWriter
        String uuid = image.getUuid();
        String filepath = ImageConfig.CONTAINER_STORAGE_PATH + uuid + ImageConstants.IMAGE_YAML_SUFFIX;
        log.info("createImageYaml -- filePath: " + filepath);

        // new file writer
        FileWriter fileWriter = new FileWriter(filepath);
        Yaml yaml = new Yaml(dumperOptions);

        // 构建spec部分yaml文件
        Map<String, Object> specData = new HashMap<>();
        //Float size = image.getSize();
        Integer vdSize = image.getVdSize();
        String format = image.getFormat().toString().toUpperCase();
        String os_arch = image.getOsArch().toString().toUpperCase();
        String os_mode = image.getOsMode().toString().toUpperCase();
        String os_type = image.getOsType().toString().toUpperCase();
        //specData.put("size", size);
        specData.put("vd_size", vdSize);
        specData.put("format", format);
        specData.put("os_arch", os_arch);
        specData.put("os_mode", os_mode);
        specData.put("os_type", os_type);

        // 构建config部分yaml文件
        /*
        Map<String, Object> configData = new HashMap<>();
        Integer min_disk = image.getMinDisk();
        configData.put("min_disk", min_disk);
        Integer min_mem = image.getMinMem();
        configData.put("min_mem", min_mem);
         */

        // 构建image部分yaml文件
        Map<String, Object> imageData = new HashMap<>();
        String name = image.getName();
        String filename = getImageFileName(uuid, image.getFormat());
        String description = image.getDescription();
        imageData.put("name", name);
        imageData.put("uuid", uuid);
        imageData.put("filename", filename);
        imageData.put("description", description);
        imageData.put("spec", specData);
        //imageData.put("config", configData);

        // 构建最顶层yaml
        Map<String, Object> dataMap = new HashMap<>();
        dataMap.put("image", imageData);
        yaml.dump(dataMap, fileWriter);
    }

    private static String getImageFileName(String imageUuid, ImageFormatEnum formatEnum){
        switch (formatEnum){
            case IMAGE:
                return imageUuid + VolumeConstants.IMG_VOLUME_SUFFIX;
            case QCOW2:
                return imageUuid + VolumeConstants.WIN_VOLUME_SUFFIX;
            default:
                return null;
        }
    }
}
