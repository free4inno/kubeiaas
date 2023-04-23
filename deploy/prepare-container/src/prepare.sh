#!/bin/sh

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

# FUNCTION: use host sh to run command
host_sh(){
  res=$(nsenter --mount=/host/proc/1/ns/mnt sh -c "$1")
  echo "$res"
}

# FUNCTION: get from env
get_env(){
  env_name=$1
  eval value='$'$env_name
  echo "${value}"
}

# ------ 0. Get env config ------
echo "[0] Check env config"

# host name
host_name=$(get_env HOST_NAME)
echo " - host_name:${host_name}"

# host ip
host_ip=$(get_env HOST_IP)
echo " - host_ip:${host_ip}"

# dhcp node
dhcp_node=$(get_env DHCP_NODE)
echo " - dhcp_node:${dhcp_node}"

# mnt node
nfs_node=$(get_env NFS_NODE)
echo " - nfs_node:${nfs_node}"

# nfs_dir_img
nfs_dir_img=$(get_env NFS_DIR_IMG)
echo " - nfs_dir_img:${nfs_dir_img}"

# nfs_dir_dv
nfs_dir_dv=$(get_env NFS_DIR_DV)
echo " - nfs_dir_dv:${nfs_dir_dv}"

# ------ 1. prepare files to host-workdir ------
echo "[1] prepare files to /workdir "

echo " - cp dir-processor.sh"
cp /workdir/dir-processor.sh /checker/

echo " - cp java-processor.sh"
cp /workdir/java-processor.sh /checker/

echo " - cp kvm-processor.sh"
cp /workdir/kvm-processor.sh /checker/

echo " - cp libvirt-processor.sh"
cp /workdir/libvirt-processor.sh /checker/

echo " - cp dhcp-processor.sh"
cp /workdir/dhcp-processor.sh /checker/

echo " - cp mnt-processor.sh"
cp /workdir/mnt-processor.sh /checker/

chmod 755 /checker/*

# ------ 2. RUN init shell script ------------------------------
echo "[2] RUN init shell script"

echo "(1/6) prepare dir"
echo " - this node is work node -> PREPARE"
host_sh "sudo /bin/bash /usr/local/kubeiaas/workdir/checker/dir-processor.sh"

echo "(2/6) prepare java"
echo " - this node is work node -> PREPARE"
host_sh "sudo /bin/bash /usr/local/kubeiaas/workdir/checker/java-processor.sh"

echo "(3/6) prepare kvm"
echo " - this node is work node -> PREPARE"
host_sh "sudo /bin/bash /usr/local/kubeiaas/workdir/checker/kvm-processor.sh"

echo "(4/6) prepare libvirt"
echo " - this node is work node -> PREPARE"
host_sh "sudo /bin/bash /usr/local/kubeiaas/workdir/checker/libvirt-processor.sh"

echo "(5/6) prepare dhcp"
if [[ "$dhcp_node" == "$host_name" || "$dhcp_node" == "$host_ip" ]]; then
  echo " - this node is dhcp node -> PREPARE"
  host_sh "sudo /bin/bash /usr/local/kubeiaas/workdir/checker/dhcp-processor.sh"
else
  echo " - this node is not dhcp node -> NO NEED TO RUN."
fi

echo "(6/6) prepare mnt"
if [[ "$nfs_node" == "$host_name" || "$nfs_node" == "$host_ip" ]]; then
  echo " - this node is nfs node -> NO NEED TO RUN."
else
  echo " - this node is work node -> PREPARE"
  host_sh "sudo /bin/bash /usr/local/kubeiaas/workdir/checker/mnt-processor.sh -i $nfs_node:$nfs_dir_img -d $nfs_node:$nfs_dir_dv"
fi

# tail -f /dev/null