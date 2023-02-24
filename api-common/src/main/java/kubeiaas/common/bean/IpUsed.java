package kubeiaas.common.bean;

import kubeiaas.common.enums.network.IpAttachEnum;
import kubeiaas.common.enums.network.IpTypeEnum;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class IpUsed {
    private Integer id;

    /* basic info */
    private String ip;
    private String mac;
    private IpTypeEnum type;

    /* related */
    private Integer ipSegmentId;
    private String bridge;          // 所用网桥

    /* for attach */
    private String instanceUuid;    // attach of: IP & VM
    private IpAttachEnum status;    // attach of: IP & MAC

    /* timestamp */
    private Timestamp createTime;

    /* !NOT_IN_DB */
    private String instanceName;
}
