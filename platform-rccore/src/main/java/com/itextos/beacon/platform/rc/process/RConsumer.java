package com.itextos.beacon.platform.rc.process;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.itextos.beacon.commonlib.constants.ClusterType;
import com.itextos.beacon.commonlib.constants.Component;
import com.itextos.beacon.commonlib.kafka.processor.AbstractKafkaComponentProcessor;
import com.itextos.beacon.commonlib.kafka.service.consumer.ConsumerInMemCollection;
import com.itextos.beacon.commonlib.message.BaseMessage;
import com.itextos.beacon.commonlib.message.IMessage;
import com.itextos.beacon.commonlib.message.MessageRequest;
import com.itextos.beacon.platform.rc.util.RCProducer;
import com.itextos.beacon.platform.rc.util.RCUtil;

public class RConsumer
        extends
        AbstractKafkaComponentProcessor
{

    private final Log log = LogFactory.getLog(RConsumer.class);

    public RConsumer(
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
            BaseMessage aBaseMessage)
    {
        final MessageRequest lMessageRequest = (MessageRequest) aBaseMessage;
        if (log.isDebugEnabled())
            log.debug("doProcess() RC Received Object .. " + lMessageRequest);

        try
        {
            RCUtil.setMessageValidityPeriod(lMessageRequest);

            if (lMessageRequest.isIsIntl())
                new RCIntlProcessor(lMessageRequest).doProcess();
            else
                new RProcessor(lMessageRequest).doRCProcess();
        }
        catch (final Exception e)
        {
            log.error("Exception occer while processing the RC Component..", e);

            try
            {
                RCProducer.sendToErrorLog(lMessageRequest, e);
            }
            catch (final Exception e1)
            {
                log.error("Unable to push the exception in eror log ..", e);
                e1.printStackTrace();
            }
        }
    }

    @Override
    public void doCleanup()
    {}

    @Override
    protected void updateBeforeSendBack(
            IMessage aMessage)
    {
        // TODO Auto-generated method stub
    }

}
