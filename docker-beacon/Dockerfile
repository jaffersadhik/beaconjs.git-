FROM openjdk:11-jre
ENV TZ="Asia/Kolkata"
EXPOSE 20000/tcp

 
COPY ./target/web-generichttpapi-1.0.war /web-generichttpapi-1.0.war
COPY ./target/platform-r3r-1.0.war /platform-r3r-1.0.war
COPY ./target/mainmodule-1.0-executable.jar /mainmodule-1.0-executable.jar
ENTRYPOINT ["java","-Dprometheus.jetty.port=20000","-Dcommon.property.file.location=/instance/global.properties","-Dlog4j.configurationFile=file:/instance/log4j2-common.xml","-jar","/mainmodule-1.0-executable.jar"]
VOLUME ["/instance"]
VOLUME ["/logs"]
