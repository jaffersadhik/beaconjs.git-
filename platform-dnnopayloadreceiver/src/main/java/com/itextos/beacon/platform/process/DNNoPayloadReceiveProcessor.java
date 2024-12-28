package com.itextos.beacon.platform.process;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.itextos.beacon.commonlib.componentconsumer.processor.AbstractKafkaComponentProcessor;
import com.itextos.beacon.commonlib.constants.ClusterType;
import com.itextos.beacon.commonlib.constants.Component;
import com.itextos.beacon.commonlib.kafkaservice.consumer.ConsumerInMemCollection;
import com.itextos.beacon.commonlib.message.BaseMessage;
import com.itextos.beacon.commonlib.message.DeliveryObject;
import com.itextos.beacon.commonlib.message.IMessage;
import com.itextos.beacon.platform.dnnopayloadreceiver.inmem.NoPayloadRetryQ;
import com.itextos.beacon.platform.dnnopayloadreceiver.util.DNPProducer;

public class DNNoPayloadReceiveProcessor
        extends
        AbstractKafkaComponentProcessor
{

    private static final Log log = LogFactory.getLog(DNNoPayloadReceiveProcessor.class);

    public DNNoPayloadReceiveProcessor(
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

        try
        {
            DeliveryObject lDeliveryObject = (DeliveryObject) aBaseMessage;

            NoPayloadRetryQ.getInstance().addMessage(lDeliveryObject);
        }
        
        catch (final Exception e)
        {
            log.error("Exception occer while processing the Carrier DN/ Internal Rejection Dlr : ", e);
            DNPProducer.sendToErrorLog(aBaseMessage, e);
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
