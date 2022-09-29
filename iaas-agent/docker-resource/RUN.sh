#!/bin/bash

echo ""
echo "# =========================== #"
echo "#    KubeIaaS @ iaas-agent    #"
echo "# =========================== #"
echo ""

# use host sh to run command
function host_sh(){
  res=$(nsenter --mount=/host/proc/1/ns/mnt sh -c "$1")
  echo "$res"
}

# 0. Check host Java8 env ---------------------
echo "[0] Check host Java8 env "

echo " - Java8 check result: "
result=$(host_sh "java -version 2>&1 >/dev/null | grep 'version'")
echo "---"
echo $result
echo "---"

if [[ $result =~ 1.8 ]]; then
  echo " - Java8 is already installed. OK"
else
  echo " - Java8 is not found."
  echo "KubeIaaS need Java8 on your host machine. Please restart iaas-agent after installed Java8!"
  echo ">>> failed"
  echo ""
  exit
fi

# 1. Check and make basic dir -----------------
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

function check_and_make_dir(){
  res=$(host_sh "if [ -e /usr/local/kubeiaas/$1 ]; then echo '1'; else echo '0'; fi;")
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

echo ""

# 2. prepare files to workdir ---------------------
echo "[2] prepare files to /workdir "

echo " - cp iaas-agent jar"
cp /kubeiaas/iaas-agent.jar /workdir/src/

echo " - cp src/*"
cp -r /kubeiaas/src/* /workdir/src/

echo " - cp checker/*"
cp -r /kubeiaas/checker/* /workdir/checker/
chmod 755 /workdir/checker/*

# 3. wait for DB-proxy ---------------------
echo "[3] loop wait for DB-proxy "
count=0
while [[ ! $(curl http://db-proxy:9091/heartbeat) =~ heartbeat ]]
do
  sleep 5
  count=$((count+1))
  echo "... try times $count"
  if [ $count -ge 60 ]; then
    echo " - wait for db-proxy timeout! exit."
    exit
  fi
done
echo " - db-proxy is ready."

# 4. RUN agent-proxy in container -----------------
echo "[4] RUN agent-proxy in container "
nohup java -jar /kubeiaas/iaas-agent-proxy.jar > /workdir/log/iaas-agent-proxy.log 2>&1 &

# 5. RUN agent on host ----------------------------
echo "[5] RUN agent on host "
host_sh "nohup java -jar /usr/local/kubeiaas/workdir/src/iaas-agent.jar > /usr/local/kubeiaas/workdir/log/iaas-agent.log 2>&1 &"
tail -f /workdir/log/iaas-agent.log
