package kubeiaas.common.bean;

import kubeiaas.common.enums.host.HostStatusEnum;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Host {
    private Integer id;
    private String uuid;            // 全局唯一标识，默认以机器网卡MAC地址生成
    private String name;            // 来自k8s，node-name
    private String ip;              // 节点ip，私网地址

    private String config;          // 节点信息
    private HostStatusEnum status;  // 节点状态
    private String role;            // 节点角色，例如：["dhcp","vnc","mnt"]
}
