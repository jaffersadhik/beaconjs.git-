package com.itextos.beacon.commonlib.kafka.service.consumer;

import com.itextos.beacon.commonlib.kafka.service.common.AbstractInMemCollection;
import com.itextos.beacon.commonlib.kafka.service.common.KafkaType;

public class ConsumerInMemCollection
        extends
        AbstractInMemCollection
{

    public ConsumerInMemCollection(
            String aConsumerTopicName)
    {
        super(KafkaType.CONSUMER, aConsumerTopicName);
    }

}
