#!/bin/bash

# ============================
#   KubeIaaS - Env Checker
#   @ kvm-checker
# ============================
# Author:   free4inno
# Date:     2022-09-22
#
# kvm-checker is used to check kvm virtualization support for this machine.
#
# Params:
#
#
# Example:
#
#   /bin/bash kvm-checker.sh
#


# ----------------------- Function -----------------------

# Check cpu's kvm support
# return：
#   - is_support (0-n, 1-y)
function is_cpuinfo_kvm(){
    result=$(cat /proc/cpuinfo | egrep 'vmx|svm')
    if [[ -z $result ]]; then
        echo "0"
    else
        echo "1"
    fi
}


# Check lsmod's kvm loaded
# return：
#   - is_loaded (0-n, 1-y)
function is_lsmod_loaded(){
    result=$(lsmod | grep kvm)
    if [[ -z $result ]]; then
        echo "0"
    else
        echo "1"
    fi
}

# ----------------------- Main -----------------------

function main(){

    # 1. check cpuinfo
    echo "[1] Check cpu's kvm support"
    res=$(is_cpuinfo_kvm)
    if [ $res == "1" ]; then
        echo "[-] cpu support kvm."
    else
        echo "[-] cpu unsupport kvm!"
        echo ">>> failed"
        echo -e "result=failed" | tee /usr/local/kubeiaas/workdir/log/checkResult-kvm.log
        echo ""
        exit
    fi

    # 2. check lsmod
    echo "[2] Check lsmod's kvm loaded"
    res=$(is_lsmod_loaded)
    if [ $res == "1" ]; then
        echo "[-] lsmod loaded kvm."
    else
        echo "[-] cpu unsupport kvm!"
        echo ">>> failed"
        echo -e "result=failed" | tee /usr/local/kubeiaas/workdir/log/checkResult-kvm.log
        echo ""
        exit
    fi

    echo ">>> success"
    echo -e "result=success" | tee /usr/local/kubeiaas/workdir/log/checkResult-kvm.log
}

# --------------------------------------------------

echo ""
echo "# ========================== #"
echo "#   KubeIaaS - Env Checker   #"
echo "#   @ kvm-checker            #"
echo "# ========================== #"
echo $(date +%Y-%m-%d\ %H:%M:%S)

echo -e "result=unknown" | tee /usr/local/kubeiaas/workdir/log/checkResult-kvm.log

echo ""
main
echo ""