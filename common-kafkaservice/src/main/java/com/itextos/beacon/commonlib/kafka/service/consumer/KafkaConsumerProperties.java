package com.itextos.beacon.commonlib.kafka.service.consumer;

import java.util.Properties;

public class KafkaConsumerProperties
{

    private final Properties mProperties;

    public KafkaConsumerProperties(
            Properties aProperties)
    {
        mProperties = aProperties;
    }

    public Properties getProperties()
    {
        return mProperties;
    }

}
