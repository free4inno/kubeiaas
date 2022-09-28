#!/bin/bash

# ============================
#   KubeIaaS - Env Checker
#   @ mnt-checker
# ============================
# Author:   free4inno
# Date:     2022-09-22
#
# mnt-checker is used to mount `images` and `data-volumes`
#
# Params:
#
#   -m : the IP which your volume is mount to
#
# Example:
#
#   sh mnt-checker.sh -m 192.168.33.6
#

# ----------------------- Function -----------------------

# Check system service's installation.
# param：
#   - service_name
# return：
#   - is_install (0-n, 1-y)
function is_service_install(){
    res=$(systemctl status $1 2>&1)
    if [[ $res =~ "could not be found" || $res =~ "Loaded: not-found" ]]; then
        echo "0"
    else
        echo "1"
    fi
}


# Check system service's status.
# param：
#   - service_name
# return：
#   - is_active (0-n, 1-y)
function is_service_active(){
    res=$(systemctl status $1)
    if [[ $res =~ (dead) || $res =~ "Active: inactive" ]]; then
        echo "0"
    elif [[ $res =~ "Active: active" ]]; then
        echo "1"
    else
        echo "0"
    fi
}

# ----------------------- Main -----------------------

function main(){

    SERVICE_NFS=nfs

    # 1. check NFS installation =============
    echo "[1] check NFS installation"
    res=$(is_service_install $SERVICE_NFS)
    if [ $res == "1" ]; then
        # --- nfs founded ---
        echo "[-] nfs is founded."
    else
        # --- nfs not found ---
        echo "[-] nfs could not be found!"
        echo "[+] start to install nfs..."

        # check Yum repo
        res=$(ls /etc/yum.repos.d | grep ali)
        if [ -z $res ]; then
            # no ali repo
            echo "[yum] adding ali repo..."
            sudo curl -o /etc/yum.repos.d/CentOS-ali.repo http://mirrors.aliyun.com/repo/Centos-7.repo
            echo "[yum] make cache..."
            yum makecache
        else
            echo "[yum] yum repo exist. OK"
        fi

        # yum install
        yum -y install nfs-utils rpcbind

        # recheck
        res=$(is_service_install $SERVICE_NFS)
        if [ $res == "1" ]; then
            # --- nfs founded ---
            echo "[-] nfs is installed successfully."
        else
            # --- nfs not found ---
            echo "[-] nfs could not be installed!"
            echo ">>> failed"
            echo -e "result=failed" | tee /usr/local/kubeiaas/workdir/log/checkResult-mnt.log
            echo ""
            exit
        fi
    fi

    # 2. check NFS active ============
    echo "[2] check NFS status"
    res=$(is_service_active $SERVICE_NFS)
    if [ $res == "1" ]; then
        echo "[-] nfs is already active."
    else
        echo "[-] nfs have not started!"
        echo "[+] try to start nfs..."

        # start
        service nfs start

        # recheck
        res=$(is_service_active $SERVICE_NFS)
        if [ $res == "1" ]; then
            echo "[-] nfs is active now."
        else
            echo "[-] nfs still have not started!"
            echo "[+] please check manually..."
            echo ">>> failed"
            echo -e "result=failed" | tee /usr/local/kubeiaas/workdir/log/checkResult-mnt.log
            echo ""
            exit
        fi
    fi

    # 3. check `/etc/fstab` ==================
    echo "[3] check '/etc/fstab' config"
    res=$(cat /etc/fstab)
    if [[ $res =~ $KUBEIAAS_PATH_IMAGES && $res =~ $KUBEIAAS_PATH_DATA_VOLUMES ]]; then
        echo "[-] fstab config is OK."
    else 
        echo "[-] fstab config have not writen!"
        echo "[-] start to write fstab config..."
        
        # write fstab
        cp /etc/fstab /etc/fstab.bak
        echo "[-] fstab backup to '/etc/fstab.bak'"
        
        echo -e "
$KUBEIAAS_MNT_TARGET:$KUBEIAAS_PATH_IMAGES $KUBEIAAS_PATH_IMAGES  nfs    rw,soft,timeo=30,retry=3 0 0
$KUBEIAAS_MNT_TARGET:$KUBEIAAS_PATH_DATA_VOLUMES $KUBEIAAS_PATH_DATA_VOLUMES  nfs    rw,soft,timeo=30,retry=3 0 0
" | tee -a /etc/fstab

        echo "[+] wrote fstab"

    fi

    # 4. check mount status ====================
    echo "[4] check mount status"
    res=$(df -h)
    if [[ $res =~ $KUBEIAAS_PATH_IMAGES && $res =~ $KUBEIAAS_PATH_DATA_VOLUMES ]]; then
        echo "[-] mount is OK."
        echo ">>> success"
        echo -e "result=success" | tee /usr/local/kubeiaas/workdir/log/checkResult-mnt.log
        echo ""
        exit
    else
        echo "[-] have not mount!"
        echo "[-] start to mount..."
        
        # write fstab
        mount -t nfs -o rw,soft,timeo=30,retry=3 $KUBEIAAS_MNT_TARGET:$KUBEIAAS_PATH_IMAGES $KUBEIAAS_PATH_IMAGES 
        mount -t nfs -o rw,soft,timeo=30,retry=3 $KUBEIAAS_MNT_TARGET:$KUBEIAAS_PATH_DATA_VOLUMES $KUBEIAAS_PATH_DATA_VOLUMES
        
        # recheck
        echo "[+] recheck mount status"
        res=$(df -h)
        if [[ $res =~ $KUBEIAAS_PATH_IMAGES && $res =~ $KUBEIAAS_PATH_DATA_VOLUMES ]]; then
            echo "[-] mount success."
            echo ">>> success"
            echo -e "result=success" | tee /usr/local/kubeiaas/workdir/log/checkResult-mnt.log
            echo ""
            exit
        else
            echo "[-] mount failed!"
            echo ">>> failed"
            echo -e "result=failed" | tee /usr/local/kubeiaas/workdir/log/checkResult-mnt.log
            echo ""
            exit
        fi
    fi
}

# --------------------------------------------------

echo ""
echo "# ========================== #"
echo "#   KubeIaaS - Env Checker   #"
echo "#   @ mnt-checker            #"
echo "# ========================== #"

KUBEIAAS_PATH_IMAGES=/usr/local/kubeiaas/data/images
KUBEIAAS_PATH_DATA_VOLUMES=/usr/local/kubeiaas/data/data-volumes

KUBEIAAS_MNT_TARGET=""

while getopts ":m:" opt
do
    case $opt in
        m)
            echo "KUBEIAAS_MNT_TARGET: $OPTARG"
            KUBEIAAS_MNT_TARGET=$OPTARG
        ;;
        *)
            echo "unknown param: $opt"
            echo ">>> failed"
            echo ""
            exit
        ;;
        ?)
            echo "unknown param: $opt"
            echo ">>> failed"
            echo ""
            exit
        ;;
    esac
done
echo ""

echo "KUBEIAAS_PATH_IMAGES: $KUBEIAAS_PATH_IMAGES"
echo "KUBEIAAS_PATH_DATA_VOLUMES: $KUBEIAAS_PATH_DATA_VOLUMES"

echo ""
main
echo ""