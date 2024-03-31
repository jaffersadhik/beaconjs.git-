FROM quay.io/wildfly/wildfly:latest-jdk17
ENV TZ="Asia/Kolkata"

EXPOSE 8080 9990
COPY standalone.xml /opt/jboss/wildfly/standalone/configuration/standalone.xml
COPY com /opt/jboss/wildfly/modules/.
COPY ./target/thirdpartyjar-1.0.jar /opt/jboss/wildfly/modules/com/unitia/ejbserver/main/thirdpartyjar-1.0.jar

COPY ./target/unitia-ejbserver-1.0.jar /opt/jboss/wildfly/modules/com/unitia/ejbserver/main/unitia-ejbserver-1.0.jar
COPY ./target/unitia-ejbserver-1.0.jar /opt/jboss/wildfly/standalone/deployments/unitia-ejbserver-1.0.jar

VOLUME ["/logs"]
CMD ["/opt/jboss/wildfly/bin/standalone.sh","-b","0.0.0.0","-Dprometheus.jetty.port=1075","-Dcommon.property.file.location=/platform/properties/bulk/global.properties"]