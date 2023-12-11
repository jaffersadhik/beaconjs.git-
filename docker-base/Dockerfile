FROM jboss/wildfly:25.0.0.Final
ENV TZ="Asia/Kolkata"
EXPOSE 8080/tcp

COPY ./standalone.xml /opt/jboss/wildfly/standalone/configuration/standalone.xml

COPY ./target/wildfly/modules/system/layers/base/org/mariadb/jdbc  /opt/jboss/wildfly/modules/system/layers/base/org/mariadb/jdbc

COPY ./target/wildfly/modules/system/layers/base/redis/clients/jedis  /opt/jboss/wildfly/modules/system/layers/base/redis/clients/jedis

COPY ./target/wildfly/modules/system/layers/base/org/slf4j  /opt/jboss/wildfly/modules/system/layers/base/org/slf4j

COPY ./target/wildfly/modules/system/layers/base/org/eclipse  /opt/jboss/wildfly/modules/system/layers/base/org/eclipse

COPY ./target/wildfly/modules/system/layers/base/org/apache/commons/pool2  /opt/jboss/wildfly/modules/system/layers/base/org/apache/commons/pool2
COPY ./target/wildfly/modules/system/layers/base/org/apache/commons/dbcp2  /opt/jboss/wildfly/modules/system/layers/base/org/apache/commons/dbcp2
COPY ./target/wildfly/modules/system/layers/base/org/apache/commons/lang  /opt/jboss/wildfly/modules/system/layers/base/org/apache/commons/lang
COPY ./target/wildfly/modules/system/layers/base/org/apache/commons/configuration  /opt/jboss/wildfly/modules/system/layers/base/org/apache/commons/configuration
COPY ./target/wildfly/modules/system/layers/base/io/kubernetes  /opt/jboss/wildfly/modules/system/layers/base/io/kubernetes
COPY ./target/wildfly/modules/system/layers/base/com/itextos  /opt/jboss/wildfly/modules/system/layers/base/com/itextos
VOLUME ["/logs"]
CMD ["/opt/jboss/wildfly/bin/standalone.sh","-b","0.0.0.0","-Dprometheus.jetty.port=1075","-Dcommon.property.file.location=/platform/properties/bulk/global.properties"]