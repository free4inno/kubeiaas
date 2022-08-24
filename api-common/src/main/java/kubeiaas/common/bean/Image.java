package kubeiaas.common.bean;

import kubeiaas.common.enums.image.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Image {
    private int id;                                //自增主键
    private String uuid;                            //全局唯一的标识
    private String name;                            //原始镜像文件名称，如CentOS 6.3.img
    private String description;

    /* basic info */
    private String directory;                        // 镜像存储相对路径，如images/a/b/CentOS 6.3-uuid.img
    private ImageFormatEnum format;        //image or iso
    private float size;                            //大小，单位G

    private ImageStatusEnum status;                //状态

    private int minMem;                            //所需最小内存
    private int minDisk;                        //所需最小硬盘

    /* OS info */
    private ImageOSTypeEnum osType;                //Linux/Windows
    private ImageOSArchEnum osArch;                //x86/i386
    private ImageOSModeEnum osMode;                //hvm/xen/lxc/uml

    /* related */
    private String parentImageUuid;
}
