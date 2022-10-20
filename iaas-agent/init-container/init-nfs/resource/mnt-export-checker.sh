#!/bin/bash

# ============================
#   KubeIaaS - Env Checker
#   @ mnt-export-checker
# ============================
# Author:   free4inno
# Date:     2022-09-22
#
# mnt-export-checker is used to export `images` and `data-volumes`
#
# Params:
#
#   -m : the IP which will be mount by
#
# Example:
#
#   sh mnt-export-checker.sh -m 192.168.33.7
#   sh mnt-export-checker.sh -m *
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
            echo -e "result=failed" | tee /usr/local/kubeiaas/workdir/log/checkResult-mnt-export.log
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
            echo -e "result=failed" | tee /usr/local/kubeiaas/workdir/log/checkResult-mnt-export.log
            echo ""
            exit
        fi
    fi

    # 3. check `/etc/exports` ==================
    echo "[3] check '/etc/exports' config"
    res=$(cat /etc/exports)
    if [[ $res =~ "$KUBEIAAS_PATH_IMAGES $KUBEIAAS_MNT_TARGET" && $res =~ "$KUBEIAAS_PATH_DATA_VOLUMES $KUBEIAAS_MNT_TARGET" ]]; then
        echo "[-] exports config is OK."
        echo ">>> success"
        echo -e "result=success" | tee /usr/local/kubeiaas/workdir/log/checkResult-mnt-export.log
        echo ""
        exit
    else 
        echo "[-] exports config have not writen!"
        echo "[-] start to write exports config..."
        
        # write exports
        cp /etc/exports /etc/exports.bak
        echo "[-] exports backup to '/etc/exports.bak'"
        
        echo -e "
$KUBEIAAS_PATH_IMAGES $KUBEIAAS_MNT_TARGET(rw,no_root_squash)
$KUBEIAAS_PATH_DATA_VOLUMES $KUBEIAAS_MNT_TARGET(rw,no_root_squash)
" | tee -a /etc/exports

        echo "[-] wrote exports"

        # enable
        echo "[-] enable exports"
        exportfs -a

        # recheck
        echo "[-] recheck exports"
        res=$(cat /etc/exports)
        if [[ $res =~ "$KUBEIAAS_PATH_IMAGES $KUBEIAAS_MNT_TARGET" && $res =~ "$KUBEIAAS_PATH_DATA_VOLUMES $KUBEIAAS_MNT_TARGET" ]]; then
            echo "[-] exports config wrote success."
            echo ">>> success"
            echo -e "result=success" | tee /usr/local/kubeiaas/workdir/log/checkResult-mnt-export.log
            echo ""
            exit
        else 
            echo "[-] exports config wrote failed!"
            echo ">>> failed"
            echo -e "result=failed" | tee /usr/local/kubeiaas/workdir/log/checkResult-mnt-export.log
            echo ""
            exit
        fi
    fi
}

# --------------------------------------------------

echo ""
echo "# ========================== #"
echo "#   KubeIaaS - Env Checker   #"
echo "#   @ mnt-export-checker     #"
echo "# ========================== #"
echo $(date +%Y-%m-%d\ %H:%M:%S)
echo ""

echo -e "result=unknown" | tee /usr/local/kubeiaas/workdir/log/checkResult-mnt-export.log

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