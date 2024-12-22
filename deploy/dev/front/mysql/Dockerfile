FROM mysql:8.0.27

COPY iaas-front-init.sql /docker-entrypoint-initdb.d

# ----- Package -----
# docker build -t registry.cn-beijing.aliyuncs.com/kubeiaas/front-mysql:1.1.0-SNAPSHOT .
# docker push registry.cn-beijing.aliyuncs.com/kubeiaas/front-mysql:1.1.0-SNAPSHOT
# ---
# docker buildx build --platform linux/amd64 -t registry.cn-beijing.aliyuncs.com/kubeiaas/front-mysql:1.1.0-SNAPSHOT . --push