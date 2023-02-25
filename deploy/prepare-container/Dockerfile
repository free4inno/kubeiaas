FROM busybox:stable

RUN mkdir -p /workdir/ \
    && mkdir -p /checker/

COPY src/prepare.sh /workdir/
COPY ./src/processor/* /workdir/

RUN chmod 755 /workdir/*

ENTRYPOINT ["/bin/sh", "/workdir/prepare.sh"]
