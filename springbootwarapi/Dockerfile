#FROM dk-reg.mas:5000/openjdk:21-jdk-slim
FROM dk-reg.mas:5000/tomcat:10.1.0-jdk21-temurin

ENV TZ="Asia/Kolkata"
EXPOSE 20000/tcp




COPY ./target/log4j2-common.xml /log4j2-common.xml

COPY ./target/custom_hosts_production.properties /custom_hosts_production.properties
COPY ./target/smpp-interface.properties /smpp-interface.properties
COPY ./target/encrypt.properties /encrypt.properties
COPY ./target/elasticsearch.properties /elasticsearch.properties
COPY ./target/elasticsearch.properties_cat /elasticsearch.properties_cat

COPY ./target/dnd.properties /dnd.properties
COPY ./target/dlttemplateconfig.properties /dlttemplateconfig.properties
COPY ./target/common.properties /common.properties
COPY ./target/common-db.properties /common-db.properties
COPY ./target/common-db.properties_cat /common-db.properties_cat

COPY ./target/callback-params.properties /callback-params.properties
COPY ./target/payload-params.properties /payload-params.properties
COPY ./target/midconfig.properties /midconfig.properties
COPY ./target/messageremove.properties /messageremove.properties
COPY ./target/kafka-producer.properties /kafka-producer.properties
COPY ./target/kafka-producer.properties_cat /kafka-producer.properties_cat
COPY ./target/kafka-custom.properties /kafka-custom.properties
COPY ./target/kafka-consumer.properties /kafka-consumer.properties
COPY ./target/kafka-consumer.properties_cat /kafka-consumer.properties_cat
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


COPY ./target/mcc-mnc-list.json /mcc-mnc-list.json
COPY ./target/springbootwarapi-1.0.war /usr/local/tomcat/webapps/springbootwarapi-1.0.war

