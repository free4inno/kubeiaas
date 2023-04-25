FROM busybox:stable

RUN mkdir -p /workdir/ \
    && mkdir -p /checker/

COPY src/prepare.sh /workdir/
COPY ./src/processor/* /workdir/

RUN chmod 755 /workdir/*

ENTRYPOINT ["/bin/sh", "/workdir/prepare.sh"]

# docker build -f ./Dockerfile -t registry.cn-beijing.aliyuncs.com/kubeiaas/iaas-prepare:0.1 .
# docker push registry.cn-beijing.aliyuncs.com/kubeiaas/iaas-prepare:0.1