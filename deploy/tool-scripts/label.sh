#!/bin/bash

echo ""
echo "# =========================== #"
echo "#    KubeIaaS @ free4inno     #"
echo "# =========================== #"
echo ""

# 读取用户输入并存储在变量 input 中
input=$(cat ../iaas-config.yml)

echo "请使用如下命令标记您的 Kubernetes 节点"
echo "---"

# 将输入的每一行处理为一个键值对
while read -r line; do
  # 使用正则表达式解析输入的 key 和 value
  key=$(echo "$line" | grep -oP '^\s*\K[\w.]+(?=\s*:)')
  values=$(echo "$line" | grep -oP ':\s*"\K[^"]+')

  # 根据输入的 key 生成相应的标签
  case "$key" in
    "nodes.work.list")
      label="kubeiaas/node"
      ;;
    "nodes.image")
      label="kubeiaas/img"
      ;;
    "nodes.control")
      label="kubeiaas/ctl"
      ;;
    "nodes.vnc")
      label="kubeiaas/vnc"
      ;;
    *)
      label=""
      ;;
  esac

  # 如果有相应的标签，则生成 kubectl label 命令并输出
  if [ -n "$label" ]; then
    IFS=','
    for node in $values; do
      node=$(echo "$node" | xargs) # 移除两边的空白字符
      echo "kubectl label node $node $label=true;"
    done
  fi
done <<< "$input"