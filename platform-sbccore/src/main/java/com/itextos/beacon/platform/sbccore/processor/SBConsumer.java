package com.itextos.beacon.platform.sbccore.processor;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.itextos.beacon.commonlib.componentconsumer.processor.AbstractKafkaComponentProcessor;
import com.itextos.beacon.commonlib.constants.ClusterType;
import com.itextos.beacon.commonlib.constants.Component;
import com.itextos.beacon.commonlib.constants.Constants;
import com.itextos.beacon.commonlib.kafkaservice.consumer.ConsumerInMemCollection;
import com.itextos.beacon.commonlib.messageobject.BaseMessage;
import com.itextos.beacon.commonlib.messageobject.IMessage;
import com.itextos.beacon.commonlib.messageobject.MessageRequest;
import com.itextos.beacon.commonlib.utility.CommonUtility;
import com.itextos.beacon.platform.sbccore.data.InmemBlockoutQueue;
import com.itextos.beacon.platform.sbccore.data.InmemScheduleQueue;

public class SBConsumer
        extends
        AbstractKafkaComponentProcessor
{

    private static final Log    log         = LogFactory.getLog(SBConsumer.class);

    private static final String SBC_FROM_VC = "VC_SCHDBLOCK";
    private static final String SBC_FROM_RC = "RC_SCHDBLOCK";

    public SBConsumer(
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
            log.debug("SBC Message Received : " + lMessageRequest);

        try
        {
            final String lScheduleBlockOutFrom = CommonUtility.nullCheck(lMessageRequest.getFromScheduleBlockout(), true);

            if (log.isDebugEnabled())
                log.debug("Schedule / Blockout Message Received from : " + lScheduleBlockOutFrom);

            if (SBC_FROM_VC.equals(lScheduleBlockOutFrom) || SBC_FROM_RC.equals(lScheduleBlockOutFrom))
            {
                doInsert(lMessageRequest);
                return;
            }

            log.error(" Invalid Next Level specified in Message :" + lMessageRequest);

            SBCProducer.sendToNextTopic(lMessageRequest);
        }
        catch (final Exception e)
        {
            final String errorMsg = e.getMessage();
            log.error("Exception while trying to insert  on insert err errorMsg  :::: \t " + errorMsg);
            log.error(" on insert err map  " + lMessageRequest.toString(), e);

            try
            {
                SBCProducer.sendToNextTopic(lMessageRequest);
            }
            catch (final Exception e1)
            {
                log.error(" process() Exception " + e1 + lMessageRequest);
            }
        }
    }

    private static void doInsert(
            MessageRequest aMessageRequest)
    {

        try
        {
            final int lBlockOutScheduel = aMessageRequest.getScheduleBlockoutMessage();

            if (log.isDebugEnabled())
                log.debug("Is Message is Blockout / Schedule : " + lBlockOutScheduel);

            if (Constants.SCHEDULE_MSG == lBlockOutScheduel)
                InmemScheduleQueue.getInstance().addRecord(aMessageRequest);
            else
                if (Constants.BLOCKOUT_MSG == lBlockOutScheduel)
                    InmemBlockoutQueue.getInstance().addRecord(aMessageRequest);
        }
        catch (final Exception e)
        {
            final String errorMsg = e.getMessage();
            log.error(" on insert err errorMsg  :::: \t " + errorMsg);
            log.error(" on insert err map  " + aMessageRequest.toString(), e);
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
