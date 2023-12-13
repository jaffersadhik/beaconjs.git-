package com.itextos.beacon.platform.iccore.util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.itextos.beacon.commonlib.constants.ClusterType;
import com.itextos.beacon.commonlib.constants.Component;
import com.itextos.beacon.commonlib.constants.exception.ItextosException;
import com.itextos.beacon.commonlib.messageobject.BaseMessage;
import com.itextos.beacon.commonlib.messageobject.MessageRequest;
import com.itextos.beacon.commonlib.messageprocessor.process.MessageProcessor;
import com.itextos.beacon.platform.msgflowutil.util.PlatformUtil;

public class ICProducer
{

    private static final Log log = LogFactory.getLog(ICProducer.class);

    private ICProducer()
    {}

    public static void sendToPlatformRejection(
            MessageRequest aMessageRequest)
    {

        try
        {
            MessageProcessor.writeMessage(Component.IC, Component.PRC, aMessageRequest);
        }
        catch (final ItextosException e)
        {
            log.error(aMessageRequest.getBaseMessageId()+ " : Exception while sending the message to Platform Reject topic.", e);
            sendToErrorLog(aMessageRequest, e);
        }
    }

    public static void sendToUrlShortner(
            MessageRequest aMessageRequest)
    {

        try
        {
            MessageProcessor.writeMessage(Component.IC, Component.R3C, aMessageRequest);
        }
        catch (final ItextosException e)
        {
            log.error("Exception while sending the message to URL Shortner topic.", e);
            sendToErrorLog(aMessageRequest, e);
        }
    }

    public static void sendToSBCVProcessor(
            MessageRequest aMessageRequest)
    {

        try
        {

            if (ClusterType.OTP == aMessageRequest.getClusterType())
            {
                if (log.isDebugEnabled())
                    log.debug("Bypassing the Schedule/Blockout funcationality for OTP request.., Hence request sending to VC");

                sendToVerifyConsumerTopic(aMessageRequest);
            }
            else
            {
                if (log.isDebugEnabled())
                    log.debug("Request sending to SBCV..");
                MessageProcessor.writeMessage(Component.IC, Component.SBCV, aMessageRequest);
            }
        }
        catch (final ItextosException e)
        {
            log.error("Exception while sending the message to SBCV Process topic.", e);
            sendToErrorLog(aMessageRequest, e);
        }
    }

    public static void sendToVerifyConsumerTopic(
            MessageRequest aMessageRequest)
    {

        try
        {
            if (aMessageRequest.isBypassDltCheck() || aMessageRequest.isIsIntl() || aMessageRequest.isIldo())
                MessageProcessor.writeMessage(Component.IC, Component.VC, aMessageRequest);
            else
                MessageProcessor.writeMessage(Component.IC, Component.DLTVC, aMessageRequest);
        }
        catch (final ItextosException e)
        {
            log.error("Exception while sending the message to Verify Consumer topic.", e);
            sendToErrorLog(aMessageRequest, e);
        }
    }

    public static void sendToErrorLog(
            BaseMessage aBaseMessage,
            Exception aErrorMsg)
    {

        try
        {
            PlatformUtil.sendToErrorLog(Component.IC, aBaseMessage, aErrorMsg);
        }
        catch (final Exception e)
        {
            log.error("Exception while sending request to error log. " + aBaseMessage, e);
        }
    }

}
