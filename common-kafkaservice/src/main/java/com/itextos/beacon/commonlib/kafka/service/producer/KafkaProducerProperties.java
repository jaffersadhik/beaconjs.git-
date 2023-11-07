package com.itextos.beacon.commonlib.kafka.service.producer;

import java.util.Properties;

public class KafkaProducerProperties
{

    private final Properties mProperties;

    public KafkaProducerProperties(
            Properties aProperties)
    {
        mProperties = aProperties;
    }

    public Properties getProperties()
    {
        return mProperties;
    }

}
