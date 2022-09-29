#!/bin/bash

# ============================
#   KubeIaaS - Env Checker
#   @ dir-checker
# ============================
# Author:   free4inno
# Date:     2022-09-24
#
# dir-checker is used to check KubeIaaS series directories.
#
# Params:
#
# Example:
#
#   sh dir-checker.sh
#

function check_and_make_dir(){
    if [[ -e /usr/local/kubeiaas/$1 || -d /usr/local/kubeiaas/$1 ]]; then
        echo " - /usr/local/kubeiaas/$1 already exist."
    else
        mkdir -p /usr/local/kubeiaas/$1
        echo " + mkdir -p /usr/local/kubeiaas/$1"
    fi
}

# ----------------------- Main -----------------------

function main(){
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

    echo ">>> success"
    echo -e "result=success" | tee /usr/local/kubeiaas/workdir/log/checkResult-dir.log
}

# --------------------------------------------------

echo ""
echo "# ========================== #"
echo "#   KubeIaaS - Env Checker   #"
echo "#   @ dir-checker            #"
echo "# ========================== #"
echo $(date +%Y-%m-%d\ %H:%M:%S)

echo -e "result=unknown" | tee /usr/local/kubeiaas/workdir/log/checkResult-dir.log

echo ""
main
echo ""