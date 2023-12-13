package com.itextos.beacon.platform.blockoutwalletconsumer.util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.itextos.beacon.commonlib.constants.Component;
import com.itextos.beacon.commonlib.constants.exception.ItextosException;
import com.itextos.beacon.commonlib.messageobject.MessageRequest;
import com.itextos.beacon.commonlib.messageprocessor.process.MessageProcessor;
import com.itextos.beacon.platform.msgflowutil.util.PlatformUtil;

public class BWCProducer
{

    private static final Log log = LogFactory.getLog(BWCProducer.class);

    private BWCProducer()
    {}

    public static void sendToCarrierHandover(
            MessageRequest aMessageRequest)
    {

        try
        {
            if (log.isDebugEnabled())
                log.debug("Request sending to CH topic .. " + aMessageRequest);

            MessageProcessor.writeMessage(Component.BWC, Component.CH, aMessageRequest);
        }
        catch (final ItextosException e)
        {
            log.error("Exception occer while sending to Carrier Handover Consumer topic ..", e);
            sendToErrorLog(Component.BWC, aMessageRequest, e);
        }
    }

    public static void sendToPlatformRejection(
            MessageRequest aMessageRequest)
    {

        try
        {
            if (log.isDebugEnabled())
                log.debug("Request sending to PRC topic .. " + aMessageRequest);
            aMessageRequest.setPlatfromRejected(true);
            MessageProcessor.writeMessage(Component.BWC, Component.PRC, aMessageRequest);
        }
        catch (final ItextosException e)
        {
            log.error("Exception occer while sending to Platfrom Rejection topic ..", e);
            sendToErrorLog(Component.BWC, aMessageRequest, e);
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

}
