package com.itextos.beacon.commonlib.kafka.service.producer;

import com.itextos.beacon.commonlib.kafka.service.common.AbstractInMemCollection;
import com.itextos.beacon.commonlib.kafka.service.common.KafkaType;

public class ProducerInMemCollection
        extends
        AbstractInMemCollection
{

    public ProducerInMemCollection(
            String aProducerTopicName)
    {
        super(KafkaType.PRODUCER, aProducerTopicName);
    }

}