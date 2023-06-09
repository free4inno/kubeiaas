FROM free4inno/java-jdk8

RUN mkdir -p /workdir/
COPY iaas-front.war /workdir

# ------- MODIFY WAR -------
# spring.datasource.url=jdbc:mysql://front-mysql:3306/hufu-front?serverTimezone=Asia/Shanghai&useSSL=false&allowPublicKeyRetrieval=true
# spring.datasource.password=MYSQL@free
# --------------------------
# docker build -t registry.cn-beijing.aliyuncs.com/kubeiaas/front-web:1.1.0-SNAPSHOT .
# docker push registry.cn-beijing.aliyuncs.com/kubeiaas/front-web:1.1.0-SNAPSHOT