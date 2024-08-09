FROM openjdk:17-jdk-slim
ENV TZ="Asia/Kolkata"
EXPOSE 20000/tcp

COPY payload-params.properties /payload-params.properties
COPY messageremove.properties /messageremove.properties
COPY mcc-mnc-list.json /mcc-mnc-list.json
COPY ./target/mainmodule-1.0-executable.jar /mainmodule-1.0-executable.jar
ENTRYPOINT ["java","-Dprometheus.jetty.port=20000","-Dcommon.property.file.location=/instance/global.properties","-Dlog4j.configurationFile=file:/instance/log4j2-common.xml","-jar","/mainmodule-1.0-executable.jar"]
VOLUME ["/instance"]
VOLUME ["/logs"]
