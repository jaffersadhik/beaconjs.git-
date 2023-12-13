package com.itextos.beacon.platform.sbcvcore.util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.itextos.beacon.commonlib.constants.Component;
import com.itextos.beacon.commonlib.constants.exception.ItextosException;
import com.itextos.beacon.commonlib.message.MessageRequest;
import com.itextos.beacon.commonlib.messageprocessor.process.MessageProcessor;
import com.itextos.beacon.platform.msgflowutil.util.PlatformUtil;

public class SBCVProducer
{

    private SBCVProducer()
    {}

    private static final Log log = LogFactory.getLog(SBCVProducer.class);

    public static void sendToPlatformRejection(
            MessageRequest aMessageRequest)
    {

        try
        {
            aMessageRequest.setPlatfromRejected(true);
            MessageProcessor.writeMessage(Component.SBCV, Component.PRC, aMessageRequest);
        }
        catch (final ItextosException e)
        {
            log.error("Exception while sending the message to Platform Reject topic.", e);
            sendToErrorLog(aMessageRequest, e);
        }
    }

    public static void sendToVerifyConsumerTopic(
            MessageRequest aMessageRequest)
    {

        try
        {
            if (aMessageRequest.isBypassDltCheck() || aMessageRequest.isIsIntl()) {
              
            	MessageProcessor.writeMessage(Component.SBCV, Component.VC, aMessageRequest);

            }else {
            
            	if(aMessageRequest.isIldo()) {
                    MessageProcessor.writeMessage(Component.SBCV, Component.VC, aMessageRequest);

            	}else {
            		MessageProcessor.writeMessage(Component.SBCV, Component.DLTVC, aMessageRequest);
            	}
            }
            	
        }
        catch (final ItextosException e)
        {
            log.error("Exception while sending the message to Verify Consumer topic.", e);
            sendToErrorLog(aMessageRequest, e);
        }
    }

    public static void sendToScheduleTopic(
            MessageRequest aMessageRequest)
    {

        try
        {
            MessageProcessor.writeMessage(Component.SBCV, Component.SBC, aMessageRequest);
        }
        catch (final ItextosException e)
        {
            log.error("Exception while sending the message to Schedule topic.", e);
            sendToErrorLog(aMessageRequest, e);
        }
    }

    public static void sendToBlockoutTopic(
            MessageRequest aMessageRequest)
    {

        try
        {
            MessageProcessor.writeMessage(Component.SBCV, Component.SBC, aMessageRequest);
        }
        catch (final ItextosException e)
        {
            log.error("Exception while sending the message to Blockout topic.", e);
            sendToErrorLog(aMessageRequest, e);
        }
    }

    public static void sendToErrorLog(
            MessageRequest aMessageRequest,
            Exception aErrorMsg)
    {

        try
        {
            PlatformUtil.sendToErrorLog(Component.SBCV, aMessageRequest, aErrorMsg);
        }
        catch (final Exception e)
        {
            log.error("Exception while sending request to error log. " + aMessageRequest, e);
        }
    }

}
