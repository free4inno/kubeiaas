#!/bin/bash

# ============================
#   KubeIaaS - Env Processor
#   @ libvirt-processor
# ============================
# Author:   free4inno
# Date:     2022-09-20
#
# libvirt-processor is used to check libvirtd env and config.
#

# ----------------------- Function -----------------------

# Check system service's status.
# param：
#   - service_name
# retrun：
#   - is_active (0-n, 1-y)
function is_service_active(){
    result=$(systemctl status $1 2>&1)
    if [[ $result =~ (dead) || $result =~ "Active: inactive" ]]; then
        echo "0"
    elif [[ $result =~ "Active: active" ]]; then
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

    LIBVIRTD=libvirtd

    # 1. check libvirtd install ==============
    echo "[1] check libvirtd installation"
    res=$(virsh version)
    if [[ $res =~ "libvirt" ]]; then
        # --- libvirt founded ---
        echo " - libvirt is founded."
    else
        # --- libvirt is not founded ---
        echo " - libvirt is not founded!"

        echo " - Please install libvirt manually..."
        echo ">>> failed"
        log libvirt failed
        echo ""
        exit
    fi

    # 2. check libvirtd status ===============
    echo "[2] restart libvirtd"
    res=$(is_service_active $LIBVIRTD)
    if [ $res == "1" ]; then
        # --- libvirtd active ---
        echo " - libvirtd is active now."
    else
        # --- libvirtd is not started ---
        echo " - start libvirtd..."
        # >>> start <<<
        service libvirtd start

        # recheck
        res=$(is_service_active $LIBVIRTD)
        if [ $res == "1" ]; then
            # --- libvirtd active ---
            echo " - libvirtd is active now."
        else
            echo " - libvirtd is failed to start! Please start libvirtd manually..."
            echo ">>> failed"
            log libvirt failed
            echo ""
            exit
        fi
    fi


    # 2. check qemu support ===============
    echo "[3] check qemu support"
    res=$(virsh version)
    echo $res
    if [[ $res =~ "QEMU" ]]; then
        echo " - libvirtd support qemu."
        echo ">>> success"
        log libvirt success
        echo ""
        exit
    else
        echo " - libvirtd API error! No QEMU found or libvirt connection error, please Check it manually..."
        echo ">>> failed"
        log libvirt failed
        echo ""
        exit
    fi
}

# --------------------------------------------------

echo ""
echo "# ========================== #"
echo "#  KubeIaaS - Env Processor  #"
echo "#  @ libvirt-processor       #"
echo "# ========================== #"
echo "$(date +%Y-%m-%d\ %H:%M:%S)"
echo ""

echo ""
main
echo ""