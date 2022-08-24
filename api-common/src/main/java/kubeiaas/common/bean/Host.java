package kubeiaas.common.bean;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Host {
    private int id;
    private String uuid;            // 全局唯一标识，默认以机器网卡MAC地址生成
    private String name;            // 来自k8s，node-name
    private String ip;              // 节点ip，私网地址
}
