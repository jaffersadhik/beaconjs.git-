#FROM quay.io/wildfly/wildfly:latest-jdk21
#FROM dk-reg.mas:5000/wildfly/wildfly:latest-jdk21

# Use OpenJDK 21 as base image
FROM openjdk:21-jdk-slim

# Install necessary utilities
RUN apt-get update && apt-get install -y curl tar

RUN mkdir -p /opt/jboss/

# Download and install Tomcat
RUN curl -fsSL https://archive.apache.org/dist/tomcat/tomcat-9/v9.0.78/bin/apache-tomcat-9.0.78.tar.gz -o /tmp/tomcat.tar.gz
RUN mkdir /opt/tomcat 
RUN tar -xvzf /tmp/tomcat.tar.gz -C /opt/tomcat --strip-components=1 
RUN rm /tmp/tomcat.tar.gz
RUN mkdir -p /opt/jboss/wildfly/logs








ENV TZ="Asia/Kolkata"

COPY ./target/mainmodule-1.0-executable.jar /opt/tomcat/lib/

RUN rm -rf /opt/tomcat/webapps/ROOT

COPY ./target/web-generichttpapi-1.0.war /opt/tomcat/webapps/genericapi.war
COPY ./target/web-migrationapi-1.0.war /opt/tomcat/webapps/migrationapi.war
COPY ./target/platform-dnr-1.0.war /opt/tomcat/webapps/dnr.war
COPY ./target/platform-messagetoolweb-1.0.war /opt/tomcat/webapps/msgtool.war
COPY ./target/platform-prepaiduIweb-1.0.war /opt/tomcat/webapps/prepaiddata.war
COPY ./target/dlrquery-dlrqueryweb-1.0.war /opt/tomcat/webapps/dlrquery.war
COPY ./target/ch-clienthandoveracceptproject-1.0.war /opt/tomcat/webapps/clienthandoveraccept.war
COPY ./target/common-pwdencryptionapplication-1.0.war /opt/tomcat/webapps/password.war
COPY ./target/platform-r3r-1.0.war /opt/tomcat/webapps/ROOT.war
COPY ./target/common-datarefresherweb-1.0.war /opt/tomcat/webapps/datarefresher.war




#COPY standalone.xml /opt/jboss/wildfly/standalone/configuration/standalone.xml

COPY ./target/log4j2-common.xml /log4j2-common.xml

COPY ./target/smpp-interface.properties /smpp-interface.properties
COPY ./target/encrypt.properties /encrypt.properties

COPY ./target/elasticsearch.properties_cat /elasticsearch.properties_cat
COPY ./target/elasticsearch.properties_do1 /elasticsearch.properties_do1

COPY ./target/dnd.properties /dnd.properties
COPY ./target/dlttemplateconfig.properties /dlttemplateconfig.properties

COPY ./target/common.properties /common.properties
COPY ./target/common-db.properties_cat /common-db.properties_cat
COPY ./target/common-db.properties_do1 /common-db.properties_do1

COPY ./target/callback-params.properties /callback-params.properties
COPY ./target/payload-params.properties /payload-params.properties
COPY ./target/midconfig.properties /midconfig.properties
COPY ./target/messageremove.properties /messageremove.properties

COPY ./target/kafka-producer.properties_cat /kafka-producer.properties_cat
COPY ./target/kafka-producer.properties_do1 /kafka-producer.properties_do1

COPY ./target/kafka-custom.properties /kafka-custom.properties

COPY ./target/kafka-consumer.properties_cat /kafka-consumer.properties_cat
COPY ./target/kafka-consumer.properties_do1 /kafka-consumer.properties_do1


COPY ./target/jndi.properties /jndi.properties
COPY ./target/internal-ui.properties /internal-ui.properties
COPY ./target/inmem-refresh-interval.properties /inmem-refresh-interval.properties
COPY ./target/http-connector.properties /http-connector.properties
COPY ./target/global.properties /global.properties
COPY ./target/genericapi.properties /genericapi.properties
COPY ./target/exception-case.properties /exception-case.properties
COPY ./target/wallet-history.properties /wallet-history.properties
COPY ./target/wallet-balance-reminder.properties /wallet-balance-reminder.properties
COPY ./target/url_hitter.properties /url_hitter.properties
COPY ./target/timezone.properties /timezone.properties
COPY ./target/timer-procesor-interval.properties /timer-procesor-interval.properties
COPY ./target/sms_request.xsd /sms_request.xsd
COPY ./target/shortcode-provider.properties /shortcode-provider.properties
COPY ./target/replace-iso.properties /replace-iso.properties
COPY ./target/r3c_addinfo.properties /r3c_addinfo.properties
COPY ./target/prometheus.control.properties /prometheus.control.properties



VOLUME ["/logs"]

CMD [" /opt/tomcat/bin/catalina.sh", "run"]
