#!/bin/bash

# ============================
#   KubeIaaS - Env Processor
#   @ kvm-processor
# ============================
# Author:   free4inno
# Date:     2022-09-22
#
# kvm-processor is used to check kvm virtualization support for this machine.
#
# Params:
#
#
# Example:
#
#   /bin/bash kvm-processor.sh
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

# Output prepare log
function log(){
  cmd="sed -i '/""$1""/d' /usr/local/kubeiaas/workdir/log/prepare_result.log"
  eval "$cmd"
  echo -e "$1=$2" | tee -a /usr/local/kubeiaas/workdir/log/prepare_result.log
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
        log kvm failed
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
        log kvm failed
        echo ""
        exit
    fi

    echo ">>> success"
    log kvm success
}

# --------------------------------------------------

echo ""
echo "# ========================== #"
echo "#  KubeIaaS - Env Processor  #"
echo "#  @ kvm-processor           #"
echo "# ========================== #"
echo "$(date +%Y-%m-%d\ %H:%M:%S)"

echo ""
main
echo ""