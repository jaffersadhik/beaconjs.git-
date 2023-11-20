FROM jboss/wildfly:25.0.0.Final
ENV TZ="Asia/Kolkata"
EXPOSE 8080/tcp

COPY ./target/wildfly/modules/system/layers/base/com/itextos  /opt/jboss/wildfly/modules/system/layers/base/com/itextos
VOLUME ["/logs"]
CMD ["/opt/jboss/wildfly/bin/standalone.sh","-b","0.0.0.0","-Dprometheus.jetty.port=1075","-Dcommon.property.file.location=/platform/properties/bulk/global.properties"]