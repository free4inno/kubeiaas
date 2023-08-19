FROM openjdk:8-jre-alpine

ADD ./target/image-operator-1.1.0-SNAPSHOT.jar /root/image-operator.jar

# ----- Package -----
# docker buildx build --platform linux/amd64 -t registry.cn-beijing.aliyuncs.com/kubeiaas/image-operator:1.1.0-SNAPSHOT . --push