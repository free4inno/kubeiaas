FROM openjdk:8-jre-alpine

ADD ./target/iaas-core-1.1.0-SNAPSHOT.jar /root/iaas-core.jar

# ----- Package -----
# docker buildx build --platform linux/amd64 -t registry.cn-beijing.aliyuncs.com/kubeiaas/iaas-core:1.1.0-SNAPSHOT . --push