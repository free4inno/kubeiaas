# 部署 KubeIaaS
> 基于非 Ubuntu 系统下的 Kubernetes 集群

如果需要在非推荐系统上部署KubeIaaS，请跟随以下引导：

首先，[确认环境要求](/docs/deploy/deploy-kubeiaas-1.0-ubuntu-cn.md#1-%E7%8E%AF%E5%A2%83%E8%A6%81%E6%B1%82)（需满足除操作系统外的其他要求）；

而后，[选择工作节点与管控节点](/docs/deploy/deploy-kubeiaas-1.0-ubuntu-cn.md#2-%E5%9F%BA%E7%A1%80%E9%85%8D%E7%BD%AE)；

最后，请按照如下环境清单配置各个工作节点，

|     | 需求  | 名称             | 版本      | 节点      | 操作                                                                                                |
|-----|-----|----------------|---------|---------|---------------------------------------------------------------------------------------------------|
| 1   | 必须  | Java8          | v1.8.0  | 每一工作节点  | 正确安装                                                                                              |
| 2   | 必须  | Libvirt        | v6.0.0+ | 每一工作节点  | 正确安装； virsh、qemu-img可用； 运行管理程序配置为 QEMU                                                            |
| 3   | 必须  | NFS            | v3/v4   | 每一工作节点  | 正确安装； 创建systemd服务链接（nfs.service）                                                                  |
| 4   | 三选一 | MacVTap        | -       | 每一工作节点  | 物理网卡支持 MacVTap                                                                                    |
|     |     | LinuxBridge    | -       | 每一工作节点  | 创建网桥，绑定能与集群互联的物理网卡                                                                                |
|     |     | OpenVSwitch    | v2.9+   | 每一工作节点  | 正确安装； 创建网桥，绑定能与集群互联的物理网卡                                                                          |
| 5   | 二选一 | NFS-Server（内部） | v3/v4   | 任意某工作节点 | 正确安装； 创建并开放特定可被挂载的工作目录： `/usr/local/kubeiaas/data/images` `/usr/local/kubeiaas/data/data-volumes` |
|     |     | NFS-Server（外部） | v3/v4   | 任意可达节点  | 开放任意两个可被挂载的工作目录                                                                                   |
| 6   | 必须  | DHCP-Server    | -       | 任意某工作节点 | 正确安装; 配置文件路径：`/etc/dhcp/dhcpd.conf` 创建systemd服务链接（dhcpd.service）                                  |

完成以上环境配置后，请转到 [3. 组件部署](/docs/deploy/deploy-kubeiaas-1.0-ubuntu-cn.md#3-%E7%BB%84%E4%BB%B6%E9%83%A8%E7%BD%B2) 继续操作。
