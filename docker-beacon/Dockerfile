FROM dk-reg.mas:5000/openjdk:21-jdk-slim
ENV TZ="Asia/Kolkata"
EXPOSE 20000/tcp

RUN mkdir -p /logs

RUN apt-get update && \
    apt-get install -y iputils-ping telnet vim curl procps  && \
    apt-get clean && \
    rm -rf /var/lib/apt/lists/*

COPY log4j2-common.xml /log4j2-common.xml

COPY custom_hosts_production.properties /custom_hosts_production.properties
COPY smpp-interface.properties /smpp-interface.properties
COPY encrypt.properties /encrypt.properties
COPY elasticsearch.properties /elasticsearch.properties
COPY dnd.properties /dnd.properties
COPY dlttemplateconfig.properties /dlttemplateconfig.properties
COPY common.properties /common.properties
COPY common-db.properties /common-db.properties
COPY callback-params.properties /callback-params.properties
COPY payload-params.properties /payload-params.properties
COPY midconfig.properties /midconfig.properties
COPY messageremove.properties /messageremove.properties
COPY kafka-producer.properties /kafka-producer.properties
COPY kafka-custom.properties /kafka-custom.properties
COPY kafka-consumer.properties /kafka-consumer.properties
COPY jndi.properties /jndi.properties
COPY internal-ui.properties /internal-ui.properties
COPY inmem-refresh-interval.properties /inmem-refresh-interval.properties
COPY http-connector.properties /http-connector.properties
COPY global.properties /global.properties
COPY genericapi.properties /genericapi.properties
COPY exception-case.properties /exception-case.properties
COPY wallet-history.properties /wallet-history.properties
COPY wallet-balance-reminder.properties /wallet-balance-reminder.properties
COPY url_hitter.properties /url_hitter.properties
COPY timezone.properties /timezone.properties
COPY timer-procesor-interval.properties /timer-procesor-interval.properties
COPY sms_request.xsd /sms_request.xsd
COPY shortcode-provider.properties /shortcode-provider.properties
COPY replace-iso.properties /replace-iso.properties
COPY r3c_addinfo.properties /r3c_addinfo.properties
COPY prometheus.control.properties /prometheus.control.properties


COPY mcc-mnc-list.json /mcc-mnc-list.json
COPY ./target/mainmodule-1.0-executable.jar /mainmodule-1.0-executable.jar
ENTRYPOINT ["java","-Xss1m","-Xmx512m","-Xms512m","-Dprometheus.jetty.port=20000","-Dcommon.property.file.location=/global.properties","-Dlog4j.configurationFile=file:/log4j2-common.xml","-jar","/mainmodule-1.0-executable.jar"]
VOLUME ["/instance"]
VOLUME ["/logs"]
