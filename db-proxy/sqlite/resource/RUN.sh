#!/bin/bash

echo ""
echo "# ================================ #"
echo "#    KubeIaaS @ db-proxy-sqlite    #"
echo "# ================================ #"
echo ""

# 1. prepare db ---------------------
echo "[1/2] check and prepare init to /db "

res=$(ls /db | grep kubeiaas.db)
if [[ -z $res ]]; then
    echo " - no kubeiaas.db found in /db"
    echo "...copy kubeiaas-init.db to /db"
    cp /root/kubeiaas-init.db /db/kubeiaas.db
else
    echo " - kubeiaas.db founded in /db"
    echo "...ok, ready"
fi

# 2. RUN agent-proxy in container -----------------
echo "[2/2] RUN db-proxy in container "
java -jar /root/db-proxy.jar