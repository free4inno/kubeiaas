package kubeiaas.common.bean;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
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
    private int id;

    /* basic info */
    private String ip;
    private String mac;
    private IpTypeEnum type;

    /* related */
    private int ipSegmentId;

    /* for attach */
    private String instanceUuid;
    private IpAttachEnum status;

    /* timestamp */
    private Timestamp createTime;
}
