# 部署 Kubernetes

本部分将主要引导完成 Kubernetes 1.23 的环境搭建。

请先确保集群已经安装 Ubuntu 22.04 或 20.04 以上的操作系统，并在集群节点间具有相互可达的网络连接。

## 1. 准备系统

### 1.1. 禁用Swap

```
swapoff -a
```

编辑并注释swap分区

```
vi /etc/fstab
```

检查禁用是否成功

```
free -m
```

### 1.2. 修改hosts

```
cat >> /etc/hosts << EOF
<HOST_IP> <HOST_NAME>
<HOST_IP> <HOST_NAME>
...
<HOST_IP> <HOST_NAME>
EOF
```

`<HOST_IP>`为集群节点IP，`<HOST_NAME>`为集群节点名称。

### 1.3. 更新apt源

确保系统已经正确配置可用的apt源，并更新 Ubuntu 系统。

> 可基于如下命令配置清华源：
>
> ```
> sudo sed -i "s@http://.*archive.ubuntu.com@https://mirrors.tuna.tsinghua.edu.cn@g" /etc/apt/sources.list
> sudo sed -i "s@http://.*security.ubuntu.com@https://mirrors.tuna.tsinghua.edu.cn@g" /etc/apt/sources.list
> ```

```
sudo apt update
sudo apt upgrade
```

## 2. 安装 Docker

安装 Docker

```
sudo apt install docker.io
docker --version
```

配置 Cgroup 与 Docker 镜像源

> 此处请使用所在地区支持的镜像源，此处以 ustc 源为例

```
docker info | grep Cgroup

sudo vi /etc/docker/daemon.json
# ------ 加入以下内容 ------
{
    "registry-mirrors":["https://docker.mirrors.ustc.edu.cn"],
    "exec-opts":["native.cgroupdriver=systemd"]
}
# ------------------------

sudo systemctl daemon-reload
sudo systemctl restart docker

docker info | grep Cgroup
```

## 3. **配置 Kubernetes 软件源**

配置 apt 支持 ssl 传输

```
sudo apt-get update && sudo apt-get install -y apt-transport-https
```

下载 gpg 密钥

> 此处请使用所在地区支持的镜像源，此处以阿里源为例

```
sudo apt install -y curl
sudo curl https://mirrors.aliyun.com/kubernetes/apt/doc/apt-key.gpg | sudo apt-key add -
```

添加 Kubernetes 镜像源

```
cat <<EOF >/etc/apt/sources.list.d/kubernetes.list
deb https://mirrors.aliyun.com/kubernetes/apt/ kubernetes-xenial main
EOF
```

更新源列表

```
sudo apt-get update
```

## 4. 安装 kubeadm、kubelet 和 kubectl

安装组件

```
sudo apt-get install -y kubelet=1.23.0-00 kubeadm=1.23.0-00 kubectl=1.23.0-00
```

锁定版本，防止自动更新

```
sudo apt-mark hold kubeadm kubelet kubectl
```

允许开机自启 kubelet

```
sudo systemctl enable kubelet
```

## 5. **初始化 Kubernetes 集群**

初始化 K8s 集群，请在 master 节点上执行以下命令，

> 此处请使用所在地区支持的镜像源，此处以阿里源为例

```
sudo kubeadm init \
--image-repository=registry.aliyuncs.com/google_containers \
--pod-network-cidr=10.244.0.0/16
```

完成后，将显示一条关于如何将其他节点加入集群的消息。请记住这条消息，并在其他 Node 节点上执行，

> 如下只是一个示例，请使用实际获取到的 token

```
kubeadm join xx.xxx.xxx.xxx:6443 --token xxxxxx.xxxxxxxxxxxxxxx \
 --discovery-token-ca-cert-hash sha256:xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx
```

初始化完成后，设置用户的 kubeconfig 文件以与新集群进行交互：

```
mkdir -p $HOME/.kube
sudo cp -i /etc/kubernetes/admin.conf $HOME/.kube/config
sudo chown $(id -u):$(id -g) $HOME/.kube/config
```

## 6. **部署 Pod 网络**

现在，部署 Pod 网络以允许集群内的通信。我们将使用 Flannel：

在集群 master 节点上，下载 kube-flannel.yml

```
curl -O https://raw.githubusercontent.com/coreos/flannel/master/Documentation/kube-flannel.yml
```

> 如果无法正常通过外网下载，可以在本地下载后上传到集群 master 节点 [链接](/docs/lib/kube-flannel.yml)

部署 Flannel

```
kubectl create -f kube-flannel.yml
```

等待所有 Pod 变为 "Running" 状态

```bash
kubectl get pods --all-namespaces
```

至此，已成功在 Ubuntu 上安装了 Kubernetes 1.35。现在可以按照 [KubeIaaS 部署文档](/docs/deploy/deploy-kubeiaas-1.0-ubuntu-cn.md) 开始在集群上部署 KubeIaaS 了。

## 常见问题

### 如何让Kubernetes的master节点能够作为KubeIaaS的工作节点？

### 如何单节点部署？

当节点不足，或是希望单节点部署KubeIaaS时，需要使 master 节点可以部署应用。请使用以下命令，

```
kubectl taint nodes --all node-role.kubernetes.io/master-
```