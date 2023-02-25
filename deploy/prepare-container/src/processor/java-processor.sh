#!/bin/bash

# ============================
#   KubeIaaS - Env Processor
#   @ java-processor
# ============================
# Author:   free4inno
# Date:     2022-09-24
#
# java-processor is used to check Java8 on host.
#
# Params:
#
# Example:
#
#   sh java-processor.sh
#

# ----------------------- Main -----------------------

function main(){
    echo "[-] Check host Java8 env "

    echo " - Java8 check result: "
    result=$(java -version 2>&1 >/dev/null | grep 'version')
    echo "---"
    echo $result
    echo "---"

    if [[ $result =~ 1.8 ]]; then
      echo " - Java8 is already installed. OK"
      echo ">>> success"
      echo -e "java=success" | tee -a /usr/local/kubeiaas/workdir/log/prepare_result.log
      echo ""
      exit
    else
      echo " - Java8 is not found."
      echo "KubeIaaS need Java8 on your host machine. Please restart iaas-agent after installed Java8!"
      echo ">>> failed"
      echo -e "java=failed" | tee -a /usr/local/kubeiaas/workdir/log/prepare_result.log
      echo ""
      exit
    fi
}

# --------------------------------------------------

echo ""
echo "# ========================== #"
echo "#   KubeIaaS - Env Processor #"
echo "#   @ java-processor         #"
echo "# ========================== #"
echo "$(date +%Y-%m-%d\ %H:%M:%S)"

echo ""
main
echo ""