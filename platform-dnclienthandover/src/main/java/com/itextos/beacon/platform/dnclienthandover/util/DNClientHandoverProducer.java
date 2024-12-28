package com.itextos.beacon.platform.dnclienthandover.util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.itextos.beacon.commonlib.constants.Component;
import com.itextos.beacon.commonlib.constants.exception.ItextosException;
import com.itextos.beacon.commonlib.message.BaseMessage;
import com.itextos.beacon.commonlib.message.DeliveryObject;
import com.itextos.beacon.commonlib.messageprocessor.process.MessageProcessor;
import com.itextos.beacon.platform.msgflowutil.util.PlatformUtil;
import com.itextos.beacon.smslog.DNPLog;

public class DNClientHandoverProducer
{

    private static final Log log = LogFactory.getLog(DNClientHandoverProducer.class);

    private DNClientHandoverProducer()
    {}

    public static void sendToHttpDLRHandover(Component fromcomponent,DeliveryObject object)
    {

            try
            {
    
                DNPLog.getInstance( object.getClientId()).log( object.getClientId(), object.getFileId()+ " : "+  object.getMessageId() + " : Sending to Next Component :  "+Component.HTTP_DLR);

                MessageProcessor.writeMessage(fromcomponent, Component.HTTP_DLR, object, true);
                     
              
            }
            catch (final ItextosException e1)
            {
                sendToErrorLog(fromcomponent,object, e1);
            }
            catch (final Exception e2)
            {
                sendToErrorLog(fromcomponent,object, e2);
            }
        
    }

    
    public static void sendToSmppDLRHandover(Component fromcomponent,DeliveryObject object)
    {

            try
            {
    
                DNPLog.getInstance( object.getClientId()).log( object.getClientId(), object.getFileId()+ " : "+  object.getMessageId() + " : Sending to Next Component :  "+Component.SMPP_DLR);

                MessageProcessor.writeMessage(fromcomponent, Component.SMPP_DLR, object, true);
                     
              
            }
            catch (final ItextosException e1)
            {
                sendToErrorLog(fromcomponent,object, e1);
            }
            catch (final Exception e2)
            {
                sendToErrorLog(fromcomponent,object, e2);
            }
        
    }

    
    public static void sendToErrorLog(Component fromComponent,
            BaseMessage aBaseMessage,
            Exception aErrorMsg)
    {

        try
        {
            PlatformUtil.sendToErrorLog(fromComponent, aBaseMessage, aErrorMsg);
        }
        catch (final Exception e21)
        {
            log.error("Exception while sending request to error log. " + aBaseMessage, e21);
        }
    }

}