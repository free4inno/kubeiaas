package kubeiaas.common.bean;

import kubeiaas.common.constants.bean.VolumeConstants;
import kubeiaas.common.utils.PathUtils;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Iso {
    private Integer id; // 自增主键
    private String uuid; // 全局唯一的标识
    private String name; // 名称
    private String description; // 描述
    private String hostUuid; // 分配host，作为资源调度
    private long size; // ISO的大小
    private String relativePath; // ISO的相对路径
    private Timestamp createTime; // 创建时间

    /* !NOT_IN_DB */
    private String nfsPath; // ISO的NFS路径
    private String nfsServer; // ISO的NFS服务器

    public String getAbsolutePath() {
        return PathUtils.mergePaths(VolumeConstants.DEFAULT_NFS_SRV_PATH + VolumeConstants.ISO_PATH + relativePath);
    }
}
