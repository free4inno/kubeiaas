package kubeiaas.common.bean;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import kubeiaas.common.enums.network.IpTypeEnum;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class IpSegment {
    private Integer id;

    /* basic info */
    private String netmask;         // 掩码
    private String gateway;         // 网关
    private String dns;             // dns

    private IpTypeEnum type;        // 公网或者私网

    private String ipRangeStart;    // 网段起始地址
    private String ipRangeEnd;      // 网段结束地址
}
