#!/bin/bash

# ============================
#   KubeIaaS - Env Checker
#   @ libvirt-checker
# ============================
# Author:   free4inno
# Date:     2022-09-20
#
# libvirt-checker is used to check libvirtd env and config.
#

# ----------------------- Function -----------------------

# Check system service's status.
# param：
#   - service_name
# retrun：
#   - is_active (0-n, 1-y)
function is_service_active(){
    result=$(systemctl status $1)
    if [[ $result =~ (dead) || $result =~ "Active: inactive" ]]; then
        echo "0"
    elif [[ $result =~ "Active: active" ]]; then
        echo "1"
    else
        echo "0"
    fi
}

# ----------------------- Main -----------------------
function main(){

    LIBVIRTD=libvirtd

    # 1. check libvirtd install ==============
    echo "[1] check libvirtd installation"
    res=$(virsh -version)
    if [[ $res =~ 7.0.0 ]]; then
        # --- libvirtd founded ---
        echo " - libvirtd is founded."
    else
        # --- libvirtd is not founded ---
        echo " - libvirtd is not founded!"
        echo " - start to install libvirtd-7.0.0...!"

        # >>> install <<<
        echo "===== [VERSION LIST] ====="
        echo "=    QEMU    : 4.0.0     ="
        echo "=    Python  : 3.6.8     ="
        echo "=    meson   : 0.61.5    ="
        echo "=    ninja   : 1.10.2    ="
        echo "=    libvirt : 7.0.0     ="
        echo "=========================="

        echo "<0> Check yum repo (ali) ---------------------"
        res=$(ls /etc/yum.repos.d | grep ali)
        if [ -z $res ]; then
            # no ali repo
            echo "[yum] adding ali repo..."
            sudo curl -o /etc/yum.repos.d/CentOS-ali.repo http://mirrors.aliyun.com/repo/Centos-7.repo
            echo "[yum] make cache..."
            yum makecache
        else
            echo "[yum] yum repo ali exist. OK"
        fi

        # ===

        echo "<1> UPDATE QEMU to 4.0.0 ---------------------"
        yum -y install glib2-devel zlib-devel pixman-devel libaio-devel
        curl -o /usr/local/kubeiaas/libvirt/qemu-4.0.0.tar.xz https://download.qemu.org/qemu-4.0.0.tar.xz
        tar -xvf /usr/local/kubeiaas/libvirt/qemu-4.0.0.tar.xz -C /usr/local/kubeiaas/libvirt/
        (cd /usr/local/kubeiaas/libvirt/qemu-4.0.0; ./configure --target-list=x86_64-softmmu --enable-linux-aio)
        (cd /usr/local/kubeiaas/libvirt/qemu-4.0.0; make && make install)

        echo " - add lib path"
        echo -e "
include /usr/local/lib" | tee -a /etc/ld.so.conf

        echo " - enable lib path config"
        ldconfig

        echo " - QEMU version: "
        qemu-img --version

        # ===

        echo "<2> epel yum repo ------------------------------"
        echo " - yum install epel-release..."
        yum -y install epel-release

        # ===

        echo "<3> Python3 ------------------------------------"
        yum -y install python3
        python3 --version

        # ===

        echo "<4> install edk2 -------------------------------"
        curl -o /etc/yum.repos.d/firmware.repo https://www.kraxel.org/repos/firmware.repo
        yum -y install edk2.git-aarch64

        # ===

        echo "<5> install dependencies ------------------------"
        yum -y install libxml2-devel readline-devel ncurses-devel libtasn1-devel gnutls-devel libattr-devel \
            libblkid-devel augeas systemd-devel libpciaccess-devel yajl-devel sanlock-devel libpcap-devel libnl3-devel \
            libselinux-devel dnsmasq radvd cyrus-sasl-devel libacl-devel parted-devel device-mapper-devel xfsprogs-devel \
            librados2-devel librbd1-devel glusterfs-api-devel glusterfs-devel numactl-devel libcap-ng-devel fuse-devel \
            netcf-devel libcurl-devel audit-libs-devel systemtap-sdt-devel libtirpc-devel nfs-utils dbus-devel scrub numad
        yum -y install libvirt-client

        # ===

        echo "<6> meson & ninja -------------------------------"
        pip3 install meson
        ln -s /usr/local/bin/meson /usr/bin/meson
        meson --version
        yum -y install ninja-build
        ninja --version

        # ===

        echo "<7> get libvirt-7.0.0 package ---------------------------------"
        curl -o /usr/local/kubeiaas/libvirt/libvirt-7.0.0.tar.xz https://libvirt.org/sources/libvirt-7.0.0.tar.xz
        tar -xvf /usr/local/kubeiaas/libvirt/libvirt-7.0.0.tar.xz -C /usr/local/kubeiaas/libvirt/

        # ===

        echo "<8> build & install -----------------------------"
        pip3 install rst2html5
        (cd /usr/local/kubeiaas/libvirt/libvirt-7.0.0; meson build --prefix=/usr)
        pip3 uninstall -y rst2html5
        (cd /usr/local/kubeiaas/libvirt/libvirt-7.0.0; meson build --wipe --prefix=/usr)

        (cd /usr/local/kubeiaas/libvirt/libvirt-7.0.0; ninja -C build)
        (cd /usr/local/kubeiaas/libvirt/libvirt-7.0.0; ninja -C build install)

        systemctl daemon-reload
        systemctl restart libvirtd
        virsh --version

        # recheck
        res=$(virsh -version)
        if [[ $res =~ 7.0.0 ]]; then
            echo " - libvirtd is successfully installed."
        else
            # --- libvirtd is not founded ---
            echo " - libvirtd is installed failed! Please install libvirtd-7.0.0 manually..."
            echo ">>> failed"
            echo ""
            exit
        fi
    fi

    # 2. check libvirtd status ===============
    echo "[2] restart libvirtd"
    echo " - restart libvirtd..."

    # >>> start <<<
    service libvirtd restart

    # recheck
    res=$(is_service_active $LIBVIRTD)
    if [ $res == "1" ]; then
        # --- libvirtd active ---
        echo " - libvirtd is active now."
    else
        # --- libvirtd is not started ---
        echo " - libvirtd is failed to start! Please start libvirtd-7.0.0 manually..."
        echo ">>> failed"
        echo ""
        exit
    fi


    # 2. check qemu support ===============
    echo "[3] check qemu support"
    res=$(virsh version)
    echo $res
    if [[ $res =~ "QEMU" ]]; then
        echo " - libvirtd support qemu."
        echo ">>> success"
        echo ""
        exit
    else
        echo " - libvirtd API error! No QEMU found or libvirt connection error, please Check it manually..."
        echo ">>> failed"
        echo ""
        exit
    fi
}

# --------------------------------------------------

echo ""
echo "# ========================== #"
echo "#   KubeIaaS - Env Checker   #"
echo "#   @ libvirt-checker        #"
echo "# ========================== #"
echo ""

echo ""
main
echo ""