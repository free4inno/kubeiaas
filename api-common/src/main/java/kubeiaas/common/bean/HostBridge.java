package kubeiaas.common.bean;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class HostBridge {
    private Integer id;

    private String hostUuid;         // 主机的UUID
    private String bridgeName;    // 接口名称

    private Integer ipSegmentId;     // 外键，关联到IpSegmentTable

    private String hostName;
}