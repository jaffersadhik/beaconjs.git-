FROM adoptopenjdk/openjdk11:ubi
ENV TZ="Asia/Kolkata"
EXPOSE 20000/tcp


COPY ./target/docker-ic-1.0-jar-with-dependencies-and-exclude-classes.jar /docker-ic-1.0-jar-with-dependencies-and-exclude-classes.jar
ENTRYPOINT ["java","-Dprometheus.jetty.port=20000","-Dcommon.property.file.location=/instance/global.properties","-Dlog4j.configurationFile=file:/instance/log4j2-common.xml","-cp","/docker-ic-1.0-jar-with-dependencies-and-exclude-classes.jar","com.itextos.beacon.App"]
VOLUME ["/instance"]
VOLUME ["/logs"]
