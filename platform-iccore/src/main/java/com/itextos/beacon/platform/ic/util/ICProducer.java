package com.itextos.beacon.platform.ic.util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.itextos.beacon.commonlib.constants.ClusterType;
import com.itextos.beacon.commonlib.constants.Component;
import com.itextos.beacon.commonlib.constants.exception.ItextosException;
import com.itextos.beacon.commonlib.message.BaseMessage;
import com.itextos.beacon.commonlib.message.MessageRequest;
import com.itextos.beacon.commonlib.messageprocessor.process.MessageProcessor;
import com.itextos.beacon.platform.dltvc.process.DltProcessor;
import com.itextos.beacon.platform.msgflowutil.util.PlatformUtil;
import com.itextos.beacon.platform.r3c.process.R3CProcess;
import com.itextos.beacon.platform.sbcv.process.SBCVProcess;

public class ICProducer
{

    private static final Log log = LogFactory.getLog(ICProducer.class);

    
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
        //    MessageProcessor.writeMessage(Component.IC, Component.R3C, aMessageRequest);
        
        	aMessageRequest.setFromComponent(Component.IC.getKey());
        	aMessageRequest.setNextComponent(Component.R3C.getKey());
        	R3CProcess.forR3C(aMessageRequest);
        }
        catch (final Exception e)
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
             //   MessageProcessor.writeMessage(Component.IC, Component.SBCV, aMessageRequest);
                
                aMessageRequest.setFromComponent(Component.IC.getKey());
                aMessageRequest.setNextComponent(Component.SBCV.getKey());
                SBCVProcess.forSBCV(aMessageRequest);
            }
        }
        catch (final Exception e)
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
            if (aMessageRequest.isBypassDltCheck() || aMessageRequest.isIsIntl() || aMessageRequest.isIldo()) {
               
            	
            	//MessageProcessor.writeMessage(Component.IC, Component.VC, aMessageRequest);
            	aMessageRequest.setFromComponent(Component.IC.getKey());
            	aMessageRequest.setNextComponent(Component.VC.getKey());
            	com.itextos.beacon.platform.vc.process.MessageProcessor.forVC(Component.VC, aMessageRequest);
            }
            else {
           //     MessageProcessor.writeMessage(Component.IC, Component.DLTVC, aMessageRequest);
            	aMessageRequest.setFromComponent(Component.IC.getKey());
            	aMessageRequest.setNextComponent(Component.DLTVC.getKey());

                DltProcessor.forDLT(aMessageRequest, Component.DLTVC);
            
            }
        }
        catch (final Exception e)
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
