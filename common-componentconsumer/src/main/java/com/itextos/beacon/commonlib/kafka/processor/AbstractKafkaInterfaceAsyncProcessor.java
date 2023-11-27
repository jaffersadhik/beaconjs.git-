package com.itextos.beacon.commonlib.kafka.processor;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.itextos.beacon.commonlib.constants.ClusterType;
import com.itextos.beacon.commonlib.constants.Component;
import com.itextos.beacon.commonlib.constants.MessagePriority;
import com.itextos.beacon.commonlib.constants.exception.ItextosException;
import com.itextos.beacon.commonlib.kafka.service.consumer.ConsumerInMemCollection;
import com.itextos.beacon.commonlib.messageobject.AsyncRequestObject;
import com.itextos.beacon.commonlib.messageobject.IMessage;
import com.itextos.beacon.commonlib.messageprocessor.process.MessageProcessor;
import com.itextos.beacon.commonlib.messageprocessor.request.ProducerKafkaRequest;

public abstract class AbstractKafkaInterfaceAsyncProcessor
        extends
        AbstractCommonComponentProcessor
{

    private static final Log log = LogFactory.getLog(AbstractKafkaInterfaceAsyncProcessor.class);
    private final Component  mFromComponent;

    protected AbstractKafkaInterfaceAsyncProcessor(
            String aThreadName,
            Component aFromComponent,
            Component aNextComponent,
            ClusterType aPlatformCluster,
            String aTopicName,
            ConsumerInMemCollection aConsumerInMemCollection,
            int aSleepInMillis)
    {
        super(aThreadName, aNextComponent, aPlatformCluster, aTopicName, aConsumerInMemCollection, aSleepInMillis);
        mFromComponent = aFromComponent;
    }

    @Override
    public void processMessage(
            IMessage aMessage)
    {
        if (log.isDebugEnabled())
            log.debug("Processing message " + aMessage);

        try
        {
            doProcess((AsyncRequestObject) aMessage);
        }
        catch (final Exception e)
        {
            log.error("Exception while processing the message " + aMessage, e);
            sendBackToTopic(aMessage);
        }
    }

    @Override
    protected void sendBackToTopic(
            IMessage aMessage)
    {

        try
        {
            updateBeforeSendBack(aMessage);
            final AsyncRequestObject   requestObject        = (AsyncRequestObject) aMessage;
            final ProducerKafkaRequest producerKafkaRequest = new ProducerKafkaRequest(mFromComponent, mComponent, requestObject.getClusterType(), requestObject.getRequestType().getGroup(),
                    requestObject.getMessageType(), MessagePriority.PRIORITY_5, false, null);
            MessageProcessor.writeMessage(producerKafkaRequest, null, aMessage);
        }
        catch (final ItextosException e)
        {
            log.error("Exception while sending the message to the same topic", e);
        }
    }

    public abstract void doProcess(
            AsyncRequestObject aMessage)
            throws Exception;

}
