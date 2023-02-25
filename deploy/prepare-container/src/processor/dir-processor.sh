#!/bin/bash

# ============================
#   KubeIaaS - Env Processor
#   @ dir-processor
# ============================
# Author:   free4inno
# Date:     2022-09-24
#
# dir-processor is used to check KubeIaaS series directories.
#
# Params:
#
# Example:
#
#   sh dir-processor.sh
#

function check_and_make_dir(){
    if [[ -e /usr/local/kubeiaas/$1 || -d /usr/local/kubeiaas/$1 || $(df -h) =~ /usr/local/kubeiaas/$1 ]]; then
        echo " - /usr/local/kubeiaas/$1 already exist."
    else
        mkdir -p /usr/local/kubeiaas/$1
        echo " + mkdir -p /usr/local/kubeiaas/$1"
    fi
}

# ----------------------- Main -----------------------

function main(){
    # 0. Get host basic info ---------------------
    info_name=$(hostname)
    info_version=$(cat /etc/redhat-release)
    info_cpu_core=$(cat /proc/cpuinfo| grep "processor" | wc -l)
    info_cpu_mhz=$(cat /proc/cpuinfo | grep MHz|head -1|awk '{print $4}')
    info_mem_size=$(cat /proc/meminfo | grep MemTotal | awk '{print $2/1024/1024}')
    info_disk_size=$(df | grep '/$' | awk '{print int($2*1/1024/1024)}')

    echo " ===== [BASIC INFO] ===== "
    printf "%-15s %-20s\n" " - Host name" ": $info_name"
    printf "%-15s %-20s\n" " - OS version" ": $info_version"
    printf "%-15s %-20s\n" " - CPU info" ": $info_cpu_core Core / $info_cpu_mhz MHz"
    printf "%-15s %-20s\n" " - MEM size" ": $info_mem_size GB"
    printf "%-15s %-20s\n" " - DISK size" ": $info_disk_size GB"
    echo " ======================== "
    echo ""

    echo "[1] Check and make basic dir "
    echo "
    /usr/local/kubeiaas
      - /workdir
        - /checker
        - /src
        - /log
      - /data
        - /images
        - /sys-volumes
        - /data-volumes
      - /libvirt
      - /vnc
        - /token
      - /mysql
        - /data
        - /cnf
      - /sqlite
    "

    check_and_make_dir workdir/src
    check_and_make_dir workdir/checker
    check_and_make_dir workdir/log

    check_and_make_dir data/images
    check_and_make_dir data/sys-volumes
    check_and_make_dir data/data-volumes

    check_and_make_dir libvirt

    check_and_make_dir vnc/token

    check_and_make_dir mysql/data
    check_and_make_dir mysql/cnf

    check_and_make_dir sqlite

    echo ">>> success"
    echo -e "dir=success" | tee /usr/local/kubeiaas/workdir/log/prepare_result.log
}

# --------------------------------------------------

echo ""
echo "# ========================== #"
echo "#   KubeIaaS - Env Processor #"
echo "#   @ dir-processor          #"
echo "# ========================== #"
echo "$(date +%Y-%m-%d\ %H:%M:%S)"

echo ""
main
echo ""