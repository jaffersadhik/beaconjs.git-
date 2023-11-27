package com.itextos.beacon.platform.subbiller.processor;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.itextos.beacon.commonlib.constants.ClusterType;
import com.itextos.beacon.commonlib.constants.Component;
import com.itextos.beacon.commonlib.constants.exception.ItextosException;
import com.itextos.beacon.commonlib.kafka.processor.AbstractKafkaComponentProcessor;
import com.itextos.beacon.commonlib.kafka.service.consumer.ConsumerInMemCollection;
import com.itextos.beacon.commonlib.messageobject.BaseMessage;
import com.itextos.beacon.commonlib.messageobject.IMessage;
import com.itextos.beacon.commonlib.messageobject.SubmissionObject;
import com.itextos.beacon.platform.billing.SubmissionProcess;

public class BillerProcessor
        extends
        AbstractKafkaComponentProcessor
{

    private static final Log log = LogFactory.getLog(BillerProcessor.class);

    public BillerProcessor(
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
        if (log.isDebugEnabled())
            log.debug("Biller Received Object .. " + aBaseMessage);

        try
        {
            final SubmissionProcess lSubProcessor = new SubmissionProcess((SubmissionObject) aBaseMessage);
            lSubProcessor.process();
        }
        catch (final ItextosException e)
        {
            log.error("Exception occure while process the submission billing : ", e);
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
