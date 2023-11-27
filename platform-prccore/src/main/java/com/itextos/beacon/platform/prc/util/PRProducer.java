package com.itextos.beacon.platform.prc.util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.itextos.beacon.commonlib.constants.Component;
import com.itextos.beacon.commonlib.constants.exception.ItextosException;
import com.itextos.beacon.commonlib.messageobject.BaseMessage;
import com.itextos.beacon.commonlib.messageobject.DeliveryObject;
import com.itextos.beacon.commonlib.messageobject.SubmissionObject;
import com.itextos.beacon.commonlib.messageprocessor.process.MessageProcessor;
import com.itextos.beacon.platform.msgflowutil.util.PlatformUtil;

public class PRProducer
{

    private static final Log log = LogFactory.getLog(PRProducer.class);

    private PRProducer()
    {}

    public static void sendToBillerTopic(
            SubmissionObject aSubmissionObject)
    {

        try
        {
            MessageProcessor.writeMessage(Component.PRC, Component.SUBBC, aSubmissionObject);
        }
        catch (final ItextosException e)
        {
            log.error("Exception occer while Final Processor MT ..", e);
            sendToErrorLog(aSubmissionObject, e);
        }
    }

    public static void sendToDLRTopic(
            DeliveryObject aDeliveryObject)
    {

        try
        {
            MessageProcessor.writeMessage(Component.PRC, Component.DLRINTLP, aDeliveryObject);
        }
        catch (final ItextosException e)
        {
            log.error("Exception occer while Dlr internal processor ..", e);
            sendToErrorLog(aDeliveryObject, e);
        }
    }

    public static void sendToErrorLog(
            BaseMessage aBaseMessage,
            Exception aErrorMsg)
    {

        try
        {
            PlatformUtil.sendToErrorLog(Component.PRC, aBaseMessage, aErrorMsg);
        }
        catch (final Exception e)
        {
            log.error("Exception while sending request to error log. " + aBaseMessage, e);
        }
    }

}
