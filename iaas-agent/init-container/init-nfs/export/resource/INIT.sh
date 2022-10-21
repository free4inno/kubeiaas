#!/bin/sh

echo ""
echo "# =========================== #"
echo "#        > init-nfs <         #"
echo "# =========================== #"
echo ""

# FUNCTION: use host sh to run command
function host_sh(){
  res=$(nsenter --mount=/host/proc/1/ns/mnt sh -c "$1")
  echo "$res"
}

# FUNCTION: get from env
function get_env(){
  env_name=$1
  eval value='$'$env_name
  echo "${value}"
}

# FUNCTION: parse_json
function parse_json(){
    echo "${1//\"/}" | sed "s/.*$2:\([^,}]*\).*/\1/"
}

# ------ 0. Get env config -----------------------------------
echo "[0] Check env config"

host_name=$(get_env HOST_NAME)
echo " - host_name:${host_name}"

json_data=$(wget -O - "http://db-proxy:9091/host/query_all_like_by_single_key?key_1=role&value_1=mnt")
value=`echo $json_data | sed s/[[:space:]]//g`
mnt_host_name=$(parse_json $value "name")
echo " - mnt_host_name:${mnt_host_name}"

#network_subnet_with_mask=$(get_env NETWORK_SUBNET_WITH_MASK)
#echo " - network_subnet_with_mask:${network_subnet_with_mask}"
#
#host_ip=$(get_env HOST_IP)
#mnt_host_ip=$(get_env MNT_HOST_IP)
#echo " - host_ip:${host_ip}"
#echo " - mnt_host_ip:${mnt_host_ip}"

# ------ 1. prepare files to host-workdir ---------------------
echo "[1] prepare files to /workdir "

echo " - cp mnt-export-checker.sh"
cp /workdir/mnt-export-checker.sh /checker/

chmod 755 /checker/*

# ------ 2. check and make mount dir --------------------------
echo "[2] Check and make mount dir"

function check_and_make_dir(){
  res=$(host_sh "if [[ -e /usr/local/kubeiaas/$1 || -d /usr/local/kubeiaas/$1 || \$(df -h) =~ /usr/local/kubeiaas/$1 ]]; then echo '1'; else echo '0'; fi;")
  if [ $res == "0" ]; then
    host_sh "mkdir -p /usr/local/kubeiaas/$1"
    echo " + mkdir -p /usr/local/kubeiaas/$1"
  else
    echo " - /usr/local/kubeiaas/$1 already exist."
  fi
}

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

# ------ 3. RUN init shell script ------------------------------
echo "[3] RUN init shell script"
if [[ $host_name == $mnt_host_name ]]; then
  echo " - this node is mnt node, run \`mnt-export-checker.sh\`"
  host_sh "./usr/local/kubeiaas/workdir/checker/mnt-export-checker.sh -m \*"
else
  echo " - this node is normal node. NO NEED TO RUN."
fi

while [ 1 ]
do
  sleep 60
done