FROM jboss/wildfly:25.0.0.Final
ENV TZ="Asia/Kolkata"
EXPOSE 8080/tcp

COPY ./standalone.xml /opt/jboss/wildfly/standalone/configuration/standalone.xml
COPY ./target/common-unittestweb-1.0.war /opt/jboss/wildfly/standalone/deployments/common-unittestweb-1.0.war

COPY ./target/wildfly/modules/system/layers/base/com/itextos  /opt/jboss/wildfly/modules/system/layers/base/com/itextos
VOLUME ["/logs"]
CMD ["/opt/jboss/wildfly/bin/standalone.sh","-b","0.0.0.0","-Dprometheus.jetty.port=1075","-Dcommon.property.file.location=/platform/properties/bulk/global.properties"]