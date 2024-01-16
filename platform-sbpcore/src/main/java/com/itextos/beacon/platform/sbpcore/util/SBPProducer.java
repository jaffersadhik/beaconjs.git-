package com.itextos.beacon.platform.sbpcore.util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.itextos.beacon.commonlib.constants.Component;
import com.itextos.beacon.commonlib.constants.exception.ItextosException;
import com.itextos.beacon.commonlib.message.MessageRequest;
import com.itextos.beacon.commonlib.messageprocessor.process.MessageProcessor;
import com.itextos.beacon.platform.bwc.process.BlockoutWalletProcessor;
import com.itextos.beacon.platform.ch.processor.CarrierHandoverProcess;
import com.itextos.beacon.platform.dltvc.process.DltProcessor;
import com.itextos.beacon.platform.msgflowutil.util.PlatformUtil;

public class SBPProducer
{

    public static final Log log = LogFactory.getLog(SBPProducer.class);

    private SBPProducer()
    {}

    public static void sendToPlatformRejection(
            MessageRequest aMessageRequest)
    {

        try
        {
            aMessageRequest.setPlatfromRejected(true);
            MessageProcessor.writeMessage(Component.SBP, Component.PRC, aMessageRequest);
        }
        catch (final ItextosException e)
        {
            sendToErrorLog(aMessageRequest, e);
        }
    }

    public static void sendToRouterComponent(
            MessageRequest aMessageRequest)
    {

        try
        {
   //         MessageProcessor.writeMessage(Component.SBP, Component.CH, aMessageRequest);
            
            aMessageRequest.setFromComponent(Component.SBP.getKey());
            aMessageRequest.setNextComponent(Component.CH.getKey());
            CarrierHandoverProcess.forCH(aMessageRequest, aMessageRequest.getClusterType());


        }
        catch (final Exception e)
        {
            sendToErrorLog(aMessageRequest, e);
        }
    }

    public static void sendToBlockoutWCComponent(
            MessageRequest aMessageRequest)
    {

        try
        {
      //      MessageProcessor.writeMessage(Component.SBP, Component.BWC, aMessageRequest);
            
        	aMessageRequest.setFromComponent(Component.SBP.getKey());
        	aMessageRequest.setNextComponent(Component.BWC.getKey());
            BlockoutWalletProcessor.forBWC(aMessageRequest);
        }
        catch (final Exception e)
        {
            sendToErrorLog(aMessageRequest, e);
        }
    }

    public static void sendToVerifyConsumer(
            MessageRequest aMessageRequest)
    {

        try
        {
            if (aMessageRequest.isBypassDltCheck() || aMessageRequest.isIsIntl()) {
 //               MessageProcessor.writeMessage(Component.SBP, Component.VC, aMessageRequest);
              	aMessageRequest.setFromComponent(Component.SBP.getKey());
            	aMessageRequest.setNextComponent(Component.VC.getKey());
            	com.itextos.beacon.platform.vc.process.MessageProcessor.forVC(Component.VC, aMessageRequest);
            		
            }else {
            //    MessageProcessor.writeMessage(Component.SBP, Component.DLTVC, aMessageRequest);
              	aMessageRequest.setFromComponent(Component.SBP.getKey());
            	aMessageRequest.setNextComponent(Component.DLTVC.getKey());

                DltProcessor.forDLT(aMessageRequest, Component.DLTVC);

            }
        }
        catch (final Exception e)
        {
            sendToErrorLog(aMessageRequest, e);
        }
    }

    public static void sendToErrorLog(
            MessageRequest aMessageRequest,
            Exception aErrorMsg)
    {

        try
        {
            PlatformUtil.sendToErrorLog(Component.SBP, aMessageRequest, aErrorMsg);
        }
        catch (final Exception e)
        {
            log.error("Exception while sending request to error log. " + aMessageRequest, e);
        }
    }

}