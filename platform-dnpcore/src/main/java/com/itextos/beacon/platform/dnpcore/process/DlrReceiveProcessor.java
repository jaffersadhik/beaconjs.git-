package com.itextos.beacon.platform.dnpcore.process;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.itextos.beacon.commonlib.componentconsumer.processor.AbstractKafkaComponentProcessor;
import com.itextos.beacon.commonlib.constants.ClusterType;
import com.itextos.beacon.commonlib.constants.Component;
import com.itextos.beacon.commonlib.kafkaservice.consumer.ConsumerInMemCollection;
import com.itextos.beacon.commonlib.messageobject.BaseMessage;
import com.itextos.beacon.commonlib.messageobject.DeliveryObject;
import com.itextos.beacon.commonlib.messageobject.IMessage;
import com.itextos.beacon.commonlib.utility.CommonUtility;
import com.itextos.beacon.platform.dnpayloadutil.PayloadProcessor;
import com.itextos.beacon.platform.dnpcore.inmem.NoPayloadRetryQ;
import com.itextos.beacon.platform.dnpcore.util.DNPProducer;
import com.itextos.beacon.platform.dnpcore.util.DNPUtil;

public class DlrReceiveProcessor
        extends
        AbstractKafkaComponentProcessor
{

    private static final Log log = LogFactory.getLog(DlrReceiveProcessor.class);

    public DlrReceiveProcessor(
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

            if (log.isDebugEnabled())
                log.debug(lDeliveryObject.getMessageId()+ " : Message received form Carrier : " + lDeliveryObject);

            // Request Received from Carrier

            final String requestFromVoice = null;// aNunMessage.getValue(MiddlewareConstant.MW_IS_VOICE_DLR);

            String       lPayloadStatus   = CommonUtility.nullCheck(lDeliveryObject.getDnPayloadStatus(), true);

            if (requestFromVoice == null)
            {

                if (lPayloadStatus.isEmpty())
                {
                    final String lCarrierFullDn = CommonUtility.nullCheck(lDeliveryObject.getCarrierFullDn(), true);

                    if (!lCarrierFullDn.isEmpty())
                        lDeliveryObject = DNPUtil.processDR(lDeliveryObject);

                    if (log.isDebugEnabled())
                        log.debug(lDeliveryObject.getMessageId()+ " : DN Message Obj - " + lDeliveryObject);

                    lDeliveryObject = PayloadProcessor.retrivePayload(lDeliveryObject);
                }

                if (log.isDebugEnabled())
                    log.debug(lDeliveryObject.getMessageId()+ " : Payload Object - " + lDeliveryObject.getJsonString());

                lPayloadStatus = CommonUtility.nullCheck(lDeliveryObject.getDnPayloadStatus(), true);
            }

            if (lDeliveryObject.isPlatfromRejected() || "1".equals(lPayloadStatus) || "1".equals(requestFromVoice))
            {
                DNPUtil.setPlatformErrorCodeBasedOnCarrierErrorCode(lDeliveryObject);

                final Map<Component, DeliveryObject> lProcessDnReceiverQ = DlrProcessUtil.processDnReceiverQ(lDeliveryObject);

                if ((lProcessDnReceiverQ != null) && !lProcessDnReceiverQ.isEmpty())
                {
                    if (log.isDebugEnabled())
                        log.debug(lDeliveryObject.getMessageId()+" :  Sending to " + lProcessDnReceiverQ.keySet() + " Message Obj :" + lDeliveryObject);

                    // return processDnReceiverQ;
                    DNPProducer.sendToNextComponents(lProcessDnReceiverQ);
                }
            }
            else
            {
                final HashMap<Component, DeliveryObject> processDNQueues = new HashMap<>();

                // -1, 0
                if ("-1".equals(lPayloadStatus))
                {
                    DNPUtil.setPlatformErrorCodeBasedOnCarrierErrorCode(lDeliveryObject);
                    processDNQueues.put(Component.T2DB_NO_PAYLOAD_DN, lDeliveryObject);
                }
                else
                    if ("0".equals(lPayloadStatus))
                        NoPayloadRetryQ.getInstance().addMessage(lDeliveryObject);

                if (log.isDebugEnabled())
                    log.debug(" Sending to " + processDNQueues.keySet() + " Message Obj:" + lDeliveryObject);

                DNPProducer.sendToNextComponents(processDNQueues);
            }
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
