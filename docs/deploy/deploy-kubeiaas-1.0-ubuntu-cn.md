# 部署 KubeIaaS
> 基于 Ubuntu 系统下的 Kubernetes 集群

## 1. 环境要求

- **系统**

  - **Ubuntu-22.04 TLS** 或 20.04 以上的其他 Ubuntu 发行版；
  - 可用的 Kubernetes 集群环境（Version: v1.23）；

- **计算**

  - 操作系统与 CPU 需支持 KVM 虚拟化
  
  > 可以通过如下命令检查：
  >  ```
  >  cat /proc/cpuinfo | egrep 'vmx|svm'
  >  lsmod | grep kvm
  >  ```

- **存储**

  - 所有KubeIaaS系统数据与云主机系统盘数据将被存储在 `/usr/local/kubeiaas` 目录下，请确保该目录具有充足的可用空间

- **网络**

  - 各节点上预留可用端口：**32200-32215**

  - 物理网卡支持 MacVTap 模式
  
  > 可以通过如下命令检查：
  > ```
  > lsmod | grep macvtap
  > ```
  > 如果不支持请尝试使用 `sudo modprobe macvtap` 开启。
  > 
  > 如果依旧无法生效，请基于 Linux Bridge 或 OVS 手动创建网桥；
  > 并在 “3.2 填写配置清单” 时修改网络模式。

## 2. 基础配置

首先您需要明确一些概念，并在您的Kubernetes集群中选定一些节点：

| 节点类型 | 用途                                            | 所需数量                                           |
|------|-----------------------------------------------|------------------------------------------------|
| 工作节点 | 云主机将会运行在这些节点上，并利用其上的资源<br />（例如，CPU、内存、磁盘、网卡） | **1~N**<br />（需要包含在 Kubernetes 集群中）            |
| 管控节点 | 提供对系统的管控能力<br />（例如，数据存储、资源调度）                | **1**<br />（需要包含在 Kubernetes 集群中，但可以不包含在工作节点中） |

在正式通过 Kubernetes 部署 KubeIaaS 的组件前，您还需要在您的宿主机上搭建一些基础环境。请跟随引导，通过简单的几步即可完成基础配置。

## 2.1 在所有工作节点上

需要在您刚刚选定的每个工作节点上准备一些必须的环境，请在您的每个工作节点上都执行如下配置。

> 需要保证您各个工作节点上的 apt 均配置了可用的源

通过 apt 安装必要的依赖（Java8、Libvirt、NFS、OpenVSwitch），

```
sudo apt -y install openjdk-8-jre-headless libvirt-dev libvirt-daemon-system libvirt-clients virtinst virt-manager qemu-kvm bridge-utils nfs-kernel-server openvswitch-switch ifupdown
```

为 NFS 系统服务创建软连接，

```
sudo ln -s /lib/systemd/system/nfs-server.service /lib/systemd/system/nfs.service
sudo systemctl daemon-reload
```

## 2.2 选择一个工作节点负责网络

需要在您的工作节点中选择一个节点负责网络相关的服务（例如，云主机IP绑定），并在该节点执行如下配置。
通过 apt 安装必要的依赖（DHCP-server），

```
sudo apt-get install -y isc-dhcp-server
```

为 DHCP 系统服务创建软连接，

```
sudo ln -s /lib/systemd/system/isc-dhcp-server.service /lib/systemd/system/dhcpd.service
sudo systemctl daemon-reload
```

> ⚠ 注意！此时DHCP可能由于缺少配置无法运行。但您无需配置，在后续部署中我们将为您完成配置。

## 2.3 选择一个工作节点负责存储

需要在您的工作节点中选择一个节点负责数据存储（云镜像 与 云硬盘）相关的服务，并在该节点执行如下配置。
创建如下存储目录，

```
# 云主机镜像存储目录
sudo mkdir -p /usr/local/kubeiaas/data/images
# 云主机数据盘存储目录
sudo mkdir -p /usr/local/kubeiaas/data/data-volumes
```

开放目录，使其可被其他工作节点挂载，

```
sudo su

# 写入配置文件
cat << EOF >> /etc/exports
/usr/local/kubeiaas/data/images *(rw,no_root_squash,no_subtree_check)
/usr/local/kubeiaas/data/data-volumes *(rw,no_root_squash,no_subtree_check)
EOF

# 使上述修改生效
exportfs -a
showmount -e
```

至此，系统集群已经完成了基础配置，请继续完成组件部署。

## 3. 组件部署

### 3.1 下载安装包

下载并解压 [kubeiaas-1.0-release.tar.gz]() 包，

```
sudo tar -xzvf kubeiaas-1.0-release.tar.gz
```

解压后能够得到如下内容，

```
├─ kubeiaas-1.0-release
│  ├─ iaas-config.yml
│  ├─ iaas-components.yml
│  ├─ iaas-front.yml
│  ├─ label.sh
```

进入 kubeiaas-v1.0-release 目录，

```
cd ./kubeiaas-1.0-release
```

### 3.2 填写配置清单

首先请按照 `iaas-config.yml` 中的说明，完善该 configMap，

```
sudo vi iaas-config.yml
```

如下是一份示例配置，各项配置的具体说明可参考：[config-example](https://gitee.com/free4inno-team/kubeiaas/blob/master/docsdocs/lib/config-example.yml)

```
# -------- 工作节点 --------
nodes.work.list: "node1,node2"

# -------- 管控组件 --------
nodes.control: "master"
nodes.image: "node2"

# -------- 基础服务 --------
nodes.vnc: "node2"
nodes.dhcp: "node2"

nodes.nfs: "node2"
nfs.dir.img: "/usr/local/kubeiaas/data/images"
nfs.dir.dv: "/usr/local/kubeiaas/data/data-volumes"

# -------- 网络配置 --------
network.bridge.type: "MACV"
```

### 3.3 标记节点

完成上述配置后，您还需要使用 kubectl 创建一个 namespace 并按照配置的内容为各个节点打上标签，

```
kubectl create namespace kubeiaas

/bin/bash ./label.sh
# 复制输出的指令并执行

kubectl get node --show-labels
```

### 3.4 部署组件

首先，请应用已经完成编辑的 `iaas-config.yml`，

```
kubectl apply -f iaas-config.yml
```

应用 `iaas-components.yml` 与 `iaas-front.yml`，

```
kubectl apply -f iaas-components.yml
kubectl apply -f iaas-front.yml
```
检查各个 pods 的运行状态，等待所有 pod 状态均变为 `Running`

```
kubectl get pods -o wide -n kubeiaas
```

## 4. 成功！

访问 `<IP>:32200` 转到系统登录页面（此处所用 `<IP>` 为集群内任一节点 IP）
，后续请参考 [用户使用手册](https://gitee.com/free4inno-team/kubeiaas/blob/master/docsuser-manual-cn.md)。 

![](../img/front-login.png)