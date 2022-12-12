package kubeiaas.iaasagent.utils;


import kubeiaas.common.bean.Image;
import kubeiaas.common.constants.bean.ImageConstants;
import kubeiaas.common.constants.bean.VolumeConstants;
import kubeiaas.common.enums.image.ImageFormatEnum;
import kubeiaas.common.utils.PathUtils;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;

import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class volumeUtils {

    public static void createImageYaml(Image image) throws IOException {
        //设置yml格式
        DumperOptions dumperOptions = new DumperOptions();
        dumperOptions.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
        //创建FileWriter
        String uuid = image.getUuid();
        String filepath = PathUtils.genFullPath(ImageConstants.IMAGE_PATH + uuid + ImageConstants.IMAGE_YAML_SUFFIX);
        FileWriter fileWriter = new FileWriter(filepath);
        Yaml yaml = new Yaml(dumperOptions);
        //构建spec部分yaml文件
        Map<String, Object> specData = new HashMap<String, Object>();
        Float size = image.getSize();
        String format = image.getFormat().toString().toUpperCase();
        String os_arch = image.getOsArch().toString().toUpperCase();
        String os_mode = image.getOsMode().toString().toUpperCase();
        String os_type = image.getOsType().toString().toUpperCase();
        specData.put("size", size);
        specData.put("format", format);
        specData.put("os_arch", os_arch);
        specData.put("os_mode", os_mode);
        specData.put("os_type", os_type);
        //构建config部分yaml文件
        Map<String, Object> configData = new HashMap<String, Object>();
        Integer min_disk = image.getMinDisk();
        configData.put("min_disk", min_disk);
        //构建image部分yaml文件
        Map<String, Object> imageData = new HashMap<String, Object>();
        String name = image.getName();
        String filename = getImageFileName(uuid, image.getFormat());
        String description = image.getDescription();
        imageData.put("name", name);
        imageData.put("uuid", uuid);
        imageData.put("filename", filename);
        imageData.put("description", description);
        imageData.put("spec", specData);
        imageData.put("config", configData);
        //构建最顶层yaml
        Map<String, Object> dataMap = new HashMap<String, Object>();
        dataMap.put("image", imageData);
        yaml.dump(dataMap, fileWriter);
    }


    public static String getImageFileName(String imageUuid, ImageFormatEnum formatEnum){
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
