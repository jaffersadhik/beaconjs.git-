FROM quay.io/wildfly/wildfly:latest-jdk17
ENV TZ="Asia/Kolkata"

EXPOSE 8080 9990

COPY standalone.xml /opt/jboss/wildfly/standalone/configuration/standalone.xml

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


COPY ./target/docker-beaconweb-1.0.ear /opt/jboss/wildfly/standalone/deployments/docker-beaconweb-1.0.ear
VOLUME ["/logs"]
CMD ["/opt/jboss/wildfly/bin/standalone.sh","-b","0.0.0.0","-Dprometheus.jetty.port=1075","-Dcommon.property.file.location=/platform/properties/bulk/global.properties"]