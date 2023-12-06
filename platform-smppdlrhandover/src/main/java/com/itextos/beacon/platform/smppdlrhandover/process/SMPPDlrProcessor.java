package com.itextos.beacon.platform.smppdlrhandover.process;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.itextos.beacon.commonlib.componentconsumer.processor.AbstractKafkaComponentProcessor;
import com.itextos.beacon.commonlib.constants.ClusterType;
import com.itextos.beacon.commonlib.constants.Component;
import com.itextos.beacon.commonlib.kafkaservice.consumer.ConsumerInMemCollection;
import com.itextos.beacon.commonlib.messageobject.BaseMessage;
import com.itextos.beacon.commonlib.messageobject.DeliveryObject;
import com.itextos.beacon.commonlib.messageobject.IMessage;
import com.itextos.beacon.platform.smppdlrhandover.inmemq.InmemoryQueue;
import com.itextos.beacon.platform.smppdlrhandover.util.SmppDlrProducer;

public class SMPPDlrProcessor
        extends
        AbstractKafkaComponentProcessor
{

    private static final Log log = LogFactory.getLog(SMPPDlrProcessor.class);

    public SMPPDlrProcessor(
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
        final DeliveryObject lDeliveryObject = (DeliveryObject) aBaseMessage;

        if (log.isDebugEnabled())
            log.debug(lDeliveryObject.getMessageId()+ " : SMPP Dlr the request : " + lDeliveryObject);

        try
        {
            InmemoryQueue.getInstance().addRecord(lDeliveryObject);
        }
        catch (final Exception e)
        {
            SmppDlrProducer.sendToErrorLog(lDeliveryObject, e);
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
