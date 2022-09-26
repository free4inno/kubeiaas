#!/bin/bash

# ============================
#   KubeIaaS - Env Checker
#   @ vnc-checker
# ============================
# Author:   free4inno
# Date:     2022-09-22
#
# In order to provide a web VNC client,
# vnc-checker is used to check and install NoVNC & Websockify.
#
# Params:
#
#
# Example:
#
# 

# ----------------------- Main -----------------------

function main(){

    # 0. check python3 install
    echo "[0] check Python3 installation"
    result=$(python3 --version)
    if [[ $result =~ "Python" ]]; then
        echo $result
        echo "[-] Python3 is already installed."
    else
        echo "[-] Python3 is not installed."
        echo "[-] ...start to install python3 (use yum)"

        # check yum repo
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
        yum install -y python3
    fi

    result=$(python3 --version)
    if [[ $result =~ "Python" ]]; then
        echo $result
        echo "[-] Python3 is successfully installed."
    else
        echo "[-] Python3 install failed!"
        echo ">>> failed"
        echo ""
        exit
    fi

    # 1. check websockify install
    echo "[1] check Websockify installation"
    if [ -f /usr/local/kubeiaas/vnc/websockify-0.10.0/run ]; then
        # exist
        echo "[-] Websockify is already installed."
    else
        # install
        echo "[-] Websockify is not installed."
        echo "[-] ...start to install Websockify"

        tar -xzvf /usr/local/kubeiaas/workdir/src/websockify-0.10.0.tar.gz -C /usr/local/kubeiaas/vnc/
        (cd /usr/local/kubeiaas/vnc/websockify-0.10.0; python3 setup.py install)
        echo "[-] Websockify is installed at '/usr/local/kubeiaas/vnc/websockify-0.10.0'."
    fi

    # 2. check novnc install
    echo "[2] check noVNC installation"
    if [ -f /usr/local/kubeiaas/vnc/noVNC-1.3.0/vnc.html ]; then
        # exist
        echo "[-] noVNC is already installed."
    else
        # install
        echo "[-] noVNC is not installed."
        echo "[-] ...start to install noVNC"

        tar -xzvf /usr/local/kubeiaas/workdir/src/noVNC-1.3.0.tar.gz -C /usr/local/kubeiaas/vnc/
        echo "[-] noVNC is installed at '/usr/local/kubeiaas/vnc/noVNC-1.3.0'."

        touch /usr/local/kubeiaas/vnc/token/token.conf
        echo "[-] touch config file at '/usr/local/kubeiaas/vnc/token/token.conf'."
    fi

    # 3. check vnc status
    echo "[3] check noVNC status"
    res=$(ps -ef | grep noVNC)
    if [[ $res =~ "websockify" ]]; then
        # running
        echo $res
        echo "[-] noVNC is already active (port: 8787)."
        echo ">>> success"
        echo ""
        exit
    else
        echo "[-] noVNC is not ready..."
        # run
        echo "[-] run noVNC (port: 8787)"
        python3 /usr/local/kubeiaas/vnc/websockify-0.10.0/websockify -D --web=/usr/local/kubeiaas/vnc/noVNC-1.3.0/ 8787 --target-config=/usr/local/kubeiaas/vnc/token/token.conf
    fi

    res=$(ps -ef | grep noVNC)
    if [[ $res =~ "websockify" ]]; then
        # running
        echo $res
        echo "[-] novnc is now active (port: 8787)."
        echo ">>> success"
        echo ""
    else 
        echo $res
        echo "[-] novnc run failed!"
        echo ">>> failed"
        echo ""
    fi
}

# --------------------------------------------------

echo ""
echo "# ========================== #"
echo "#   KubeIaaS - Env Checker   #"
echo "#   @ vnc-checker            #"
echo "# ========================== #"

echo ""
main
echo ""