#!/bin/bash

echo ""
echo "# =========================== #"
echo "#     KubeIaaS @ prepare      #"
echo "# =========================== #"
echo ""
echo " - KubeIaaS is a virtual machine management suite for Kubernetes."
echo "[-] It realizes the management and control of virtual machine based on Kubernetes. "
echo "[-] In addition, the running of VMS is not affected by the container environment. "
echo "[-] We also realized the integration of scheduling operation and maintenance with Kubernetes cluster."
echo ""
echo " - Now let's begin to prepare basic environment for KubeIaaS host."
echo ""

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

# MOD 1: PROCESS ---------------------------
function do_process_mod() {
  # 1. Query Prepare Config ---------------------
  ## ============================================
  ## -- basic
  pre_dir=1

  ## -- compute
  pre_kvm=0
  pre_libvirt=0

  ## -- storage
  pre_nfs_mnt=0
  pre_nfs_address_img=""
  pre_nfs_address_datavol=""

  ## -- network
  pre_dhcp=0
  pre_dhcp_private_subnet=""
  pre_dhcp_private_netmask=""
  pre_dhcp_private_subnet=""
  pre_dhcp_private_netmask=""
  ## ============================================

  # Steps ---------------------
  steps_total=3
  steps_now=0
  echo " - Next, tell us your env demand, total ($steps_total) steps: "

  # KVM & LIBVIRT ---------------------
  steps_now=$(( steps_now + 1 ))
  echo "($steps_now/$steps_total) Do you need to setup KVM & LIBVIRT? (y/n)"
  read -r choice

  if [ "$choice" == "y" ]
  then
    pre_kvm=1
    pre_libvirt=1
  else
    pre_kvm=0
    pre_libvirt=0
  fi

  # NFS ---------------------
  steps_now=$(( steps_now + 1 ))
  echo "($steps_now/$steps_total) Do you need to setup NFS mount? (y/n)"
  read -r choice

  if [ "$choice" == "y" ]
  then
    pre_nfs_mnt=1

    # image address
    choice="n"
    while [ "$choice" != "y" ]
    do
      echo "($steps_now/$steps_total) Enter the [NFS ADDRESS] you want to mount the [IMAGE STORAGE]: (for example '192.168.0.1:/mnt/path')"
      read -r pre_nfs_address_img
      echo " - Mount IMAGE STORAGE to [$pre_nfs_address_img], right? (y/n)"
      read -r choice
    done

    # data volume address
    choice="n"
    while [ "$choice" != "y" ]
    do
      echo "($steps_now/$steps_total) Enter the [NFS ADDRESS] you want to mount the [DATA_VOLUME STORAGE]: (for example '192.168.0.1:/mnt/path')"
      read -r pre_nfs_address_datavol
      echo " - Mount DATA_VOLUME STORAGE to [$pre_nfs_address_datavol], right? (y/n)"
      read -r choice
    done
  else
    pre_nfs_mnt=0
  fi

  # DHCP ---------------------
  steps_now=$(( steps_now + 1 ))
  echo "($steps_now/$steps_total) Do you need to setup DHCP (for network node)? (y/n)"
  read -r choice

  if [ "$choice" == "y" ]
  then
    pre_dhcp=1

    # private
    choice="n"
    while [ "$choice" != "y" ]
    do
      echo "($steps_now/$steps_total) Enter the [subnet] of your [private] network: (for example '192.168.0.0')"
      read -r pre_dhcp_private_subnet
      echo "($steps_now/$steps_total) Enter the [netmask] of your [private] network: (for example '255.255.255.0')"
      read -r pre_dhcp_private_netmask
      echo " - Set private network subnet [$pre_dhcp_private_subnet] netmask [$pre_dhcp_private_netmask], right? (y/n)"
      read -r choice
    done

    # public
    choice="n"
    while [ "$choice" != "y" ]
    do
      echo "($steps_now/$steps_total) Enter the [subnet] of your [public] network: (for example '192.168.0.0')"
      read -r pre_dhcp_public_subnet
      echo "($steps_now/$steps_total) Enter the [netmask] of your [public] network: (for example '255.255.255.0')"
      read -r pre_dhcp_public_netmask
      echo " - Set public network subnet [$pre_dhcp_public_subnet] netmask [$pre_dhcp_public_netmask], right? (y/n)"
      read -r choice
    done
  else
    pre_dhcp=0
  fi
  echo ""

  ## ============================================

  # 2. Show list
  function show_pre_list() {
      if [ $1 -eq 1 ]
      then
        echo "AUTO"
      else
        echo "MANUAL"
      fi
  }

  echo " ===== [CONFIG PROCESS LIST] ===== "
  echo " ------------- Basic ------------- "
  printf "%-20s %-20s\n" " - Java 8" ": MANUAL"
  printf "%-20s %-20s\n" " - OVS & Bridge" ": MANUAL"
  printf "%-20s %-20s\n" " - Sys Directory" ": $(show_pre_list $pre_dir)"

  echo " ------------ Compute ------------ "
  printf "%-20s %-20s\n" " - KVM" ": $(show_pre_list $pre_kvm)"
  printf "%-20s %-20s\n" " - Libvirt" ": $(show_pre_list $pre_libvirt)"

  echo " ------------ Stroage ------------ "
  printf "%-20s %-20s\n" " - NFS Mount" ": $(show_pre_list $pre_nfs_mnt)"
  if [ $pre_nfs_mnt -eq 1 ]
  then
    printf "%-20s %-20s %-20s\n" " --> " "image mount" ": $pre_nfs_address_img"
    printf "%-20s %-20s %-20s\n" " --> " "data volume mount" ": $pre_nfs_address_datavol"
  fi

  echo " ------------ Network ------------ "
  printf "%-20s %-20s\n" " - DHCP " ": $(show_pre_list $pre_dhcp)"
  if [ $pre_dhcp -eq 1 ]
  then
    printf "%-20s %-20s %-20s\n" " --> " "private subnet" ": $pre_dhcp_private_subnet"
    printf "%-20s %-20s %-20s\n" " --> " "private netmask" ": $pre_dhcp_private_subnet"
    printf "%-20s %-20s %-20s\n" " --> " "public subnet" ": $pre_dhcp_public_subnet"
    printf "%-20s %-20s %-20s\n" " --> " "public netmask" ": $pre_dhcp_public_subnet"
  fi

  echo " ================================= "
  echo ""

  # Confirm and RUN Process -------------------
  echo " - Confirm above configuration? (y/n)"
  read -r choice

  if [ "$choice" == "y" ]
  then
    echo "[-] Process Start..."

    # **** Sys Directory ****
    if [ $pre_dir -eq 1 ]
    then
      echo "[+] ******** Sys Directory ********"
      source ./processor/dir-processor.sh
    fi

    # **** KVM & LIBVIRT ****
    if [ $pre_kvm -eq 1 ]
    then
      echo "[+] ******** KVM ********"
      source ./processor/kvm-processor.sh
    fi
    if [ $pre_libvirt -eq 1 ]
    then
      echo "[+] ******** LIBVIRT ********"
      source ./processor/libvirt-processor.sh
    fi

    # **** NFS mount ****
    if [ $pre_nfs_mnt -eq 1 ]
    then
      echo "[+] ******** NFS mount ********"
      source ./processor/mnt-processor.sh -i "$pre_nfs_address_img" -d "$pre_nfs_address_datavol"
    fi

    # **** DHCP ****
    if [ $pre_dhcp -eq 1 ]
    then
      echo "[+] ******** DHCP ********"
      source ./processor/dhcp-processor.sh -n "$pre_dhcp_private_subnet" -m "$pre_dhcp_private_netmask" -p "$pre_dhcp_public_subnet" -q "$pre_dhcp_public_netmask"
    fi

  else
    echo "[x] Process Cancel..."
  fi
}

# MOD 2: CHECK -----------------------------
function do_check_mod() {
  echo "Sorry, currently unavailable, soon release..."
}

# 1. Select mod ------------------------------
echo "[+] Please select the prepare mod: "
echo "<1> PROCESS MOD : automated configuration environment (ATTENTION: Partial function only support CentOS 7.X)"
echo "<2> CHECK MOD   : check environment availability"
echo "<3> QUIT"
echo " - Enter the <NUMBER> to set: "
stty erase ^h
read -r mod_num

case $mod_num in
  1)
    echo "[-] PROCESS MOD selected..."
    do_process_mod
    ;;
  2)
    echo "[-] CHECK MOD selected..."
    do_check_mod
    ;;
  3)
    echo "[-] Bye"
    ;;
  *)
    echo "[-] Illegal number, quit."
    ;;
esac








