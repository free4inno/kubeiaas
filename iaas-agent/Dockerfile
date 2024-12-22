#FROM openjdk:8
#
#RUN mkdir -p /kubeiaas
#
#ADD ./target/iaas-agent-1.1.0-SNAPSHOT.jar /kubeiaas/iaas-agent.jar
#ADD ./iaas-agent-proxy/target/iaas-agent-proxy-1.0-SNAPSHOT.jar /kubeiaas/iaas-agent-proxy.jar
#
#COPY ./docker-resource/RUN.sh /kubeiaas/

# ----- Package -----
# docker buildx build --platform linux/amd64 -t registry.cn-beijing.aliyuncs.com/kubeiaas/iaas-agent:1.1.0-SNAPSHOT . --push

FROM openjdk:8-jdk-slim as build

ARG JAR_FILE=target/iaas-agent-1.1.0-SNAPSHOT.jar
ADD ${JAR_FILE} iaas-agent.jar
RUN mkdir -p target/extracted \
    && java -Djarmode=layertools -jar iaas-agent.jar extract --destination target/extracted

FROM openjdk:8-jdk-slim

ARG EXTRACTED=/target/extracted
ARG PORT=9090

#RUN mkdir /app \
#    && groupadd --system --gid 3000 spring \
#    && useradd --system --uid 1000 --shell /bin/false --gid spring spring \
#    && chown -R spring:spring /app
RUN mkdir /kubeiaas

WORKDIR /kubeiaas
USER root

RUN apt-get update && apt-get install -y curl

ADD ./iaas-agent-proxy/target/iaas-agent-proxy-1.0-SNAPSHOT.jar /kubeiaas/iaas-agent-proxy.jar
COPY ./docker-resource/RUN.sh /kubeiaas/

COPY --from=build ${EXTRACTED}/dependencies/ /kubeiaas
COPY --from=build ${EXTRACTED}/spring-boot-loader/ /kubeiaas
COPY --from=build ${EXTRACTED}/snapshot-dependencies/ /kubeiaas
COPY --from=build ${EXTRACTED}/application/ /kubeiaas

EXPOSE ${PORT}
#ENTRYPOINT ["java", "org.springframework.boot.loader.launch.JarLauncher"]


# reference: https://spring.io/guides/topicals/spring-boot-docker/
# reference: https://www.dsinnovators.com/blogs/java-container-with-docker
# build: docker build -t registry.cn-beijing.aliyuncs.com/hufu-dev/illm-portal:0.0.1-SNAPSHOT .
# build and push to aliyun: docker buildx build -t registry.cn-beijing.aliyuncs.com/hufu-dev/illm-portal:0.0.1-SNAPSHOT . --push