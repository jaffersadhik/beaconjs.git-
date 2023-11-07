package com.itextos.beacon.commonlib.kafka.processor.extend;

import com.itextos.beacon.commonlib.constants.ClusterType;
import com.itextos.beacon.commonlib.constants.Component;
import com.itextos.beacon.commonlib.exception.ItextosException;
import com.itextos.beacon.commonlib.kafka.process.MessageProcessor;
import com.itextos.beacon.commonlib.kafka.processor.AbstractKafkaComponentProcessor;
import com.itextos.beacon.commonlib.kafka.service.consumer.ConsumerInMemCollection;
import com.itextos.beacon.commonlib.message.BaseMessage;
import com.itextos.beacon.commonlib.message.IMessage;

public class SampleComponentProcessor
        extends
        AbstractKafkaComponentProcessor
{

    protected SampleComponentProcessor(
            String aThreadName,
            Component aComponent,
            ClusterType aPlatformCluster,
            String aTopicName,
            ConsumerInMemCollection aConsumerInMemCollection,
            int aSleepInMillis)
    {
        super(aThreadName, aComponent, aPlatformCluster, aTopicName, aConsumerInMemCollection, aSleepInMillis);
    }

    @Override
    public void doProcess(
            BaseMessage aMessage)
    {

        try
        {
            MessageProcessor.writeMessage(Component.IC, Component.VC, aMessage);
        }
        catch (final ItextosException e)
        {
            e.printStackTrace();
        }
    }

    @Override
    public void doCleanup()
    {}

    @Override
    protected void updateBeforeSendBack(
            IMessage aMessage)
    {}

}