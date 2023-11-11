package com.itextos.beacon.platform.dnpcore.process;

import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.itextos.beacon.commonlib.constants.ClusterType;
import com.itextos.beacon.commonlib.constants.Component;
import com.itextos.beacon.commonlib.kafka.processor.AbstractKafkaComponentProcessor;
import com.itextos.beacon.commonlib.kafka.service.consumer.ConsumerInMemCollection;
import com.itextos.beacon.commonlib.message.BaseMessage;
import com.itextos.beacon.commonlib.message.DeliveryObject;
import com.itextos.beacon.commonlib.message.IMessage;
import com.itextos.beacon.platform.dnpcore.util.DNPProducer;

public class DlrInternalProcessor
        extends
        AbstractKafkaComponentProcessor
{

    private static final Log log = LogFactory.getLog(DlrInternalProcessor.class);

    public DlrInternalProcessor(
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
            log.debug(" Message received : " + aBaseMessage);

        final DeliveryObject lDeliveryObject = (DeliveryObject) aBaseMessage;

        try
        {
            final Map<Component, DeliveryObject> processInternalDNQ = DlrProcessUtil.processDnReceiverQ(lDeliveryObject);

            DNPProducer.sendToNextComponents(processInternalDNQ);
        }
        catch (final Exception e)
        {
            log.error("Exception occer while processing the  Internal Processor Dlr : ", e);

            DNPProducer.sendToErrorLog(lDeliveryObject, e);
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
