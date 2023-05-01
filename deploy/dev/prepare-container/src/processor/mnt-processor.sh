#!/bin/bash

# ============================
#   KubeIaaS - Env Processor
#   @ mnt-processor
# ============================
# Author:   free4inno
# Date:     2022-09-22
#
# mnt-processor is used to mount `images` and `data-volumes`
#
# Params:
#
#   -i : the address which your image storage mount to
#   -d : the address which your data-volume storage mount to
#
# Example:
#
#   sh mnt-processor.sh -i 192.168.33.7:/image/path -d 192.168.33.7:/datavol/path/
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
    res=$(systemctl status $1 2>&1)
    if [[ $res =~ (dead) || $res =~ "Active: inactive" ]]; then
        echo "0"
    elif [[ $res =~ "Active: active" ]]; then
        echo "1"
    else
        echo "0"
    fi
}

# Output prepare log
function log(){
  cmd="sed -i '/""$1""/d' /usr/local/kubeiaas/workdir/log/prepare_result.log"
  eval "$cmd"
  echo -e "$1=$2" | tee -a /usr/local/kubeiaas/workdir/log/prepare_result.log
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

        echo ">>> failed"
        log mnt failed
        echo ""
        exit
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
            log mnt failed
            echo ""
            exit
        fi
    fi

    # 3. check `/etc/fstab` ==================
    echo "[3] check '/etc/fstab' config"
    res=$(cat /etc/fstab)
    if [[ $res =~ $KUBEIAAS_MNT_IMG && $res =~ $KUBEIAAS_MNT_DATAVOL ]]; then
        echo "[-] fstab config is OK."
    else 
        echo "[-] fstab config have not writen!"
        echo "[-] start to write fstab config..."
        
        # write fstab
        cp /etc/fstab /etc/fstab.bak
        echo "[-] fstab backup to '/etc/fstab.bak'"
        
        echo -e "
$KUBEIAAS_MNT_IMG $KUBEIAAS_PATH_IMAGES  nfs    rw,soft,timeo=30,retry=3 0 0
$KUBEIAAS_MNT_DATAVOL $KUBEIAAS_PATH_DATA_VOLUMES  nfs    rw,soft,timeo=30,retry=3 0 0
" | tee -a /etc/fstab

        echo "[+] wrote fstab"

    fi

    # 4. check mount status ====================
    echo "[4] check mount status"
    res=$(df -h)
    if [[ $res =~ $KUBEIAAS_PATH_IMAGES && $res =~ $KUBEIAAS_PATH_DATA_VOLUMES ]]; then
        echo "[-] mount is OK."
        echo ">>> success"
        log mnt success
        echo ""
        exit
    else
        echo "[-] have not mount!"
        echo "[-] start to mount..."
        
        # write fstab
        mount -t nfs -o rw,soft,timeo=30,retry=3 $KUBEIAAS_MNT_IMG $KUBEIAAS_PATH_IMAGES
        mount -t nfs -o rw,soft,timeo=30,retry=3 $KUBEIAAS_MNT_DATAVOL $KUBEIAAS_PATH_DATA_VOLUMES
        
        # recheck
        echo "[+] recheck mount status"
        res=$(df -h)
        if [[ $res =~ $KUBEIAAS_PATH_IMAGES && $res =~ $KUBEIAAS_PATH_DATA_VOLUMES ]]; then
            echo "[-] mount success."
            echo ">>> success"
            log mnt success
            echo ""
            exit
        else
            echo "[-] mount failed!"
            echo ">>> failed"
            log mnt failed
            echo ""
            exit
        fi
    fi
}

# --------------------------------------------------

echo ""
echo "# ========================== #"
echo "#  KubeIaaS - Env Processor  #"
echo "#  @ mnt-processor           #"
echo "# ========================== #"
echo "$(date +%Y-%m-%d\ %H:%M:%S)"
echo ""

KUBEIAAS_PATH_IMAGES=/usr/local/kubeiaas/data/images
KUBEIAAS_PATH_DATA_VOLUMES=/usr/local/kubeiaas/data/data-volumes

KUBEIAAS_MNT_IMG=""
KUBEIAAS_MNT_DATAVOL=""

while getopts ":i:d:" opt
do
    case $opt in
        i)
            echo "KUBEIAAS_MNT_IMG: $OPTARG"
            KUBEIAAS_MNT_IMG=$OPTARG
        ;;
        d)
            echo "KUBEIAAS_MNT_DATAVOL: $OPTARG"
            KUBEIAAS_MNT_DATAVOL=$OPTARG
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