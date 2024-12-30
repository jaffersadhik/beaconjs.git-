package com.itextos.beacon.smpp.messagetype;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.itextos.beacon.commonlib.constants.MessageClass;

public class MessageType {
	
    private static final Log log                  = LogFactory.getLog(MessageType.class);

	  public static MessageClass checkMsgType(
	            int aDestPort,int mDcs )
	    {
		  MessageClass     mMsgclass            = null;
	        if (log.isDebugEnabled())
	            log.debug("destPort : " + aDestPort);

	        switch (mDcs)
	        {
	            case 11:
	            case -11:
	                mMsgclass = MessageClass.BINARY_MESSAGE;
	                break;

	            case 4:
	                mMsgclass = MessageClass.SP_PLAIN_MESSAGE;
	                break;

	            case 12:
	                mMsgclass = MessageClass.SP_PLAIN_MESSAGE;
	                break;

	            case 8:
	                if ((aDestPort == 0))
	                    mMsgclass = MessageClass.UNICODE_MESSAGE;
	                else
	                    mMsgclass = MessageClass.SP_UNICODE_MESSAGE;
	                break;

	            case 16:
	            case -16:
	                mMsgclass = MessageClass.FLASH_PLAIN_MESSAGE;
	                break;

	            case 18:
	            case 24:
	                mMsgclass = MessageClass.FLASH_UNICODE_MESSAGE;
	                break;

	            case 0:
	                mMsgclass = MessageClass.PLAIN_MESSAGE;
	                break;

	            default:
	                mMsgclass = MessageClass.PLAIN_MESSAGE;
	                break;
	        }
	        
	        return mMsgclass;
	    }

}
