package com.itextos.beacon.platform.vcprocess.util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.itextos.beacon.commonlib.constants.Component;
import com.itextos.beacon.commonlib.constants.InterfaceType;
import com.itextos.beacon.commonlib.exception.ItextosException;
import com.itextos.beacon.commonlib.kafka.process.MessageProcessor;
import com.itextos.beacon.commonlib.message.MessageRequest;
import com.itextos.beacon.platform.msgflowutil.util.PlatformUtil;

public class VCProducer
{

    private static final Log log = LogFactory.getLog(VCProducer.class);

    private VCProducer()
    {}

    public static void sendToNextComponent(
            Component aSourceComponent,
            Component aComponent,
            MessageRequest aMessageRequest)
    {

        try
        {
            /**
             * Execute Split logic here for other interface requests except SMPP
             * For SMPP Request platform will skip the Message Split and UDH generate
             * process
             */
            if (aMessageRequest.getInterfaceType() != InterfaceType.SMPP)
                doFeatureCodeIdentifier(aMessageRequest);

            if (!VCUtil.doCappingCheck(aMessageRequest, aSourceComponent))
                return;

            switch (aComponent)
            {
                case PRC:
                    sendToPlatformRejection(aSourceComponent, aMessageRequest);
                    break;

                case WC:
                    sendToWalletComponent(aSourceComponent, aMessageRequest);
                    break;

                case RC:
                    sendToRouterComponent(aSourceComponent, aMessageRequest);
                    break;

                default:
                    break;
            }
        }
        catch (final Exception e)
        {
            sendToErrorLog(aSourceComponent, aMessageRequest, e);
        }
    }

    public static void sendToPlatformRejection(
            Component aComponent,
            MessageRequest aMessageRequest)
    {

        try
        {
            if (log.isDebugEnabled())
                log.debug("Request sending to PRC topic .. " + aMessageRequest);

            aMessageRequest.setPlatfromRejected(true);
            MessageProcessor.writeMessage(aComponent, Component.PRC, aMessageRequest);
        }
        catch (final ItextosException e)
        {
            log.error("Exception occer while sending to Platfrom Rejection topic ..", e);
            sendToErrorLog(aComponent, aMessageRequest, e);
        }
    }

    public static void sendToWalletComponent(
            Component aComponent,
            MessageRequest aMessageRequest)
    {

        try
        {
            if (log.isDebugEnabled())
                log.debug("Request sending to WC topic .. " + aMessageRequest);

            MessageProcessor.writeMessage(aComponent, Component.WC, aMessageRequest);
        }
        catch (final ItextosException e)
        {
            log.error("Exception occer while sending to Prepaid topic ..", e);
            sendToErrorLog(aComponent, aMessageRequest, e);
        }
    }

    public static void sendToRouterComponent(
            Component aComponent,
            MessageRequest aMessageRequest)
    {

        try
        {
            if (log.isDebugEnabled())
                log.debug("Request sending to RC topic .. " + aMessageRequest);

            MessageProcessor.writeMessage(aComponent, Component.RC, aMessageRequest);
        }
        catch (final ItextosException e)
        {
            log.error("Exception occer while sending to Route Consumer topic ..", e);
            sendToErrorLog(aComponent, aMessageRequest, e);
        }
    }

    public static void sendToErrorLog(
            Component aComponent,
            MessageRequest aBaseMessage,
            Exception aErrorMsg)
    {

        try
        {
            if (log.isDebugEnabled())
                log.debug("Request sending to ERROR topic .. " + aBaseMessage);

            PlatformUtil.sendToErrorLog(aComponent, aBaseMessage, aErrorMsg);
        }
        catch (final Exception e)
        {
            log.error("Exception while sending request to error log", e);
        }
    }

    private static void doFeatureCodeIdentifier(
            MessageRequest aMessagaeRequest)
    {
        new FCFinder(aMessagaeRequest).splitMessageProcess();
    }

}
