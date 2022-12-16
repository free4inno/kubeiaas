package kubeiaas.common.bean;

import kubeiaas.common.enums.image.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Image {
    private Integer id;                 // 自增主键
    private String uuid;                // 全局唯一的标识
    private String name;                // 原始镜像文件名称，如CentOS 6.3.img
    private String description;

    /* basic info */
    private String directory;           // 镜像存储相对路径，如images/a/b/CentOS 6.3-uuid.img
    private ImageFormatEnum format;     // image or iso
    private Float size;                 // 大小，单位G【废弃】
    private Integer vdSize;             // 虚拟磁盘大小

    private ImageStatusEnum status;     // 状态

    private Integer minMem;     // 所需最小内存【废弃】
    private Integer minDisk;    // 所需最小硬盘【废弃】

    /* OS info */
    private ImageOSTypeEnum osType;     // Linux/Windows
    private ImageOSArchEnum osArch;     // x86/i386
    private ImageOSModeEnum osMode;     // hvm/xen/lxc/uml

    /* related */
    private String parentImageUuid;
    private List<Image> childImages;


    public Image(String uuid, String name, ImageOSTypeEnum osType) {
        this.uuid = uuid;
        this.name = name;
        this.osType = osType;
    }

    public Image(String uuid, String name, String description) {
        this.uuid = uuid;
        this.name = name;
        this.description = description;
    }

    public Image(String uuid, String directory) {
        this.uuid = uuid;
        this.directory = directory;
    }
}
