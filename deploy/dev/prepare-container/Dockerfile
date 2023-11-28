FROM busybox:stable

RUN mkdir -p /workdir/ \
    && mkdir -p /checker/

COPY src/prepare.sh /workdir/
COPY ./src/processor/* /workdir/

RUN chmod 755 /workdir/*

ENTRYPOINT ["/bin/sh", "/workdir/prepare.sh"]

# ----- Package -----
# docker build -f ./Dockerfile -t registry.cn-beijing.aliyuncs.com/kubeiaas/iaas-prepare:1.1.0-SNAPSHOT .
# docker push registry.cn-beijing.aliyuncs.com/kubeiaas/iaas-prepare:1.1.0-SNAPSHOT
# ---
# docker buildx build --platform linux/amd64 -t registry.cn-beijing.aliyuncs.com/kubeiaas/iaas-prepare:1.1.0-SNAPSHOT . --push