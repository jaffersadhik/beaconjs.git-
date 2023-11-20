FROM jboss/wildfly:25.0.0.Final
ENV TZ="Asia/Kolkata"
EXPOSE 8080/tcp

COPY ./target/platform-icweb-1.0.war /opt/jboss/wildfly/standalone/deployments/platform-icweb-1.0.war
COPY ./target/platform-vcweb-1.0.war /opt/jboss/wildfly/standalone/deployments/platform-vcweb-1.0.war
COPY ./target/platform-chweb-1.0.war /opt/jboss/wildfly/standalone/deployments/platform-chweb-1.0.war
COPY ./target/platform-rcweb-1.0.war /opt/jboss/wildfly/standalone/deployments/platform-rcweb-1.0.war
COPY ./target/platform-sbcvweb-1.0.war /opt/jboss/wildfly/standalone/deployments/platform-sbcvweb-1.0.war
VOLUME ["/logs"]
CMD ["/opt/jboss/wildfly/bin/standalone.sh","-b","0.0.0.0","-Dprometheus.jetty.port=1075","-Dcommon.property.file.location=/platform/properties/bulk/global.properties"]