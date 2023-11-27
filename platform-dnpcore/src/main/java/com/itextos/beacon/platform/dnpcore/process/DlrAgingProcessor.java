package com.itextos.beacon.platform.dnpcore.process;

import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.itextos.beacon.commonlib.constants.ClusterType;
import com.itextos.beacon.commonlib.constants.Component;
import com.itextos.beacon.commonlib.kafka.processor.AbstractKafkaComponentProcessor;
import com.itextos.beacon.commonlib.kafka.service.consumer.ConsumerInMemCollection;
import com.itextos.beacon.commonlib.messageobject.BaseMessage;
import com.itextos.beacon.commonlib.messageobject.DeliveryObject;
import com.itextos.beacon.commonlib.messageobject.IMessage;
import com.itextos.beacon.platform.dnpcore.util.DNPProducer;

public class DlrAgingProcessor
        extends
        AbstractKafkaComponentProcessor
{

    private static final Log log = LogFactory.getLog(DlrAgingProcessor.class);

    public DlrAgingProcessor(
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

        try
        {
            final Map<Component, DeliveryObject> processAgeingDNQ = DlrProcessUtil.processAgeingDNProcessQ(lDeliveryObject);

            if ((processAgeingDNQ != null) && !processAgeingDNQ.isEmpty())
            {
                if (log.isDebugEnabled())
                    log.debug(" process() - Sending to " + processAgeingDNQ.keySet() + " json:" + lDeliveryObject.toString());

                DNPProducer.sendToNextComponents(processAgeingDNQ);
            }
        }
        catch (final Exception e)
        {
            log.error("Exception occer while processing Aging Dlr Processor....", e);
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
