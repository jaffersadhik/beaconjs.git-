FROM openjdk:17-jdk-slim

# Set environment variables
ENV WILDFLY_VERSION 24.0.1.Final
ENV WILDFLY_HOME /opt/wildfly

# Download and install WildFly
RUN apt-get update && apt-get install -y curl && rm -rf /var/lib/apt/lists/*
RUN curl -O https://download.jboss.org/wildfly/$WILDFLY_VERSION/wildfly-$WILDFLY_VERSION.tar.gz
RUN tar xf wildfly-$WILDFLY_VERSION.tar.gz -C /opt
RUN mv /opt/wildfly-$WILDFLY_VERSION $WILDFLY_HOME
RUN rm wildfly-$WILDFLY_VERSION.tar.gz

# Expose the ports needed by WildFly
EXPOSE 8080 9990
COPY ./target/docker-beaconweb-1.0.ear /opt/wildfly/standalone/deployments/docker-beaconweb-1.0.ear
VOLUME ["/logs"]
CMD ["/opt/wildfly/bin/standalone.sh","-b","0.0.0.0","-Dprometheus.jetty.port=1075","-Dcommon.property.file.location=/platform/properties/bulk/global.properties"]