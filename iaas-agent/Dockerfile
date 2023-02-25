FROM openjdk:8

RUN mkdir -p /kubeiaas

ADD ./target/iaas-agent-0.1.1-SNAPSHOT.jar /kubeiaas/iaas-agent.jar
ADD ./iaas-agent-proxy/target/iaas-agent-proxy-1.0-SNAPSHOT.jar /kubeiaas/iaas-agent-proxy.jar

COPY ./docker-resource/RUN.sh /kubeiaas/
