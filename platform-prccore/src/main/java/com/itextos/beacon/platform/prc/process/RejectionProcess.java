package com.itextos.beacon.platform.prc.process;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.itextos.beacon.commonlib.constants.ClusterType;
import com.itextos.beacon.commonlib.constants.Component;
import com.itextos.beacon.commonlib.constants.InterfaceType;
import com.itextos.beacon.commonlib.constants.PlatformStatusCode;
import com.itextos.beacon.commonlib.kafka.processor.AbstractKafkaComponentProcessor;
import com.itextos.beacon.commonlib.kafka.service.consumer.ConsumerInMemCollection;
import com.itextos.beacon.commonlib.messageobject.BaseMessage;
import com.itextos.beacon.commonlib.messageobject.IMessage;
import com.itextos.beacon.commonlib.messageobject.MessageRequest;
import com.itextos.beacon.commonlib.messageobject.SubmissionObject;
import com.itextos.beacon.platform.prc.util.PRCUtil;
import com.itextos.beacon.platform.prc.util.PRProducer;

public class RejectionProcess
        extends
        AbstractKafkaComponentProcessor
{

    private static final Log log = LogFactory.getLog(RejectionProcess.class);

    public RejectionProcess(
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
            log.debug("Message received from PRC : " + aBaseMessage);

        if (aBaseMessage instanceof MessageRequest)
            processMessageRequest((MessageRequest) aBaseMessage);

        if (aBaseMessage instanceof SubmissionObject)
            processSubmissionRequest((SubmissionObject) aBaseMessage);
    }

    private static void processMessageRequest(
            MessageRequest aMessageRequest)
    {

        try
        {
            boolean       canProcessMultiple = false;
            final boolean lprocessDNCarrier  = PRCUtil.processDNDToCarrier(aMessageRequest);

            if (log.isDebugEnabled())
                log.debug("Is Process DN Carrier for DND Fail :" + lprocessDNCarrier);

            final List<BaseMessage> lSubmissionRequestLst = aMessageRequest.getSubmissions();

            if (log.isInfoEnabled())
                log.info("msglist:" + lSubmissionRequestLst.size());

            if (PlatformStatusCode.PARTIALLY_CARRIER_HANDOVER_FAILED.getStatusCode().equals(aMessageRequest.getSubOriginalStatusCode()) || lprocessDNCarrier)
                canProcessMultiple = true;

            if (InterfaceType.SMPP == aMessageRequest.getInterfaceType())
                canProcessMultiple = true;

            if ((lSubmissionRequestLst.size() > 1) && canProcessMultiple)
            {
                if (log.isDebugEnabled())
                    log.debug("Multipart Message request... ");

                PRCUtil.processReq(aMessageRequest, lprocessDNCarrier, true);
            }
            else
                PRCUtil.processReq(aMessageRequest, lprocessDNCarrier, false);
        }
        catch (final Exception e)
        {
            PRProducer.sendToErrorLog(aMessageRequest, e);
        }
    }

    private static void processSubmissionRequest(
            SubmissionObject aSubmissionObject)
    {
        if (log.isDebugEnabled())
            log.debug("Processing Submission Object : " + aSubmissionObject);

        try
        {
            PRCUtil.processReq(aSubmissionObject, false);
        }
        catch (final Exception e)
        {
            PRProducer.sendToErrorLog(aSubmissionObject, e);
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
