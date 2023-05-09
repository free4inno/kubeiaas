# 安装操作系统

在该部分中，将简要介绍如何基于裸机集群部署KubeIaaS。

## 1. 环境要求

请首先确保集群设备CPU支持KVM虚拟化，

请检查 BIOS 设置，确保CPU虚拟化能力处于开启状态（Intel vmx / AMD svm）

## 2. 安装操作系统

### 2.1. 准备工作

1. 下载 Ubuntu 22.04 LTS 镜像文件：

   > 推荐为操作系统安装图形界面，以便于后期对节点上的云主机进行本地操作维护。

   如果希望安装包含图形界面的 Desktop 版本，

   请访问 https://releases.ubuntu.com/22.04.2/ubuntu-22.04.2-desktop-amd64.iso 下载镜像文件。

   如果希望安装不含图形界面的 Server 版本，

   请访问 https://releases.ubuntu.com/22.04.2/ubuntu-22.04.2-live-server-amd64.iso 下载镜像文件。

2. 准备一个可启动的 USB 驱动器（至少 4GB 容量）或一张空白的 DVD 光盘。我们将在其中创建一个启动介质。

3. 下载一个适合的操作系统 USB 启动盘制作工具（例如：Rufus、Etcher 等），或使用光盘刻录软件（如 Nero、ImgBurn 等）。

### 2.2. 执行安装

1. 将制作好的启动介质插入计算机。
2. 重新启动计算机，进入 BIOS/UEFI 设置，更改启动顺序，将 USB 驱动器或 DVD 光盘设置为首选启动设备。保存设置并退出。
3. 计算机将从启动介质启动，进入 Ubuntu 22.04 LTS 安装程序。
4. 跟随引导完成操作系统安装。

> 在系统分区过程中，请确保 /usr/local 目录下有足够的可用空间用于承载 IaaS 云业务，同时建议使用 LVM 分区以便于后期可能需要的系统扩容。

完成操作系统安装后，请转到 [Kubernetes 部署文档](/docs/deploy/deploy-kubernetes-1.23-cn.md) 继续完成 Kubernetes 集群环境搭建。

## 常见问题

### 如何为我的系统安装 SSH？

如果安装的是 Ubuntu 的 Desktop 版本，或在安装 Server 版本时未勾选 SSH 服务，请使用如下命令安装 SSH 服务并开启，

> 请确保系统 apt 包含可用的源

```
sudo apt update
sudo apt upgrade

sudo apt install openssh-server
sudo systemctl status ssh
```

如果还希望进一步开启root远程连接权限，请编辑 `/etc/ssh/sshd_config` 并加入 / 修改如下内容

```
sudo vi /etc/ssh/sshd_config

# PermitRootLogin prohibit-password
PermitRootLogin yes
```

### 如何为我的系统安装图形界面？

如果安装的是 Ubuntu 的 Server 版本，将不会带有图形界面，请使用如下命令安装 GNOME 桌面，

> 请确保系统 apt 包含可用的源

```
sudo apt update
sudo apt upgrade

sudo apt install ubuntu-desktop
```

桌面环境安装完成后，需要启用系统的图形界面。运行以下命令以启用图形界面：

```
sudo systemctl set-default graphical.target
```

现在，需要重启系统以加载新安装的桌面环境。在终端中运行以下命令以重新启动计算机：

```
sudo reboot
```

重启后，即可看到登录页面，并可以使用新安装的图形界面。

### 为何我的系统端口无法被外部访问？

这是由于 Ubuntu 默认开启了系统防火墙（ufw），可以根据实际需求选择禁用，

```
ufw status
ufw disable
```

或是按照规则预留某些特定端口（以22端口为例）

```
ufw allow 22
ufw reload
```



