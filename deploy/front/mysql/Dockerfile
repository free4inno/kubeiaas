FROM mysql:8.0.27

COPY ./iaas-front-init.sql /docker-entrypoint-initdb.d

# docker build -t registry.cn-beijing.aliyuncs.com/kubeiaas/iaas-front-mysql:1.0-SNAPSHOT .
# docker push registry.cn-beijing.aliyuncs.com/kubeiaas/iaas-front-mysql:1.0-SNAPSHOT