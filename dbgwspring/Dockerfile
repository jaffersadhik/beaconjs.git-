FROM openjdk:17-jdk-slim
ENV TZ="Asia/Kolkata"
EXPOSE 20000/tcp

 
COPY ./target/dbgwspring-1.0-executable.jar /dbgwspring-1.0-executable.jar
ENTRYPOINT ["java","-Dprometheus.jetty.port=20000","-Dcommon.property.file.location=/instance/global.properties","-Dlog4j.configurationFile=file:/instance/log4j2-common.xml","-jar","/dbgwspring-1.0-executable.jar"]
VOLUME ["/instance"]
VOLUME ["/logs"]
