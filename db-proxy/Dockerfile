FROM free4inno/iaas-sqlite:1.0

# sqlite
RUN mkdir /db

#RUN apt-get -y update \
#  && apt-get -y upgrade \
#  && apt-get install -y sqlite3 libsqlite3-dev \
#  && /usr/bin/sqlite3 /db/kubeiaas.db

COPY ./sqlite/resource/kubeiaas-init.db /root/
COPY ./sqlite/resource/RUN.sh /root/

ADD ./target/db-proxy-1.0-RELEASE.jar /root/db-proxy.jar

ENTRYPOINT ["/bin/bash", "/root/RUN.sh"]
