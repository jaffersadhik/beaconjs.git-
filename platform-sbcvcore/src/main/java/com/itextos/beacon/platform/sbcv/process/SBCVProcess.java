package com.itextos.beacon.platform.sbcv.process;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.itextos.beacon.commonlib.componentconsumer.processor.AbstractKafkaComponentProcessor;
import com.itextos.beacon.commonlib.constants.ClusterType;
import com.itextos.beacon.commonlib.constants.Component;
import com.itextos.beacon.commonlib.constants.Constants;
import com.itextos.beacon.commonlib.kafkaservice.consumer.ConsumerInMemCollection;
import com.itextos.beacon.commonlib.message.BaseMessage;
import com.itextos.beacon.commonlib.message.IMessage;
import com.itextos.beacon.commonlib.message.MessageRequest;
import com.itextos.beacon.platform.sbcv.util.SBCVProducer;

public class SBCVProcess
        extends
        AbstractKafkaComponentProcessor
{

    private static final Log log = LogFactory.getLog(SBCVProcess.class);

    public SBCVProcess(
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
            log.debug("SBCV Receive the request : " + lMessageRequest);

        try
        {
            final boolean canProcess = new ScheduleBlockout(lMessageRequest).validateScheduleBlockoutMsg();

            if (!canProcess)
            {
                final boolean isRejectedMessage = lMessageRequest.isPlatfromRejected();

                if (log.isDebugEnabled())
                    log.debug("Is Message drop based on Schedule / Blockout ? " + isRejectedMessage);

                if (isRejectedMessage)
                    SBCVProducer.sendToPlatformRejection(lMessageRequest);
                else
                    sendToNextProcess(lMessageRequest);
            }
            else
            {
                if (log.isDebugEnabled())
                    log.debug("Request sending to VC topic : " + lMessageRequest.getBaseMessageId());

                SBCVProducer.sendToVerifyConsumerTopic(lMessageRequest);
            }
        }
        catch (final Exception e)
        {
            e.printStackTrace();
        }
    }

    private static void sendToNextProcess(
            MessageRequest aMessageRequest)
    {
        if (log.isDebugEnabled())
            log.debug("Sending to Schedule/ Blockout Topic : " + aMessageRequest.getBaseMessageId());

        final int lBlockOutScheduel = aMessageRequest.getScheduleBlockoutMessage();

        if (log.isDebugEnabled())
            log.debug("Is message sending Schedule / Blockout ? " + lBlockOutScheduel);

        if (Constants.SCHEDULE_MSG == lBlockOutScheduel)
            SBCVProducer.sendToScheduleTopic(aMessageRequest);

        if (Constants.BLOCKOUT_MSG == lBlockOutScheduel)
            SBCVProducer.sendToBlockoutTopic(aMessageRequest);
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
