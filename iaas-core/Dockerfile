#FROM openjdk:8-jre-alpine
#
#ADD ./target/iaas-core-1.1.0-SNAPSHOT.jar /root/iaas-core.jar

# ----- Package -----
# docker buildx build --platform linux/amd64 -t registry.cn-beijing.aliyuncs.com/kubeiaas/iaas-core:1.1.0-SNAPSHOT . --push

FROM openjdk:8-jdk-slim as build

ARG JAR_FILE=target/iaas-core-1.1.0-SNAPSHOT.jar
ADD ${JAR_FILE} iaas-core.jar
RUN mkdir -p target/extracted \
    && java -Djarmode=layertools -jar iaas-core.jar extract --destination target/extracted

FROM openjdk:8-jdk-slim

ARG EXTRACTED=/target/extracted
ARG PORT=9080

#RUN mkdir /app \
#    && groupadd --system --gid 3000 spring \
#    && useradd --system --uid 1000 --shell /bin/false --gid spring spring \
#    && chown -R spring:spring /app
RUN mkdir /kubeiaas

WORKDIR /kubeiaas
USER root

COPY --from=build ${EXTRACTED}/dependencies/ /kubeiaas
COPY --from=build ${EXTRACTED}/spring-boot-loader/ /kubeiaas
COPY --from=build ${EXTRACTED}/snapshot-dependencies/ /kubeiaas
COPY --from=build ${EXTRACTED}/application/ /kubeiaas

EXPOSE ${PORT}

ENTRYPOINT ["java", "org.springframework.boot.loader.JarLauncher"]