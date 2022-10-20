FROM busybox:stable

RUN mkdir -p /workdir/ \
    && mkdir -p /checker/

COPY ./resource/* /workdir/

RUN chmod 755 /workdir/*

ENTRYPOINT ["/workdir/INIT.sh"]
