#!/bin/bash

nohup java -jar /root/iaas-agent-proxy-1.0-SNAPSHOT.jar > /workdir/iaas-agent-proxy.log 2>&1 &

cp /root/iaas-agent-0.0.1-SNAPSHOT.jar /workdir
nsenter --mount=/host/proc/1/ns/mnt sh -c "nohup java -jar /workdir/iaas-agent-0.0.1-SNAPSHOT.jar > /workdir/iaas-agent.log 2>&1 &"
tail -f /workdir/iaas-agent.log
