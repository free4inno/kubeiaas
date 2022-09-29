#!/bin/bash

# ============================
#   KubeIaaS - Env Checker
#   @ dhcp-checker
# ============================
# Author:   free4inno
# Date:     2022-09-20
#
# dhcp-checker is used to check dhcpd env and setup config.
#
# Params:
#
#   -b : A usable Linux bridge name
#   -s : Network subnet's ip
#   -m : Network subnet's netmask
#   -g : Network Gateway ip
#
# Example:
#
#   /bin/bash dhcp-checker.sh -b br0 -s 192.168.32.0 -m 255.255.224.0 -g 129.168.32.1
#


# ----------------------- Function -----------------------

# Check system service's installation.
# param：
#   - service_name
# return：
#   - is_install (0-n, 1-y)
function is_service_install(){
    result=$(systemctl status $1 2>&1)
    if [[ $result =~ "could not be found" || $result =~ "Loaded: not-found" ]]; then
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
    result=$(systemctl status $1)
    if [[ $result =~ (dead) || $result =~ "Active: inactive" ]]; then
        echo "0"
    elif [[ $result =~ "Active: active" ]]; then
        echo "1"
    else
        echo "0"
    fi
}

# ----------------------- Main -----------------------

function main(){

    SERVICE_DHCPD=dhcpd

    # 1. check dhcpd install =============
    echo "[1] check dhcpd installation"
    res=$(is_service_install $SERVICE_DHCPD)
    if [ $res == "1" ]; then
        # --- dhcpd founded ---
        echo "[-] dhcpd is founded."
    else
        # --- dhcpd not found ---
        echo "[-] dhcpd could not be found!"
        echo "[+] start to install dhcpd..."

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
        yum install dhcp -y

        # write dhcpd.conf
        cp /etc/dhcp/dhcpd.conf /etc/dhcp/dhcpd.conf.bak
        echo "[-] dhcpd.conf backup to '/etc/dhcp/dhcpd.conf.bak'"
        
        echo "[+] begin to write dhcpd.conf..."
        echo -e "
deny unknown-clients;
shared-network "$KUBEIAAS_NETWORK_BRIDGE" {
    interface $KUBEIAAS_NETWORK_BRIDGE;
    subnet $KUBEIAAS_NETWORK_SUBNET netmask $KUBEIAAS_NETWORK_NETMASK {
        option routers $KUBEIAAS_NETWORK_GATEWAY;
    }
}\n" | tee -a /etc/dhcp/dhcpd.conf

    fi

    # 2. check dhcpd active =============
    echo "[2] check dhcpd status"
    res=$(is_service_active $SERVICE_DHCPD)
    if [ $res == "1" ]; then
        echo "[-] dhcpd is active."
    else
        echo "[-] dhcpd have not started!"
        echo "[+] try to start dhcpd..."

        service dhcpd start
    fi

    res=$(is_service_active $SERVICE_DHCPD)
    if [ $res == "1" ]; then
        echo "[-] dhcpd is active."
        echo ">>> success"
        echo -e "result=success" | tee /usr/local/kubeiaas/workdir/log/checkResult-dhcp.log
    else
        echo "[-] dhcpd have not started!"
        echo "[+] please check manually..."
        echo ">>> failed"
        echo -e "result=failed" | tee /usr/local/kubeiaas/workdir/log/checkResult-dhcp.log
    fi
}

# --------------------------------------------------

echo ""
echo "# ========================== #"
echo "#   KubeIaaS - Env Checker   #"
echo "#   @ dhcp-checker           #"
echo "# ========================== #"
echo $(date +%Y-%m-%d\ %H:%M:%S)
echo ""

echo -e "result=unknown" | tee /usr/local/kubeiaas/workdir/log/checkResult-dhcp.log

KUBEIAAS_NETWORK_BRIDGE=""
KUBEIAAS_NETWORK_SUBNET=""
KUBEIAAS_NETWORK_NETMASK=""
KUBEIAAS_NETWORK_GATEWAY=""

while getopts ":b:s:m:g:" opt
do
    case $opt in
        b)
            echo "KUBEIAAS_NETWORK_BRIDGE: $OPTARG"
            KUBEIAAS_NETWORK_BRIDGE=$OPTARG
        ;;
        s)
            echo "KUBEIAAS_NETWORK_SUBNET: $OPTARG"
            KUBEIAAS_NETWORK_SUBNET=$OPTARG
        ;;
        m)
            echo "KUBEIAAS_NETWORK_NETMASK: $OPTARG"
            KUBEIAAS_NETWORK_NETMASK=$OPTARG
        ;;
        g)
            echo "KUBEIAAS_NETWORK_GATEWAY: $OPTARG"
            KUBEIAAS_NETWORK_GATEWAY=$OPTARG
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
main
echo ""