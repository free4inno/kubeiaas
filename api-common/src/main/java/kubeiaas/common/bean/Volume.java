package kubeiaas.common.bean;

import kubeiaas.common.enums.volume.VolumeStatusEnum;
import kubeiaas.common.enums.volume.VolumeFormatEnum;
import kubeiaas.common.enums.volume.VolumeUsageEnum;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Volume {
    private Integer id;                            //自增主键
    private String uuid;                     //全局唯一的标识
    private String name;                           //名称
    private String description;

    /* for create. */
    private String hostUuid;
    private String imageUuid;
    private Integer size;                               // size (GB)
    private String providerLocation;                // IP:/srv/nfs4/:volumes/a/z/uuid.img

    /* type */
    private VolumeFormatEnum formatType;           //qcow2 or raw
    private VolumeUsageEnum usageType;               // system or data

    /* status */
    private VolumeStatusEnum status;                // VolumeStatusEnum

    /* for attach */
    private String instanceUuid;                    // instanceUuid
    private String bus;                             // virtio, ide...
    private String mountPoint;                      // vda, sda...

    /* timestamp */
    private Timestamp createTime;

    /* !NOT_IN_DB */
    private String ip;                              // not in database
    private String nfsRoot;                         // not in database
    private String imageFilePath;                   // not in database
    private String message;                         // not in database

    private Vm instanceVm;
}
