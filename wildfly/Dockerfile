FROM openjdk:21-jdk-slim as build

# Install WildFly
FROM jboss/wildfly:21.0.0.Final

# Set Java 21 as the default Java
ENV JAVA_HOME=/usr/lib/jvm/java-21-openjdk
ENV PATH=$JAVA_HOME/bin:$PATH

