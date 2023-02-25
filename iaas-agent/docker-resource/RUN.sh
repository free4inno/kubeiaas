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

# 1. prepare files to workdir ---------------------
echo "[1/4] prepare files to /workdir "

echo " - cp iaas-agent jar"
cp /kubeiaas/iaas-agent.jar /workdir/src/

# 2. wait for DB-proxy ---------------------
echo "[2/4] loop wait for DB-proxy "
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

# 3. RUN agent-proxy in container -----------------
echo "[3/4] RUN agent-proxy in container "
nohup java -jar /kubeiaas/iaas-agent-proxy.jar > /workdir/log/iaas-agent-proxy.log 2>&1 &

# 4. RUN agent on host ----------------------------
echo "[4/4] RUN agent on host "
host_sh "nohup java -jar /usr/local/kubeiaas/workdir/src/iaas-agent.jar > /usr/local/kubeiaas/workdir/log/iaas-agent.log 2>&1 &"
tail -f /workdir/log/iaas-agent.log
