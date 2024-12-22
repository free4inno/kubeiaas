FROM openjdk:8-jre-alpine

ADD ./target/resource-operator-1.1.0-SNAPSHOT.jar /root/resource-operator.jar

# ----- Package -----
# docker buildx build --platform linux/amd64 -t registry.cn-beijing.aliyuncs.com/kubeiaas/resource-operator:1.1.0-SNAPSHOT . --push
