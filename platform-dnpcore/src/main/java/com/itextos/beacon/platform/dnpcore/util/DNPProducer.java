package com.itextos.beacon.platform.dnpcore.util;

import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.itextos.beacon.commonlib.constants.Component;
import com.itextos.beacon.commonlib.constants.exception.ItextosException;
import com.itextos.beacon.commonlib.message.BaseMessage;
import com.itextos.beacon.commonlib.message.DeliveryObject;
import com.itextos.beacon.commonlib.messageprocessor.process.MessageProcessor;
import com.itextos.beacon.inmemory.clientdlrpreferences.ClientDlrConfig;
import com.itextos.beacon.inmemory.clientdlrpreferences.ClientDlrConfigUtil;
import com.itextos.beacon.platform.msgflowutil.util.PlatformUtil;

public class DNPProducer
{

    private static final Log log = LogFactory.getLog(DNPProducer.class);

    private DNPProducer()
    {}

    public static void sendToNextComponents(
            Map<Component, DeliveryObject> aNextProcess)
    {
        aNextProcess.entrySet().stream().forEach(e -> {

            try
            {
                if (log.isDebugEnabled())
                    log.debug(e.getValue().getMessageId()+" : Sending to Next Component : " + e.getKey() + ":: " + e.getValue());

                if (Component.HTTP_DLR == e.getKey())
                {
                    final ClientDlrConfig lClientDlrConfig = ClientDlrConfigUtil.getDlrHandoverConfig(e.getValue().getClientId(), "sms", e.getValue().getInterfaceType(),
                            e.getValue().isDlrRequestFromClient());

                    if (lClientDlrConfig != null)
                    {
                        if (log.isDebugEnabled())
                            log.debug("Client Dlr Config : " + lClientDlrConfig);

                        if (lClientDlrConfig.isClientSpecificHttpTopic())
                        {
                            if (log.isDebugEnabled())
                                log.debug("Sending to Http Client Handover specific topic...");

                            MessageProcessor.writeMessage(Component.DNP, e.getKey(), e.getValue(), true);
                        }
                        else
                            MessageProcessor.writeMessage(Component.DNP, e.getKey(), e.getValue());
                    }
                    else
                        log.warn("DlrHandover not configure for client : " + e.getValue().getClientId());
                }
                else
                    MessageProcessor.writeMessage(Component.DNP, e.getKey(), e.getValue());
            }
            catch (final ItextosException e1)
            {
                log.error("Exception occer while sending to " + e.getKey() + " ..", e1);
                sendToErrorLog(e.getValue(), e1);
            }
            catch (final Exception e2)
            {
                log.error("Exception occer while sending to " + e.getKey() + " ..", e2);
                sendToErrorLog(e.getValue(), e2);
            }
        });
    }

    public static void sendToErrorLog(
            BaseMessage aBaseMessage,
            Exception aErrorMsg)
    {

        try
        {
            PlatformUtil.sendToErrorLog(Component.DNP, aBaseMessage, aErrorMsg);
        }
        catch (final Exception e21)
        {
            log.error("Exception while sending request to error log. " + aBaseMessage, e21);
        }
    }

}