#!/bin/bash

# ============================
#   KubeIaaS - Env Processor
#   @ dhcp-processor
# ============================
# Author:   free4inno
# Date:     2022-09-20
#
# dhcp-processor is used to check dhcpd env and setup config.
#
# Params:
#
#   -n : private subnet
#   -m : private netmask
#   -p : public subnet
#   -q : public netmask
#
# Example:
#
#   /bin/bash dhcp-processor.sh -n 192.168.255.0 -m 255.255.255.0 -p 129.168.31.0 -q 255.255.255.0
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
        echo "[+] please check manually..."
        echo ">>> failed"

        log dhcp failed

    fi

    # 2. check dhcpd active =============
    echo "[2] check dhcpd status"
    res=$(is_service_active $SERVICE_DHCPD)
    if [ $res == "1" ]; then
        echo "[-] dhcpd is active."
    else
        echo "[-] dhcpd have not started!"
        echo "[+] try to start dhcpd..."

        # write default conf
        echo -e "
deny unknown-clients;
shared-network default {subnet 0.0.0.0 netmask 0.0.0.0 {}}
" | tee /etc/dhcp/dhcpd.conf

        service dhcpd start
    fi

    res=$(is_service_active $SERVICE_DHCPD)
    if [ $res == "1" ]; then
        echo "[-] dhcpd is active."
        echo ">>> success"

        log dhcp success
    else
        echo "[-] dhcpd have not started!"
        echo "[+] please check manually..."
        echo ">>> failed"

        log dhcp failed
    fi
}

# --------------------------------------------------

echo ""
echo "# ========================== #"
echo "#  KubeIaaS - Env Processor  #"
echo "#  @ dhcp-processor          #"
echo "# ========================== #"
echo "$(date +%Y-%m-%d\ %H:%M:%S)"
echo ""

#PRIVATE_SUBNET=""
#PRIVATE_NETMASK=""
#PUBLIC_SUBNET=""
#PUBLIC_NETMASK=""

#while getopts ":n:m:p:q:" opt
#do
#    case $opt in
#        n)
#            echo "PRIVATE_SUBNET: $OPTARG"
#            PRIVATE_SUBNET=$OPTARG
#        ;;
#        m)
#            echo "PRIVATE_NETMASK: $OPTARG"
#            PRIVATE_NETMASK=$OPTARG
#        ;;
#        p)
#            echo "PUBLIC_SUBNET: $OPTARG"
#            PUBLIC_SUBNET=$OPTARG
#        ;;
#        q)
#            echo "PUBLIC_NETMASK: $OPTARG"
#            PUBLIC_NETMASK=$OPTARG
#        ;;
#        *)
#            echo "unknown param: $opt"
#            echo ">>> failed"
#            echo ""
#            exit
#        ;;
#        ?)
#            echo "unknown param: $opt"
#            echo ">>> failed"
#            echo ""
#            exit
#        ;;
#    esac
#done

echo ""
main
echo ""