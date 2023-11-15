package com.itextos.beacon.platform.t2e.processor;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.itextos.beacon.commonlib.constants.ClusterType;
import com.itextos.beacon.commonlib.constants.Component;
import com.itextos.beacon.commonlib.kafka.processor.AbstractKafkaComponentProcessor;
import com.itextos.beacon.commonlib.kafka.service.consumer.ConsumerInMemCollection;
import com.itextos.beacon.commonlib.message.BaseMessage;
import com.itextos.beacon.commonlib.message.IMessage;
import com.itextos.beacon.commonlib.message.SubmissionObject;
import com.itextos.beacon.commonlib.utility.CommonUtility;
import com.itextos.beacon.platform.esutil.EsProcess;
import com.itextos.beacon.platform.t2e.util.T2EProducer;

public class AgingInsertProcessor
        extends
        AbstractKafkaComponentProcessor
{

    private static final Log     log           = LogFactory.getLog(AgingInsertProcessor.class);
    private static final boolean isDropMessage = CommonUtility.isTrue(System.getProperty("dropmessage"));

    public AgingInsertProcessor(
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
        if (isDropMessage)
            return;

        final SubmissionObject lSubmissionObject = (SubmissionObject) aBaseMessage;

        if (log.isDebugEnabled())
            log.debug("doProcess() Aging Insert Received Object .. " + lSubmissionObject);

        try
        {
            EsProcess.insertAgingDn(lSubmissionObject);
        }
        catch (final Exception e)
        {
            log.error("Exception occer while insert Aging record into Elastic Search.", e);
            T2EProducer.sendToErrorLog(lSubmissionObject, e, mComponent);
        }
    }

    @Override
    public void doCleanup()
    {
        // TODO Auto-generated method stub
    }

    @Override
    protected void updateBeforeSendBack(
            IMessage aMessage)
    {
        // TODO Auto-generated method stub
    }

}