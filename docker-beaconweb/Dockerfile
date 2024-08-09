FROM quay.io/wildfly/wildfly:latest-jdk17
ENV TZ="Asia/Kolkata"

EXPOSE 8080 9990

COPY standalone.xml /opt/jboss/wildfly/standalone/configuration/standalone.xml

COPY payload-params.properties /payload-params.properties
COPY messageremove.properties /messageremove.properties

COPY ./target/docker-beaconweb-1.0.ear /opt/jboss/wildfly/standalone/deployments/docker-beaconweb-1.0.ear
VOLUME ["/logs"]
CMD ["/opt/jboss/wildfly/bin/standalone.sh","-b","0.0.0.0","-Dprometheus.jetty.port=1075","-Dcommon.property.file.location=/platform/properties/bulk/global.properties"]