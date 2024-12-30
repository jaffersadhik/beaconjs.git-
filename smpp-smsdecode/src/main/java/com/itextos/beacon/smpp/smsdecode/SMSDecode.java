package com.itextos.beacon.smpp.smsdecode;

import java.util.Arrays;
import java.util.Map;

import com.cloudhopper.commons.charset.Charset;
import com.cloudhopper.commons.charset.CharsetUtil;
import com.cloudhopper.commons.util.HexUtil;
import com.cloudhopper.smpp.pdu.SubmitSm;
import com.itextos.beacon.commonlib.constants.MessageClass;
import com.itextos.beacon.commonlib.constants.MiddlewareConstant;
import com.itextos.beacon.commonlib.utility.CommonUtility;
import com.itextos.beacon.smpp.dto.SessionDetail;
import com.itextos.beacon.smpp.messagetype.MessageType;
import com.itextos.beacon.smpp.utils.ItextosSmppConstants;
import com.itextos.beacon.smpp.utils.UdhExtractor;
import com.itextos.beacon.smpp.utils.enums.SmppCharset;

public class SMSDecode {

	
	  public static SMSBodyContent updateMessageAndMessageClass(SubmitSm mSubmitSm,int mDcs,String mEsmClass,SessionDetail mSessionDetail)
	    {

		  SMSBodyContent smsbodycontent = new SMSBodyContent();
	        /**
	         * In MIDE the message length more than 160 characters will be encrypted
	         * and set in the Messagepayload and privacy indicator will be set to 1.
	         * If the esm value is set then UDH will be splitted from the message.
	         */
	        try
	        {

	            if ((mEsmClass != null) && (mEsmClass.equals(ItextosSmppConstants.ESM_CLASS_40) || mEsmClass.equals(ItextosSmppConstants.ESM_CLASS_43)))
	            {
	                // Multipart Message
	                final String msgWithHeader = HexUtil.toHexString(mSubmitSm.getShortMessage());
	                spiltMsgWithHeader(msgWithHeader,smsbodycontent,mDcs);

	              String  mMessage = (smsbodycontent.getmMessage() == null) ? "" : smsbodycontent.getmMessage();
	                if (!mMessage.isBlank())
	                	mMessage=SMSDecode.convertMessageFromMessage(smsbodycontent.ismIsHexMsg() ? mMessage.getBytes() : HexUtil.toByteArray(mMessage),mDcs,mSessionDetail);

	             String   mUdhi = "0";
	             smsbodycontent.setmUdhi(mUdhi);
	            }
	            else
	            {
	         
	                if ((mDcs == ItextosSmppConstants.DCS_8) || (mDcs == ItextosSmppConstants.DCS_18) || (mDcs == ItextosSmppConstants.DCS_24))
	                {
	                    // Single Part Unicode
	                String    mMessage  = HexUtil.toHexString(mSubmitSm.getShortMessage());
	                boolean    mIsHexMsg = true;

	                smsbodycontent.setmMessage(mMessage);
	                smsbodycontent.setmIsHexMsg(mIsHexMsg);
	                }
	                else
	                {
	                  
	                   
	                  String  mMessage=SMSDecode.convertMessageFromObject(mSubmitSm,mDcs,mSessionDetail);


	                   mMessage = (mMessage == null) ? "" : mMessage;
	                   
	                   smsbodycontent.setmMessage(mMessage);
	                }
	            }

	            if (mDcs != ItextosSmppConstants.DCS_INVALID)
	            {
	                final Map map = UdhExtractor.extractParams(smsbodycontent.getmUdh(), true);
	                int  mDestPort=CommonUtility.getInteger(CommonUtility.nullCheck(map.get(MiddlewareConstant.MW_DESTINATION_PORT)));
	               MessageClass mMsgclass= MessageType.checkMsgType(mDestPort,mDcs);
	               smsbodycontent.setmDestPort(mDestPort);
	            }
	            else {
	            	MessageClass mMsgclass = MessageClass.PLAIN_MESSAGE;
	            	smsbodycontent.setmMsgclass(mMsgclass);
	            }
	        }
	        catch (final Exception e)
	        {
	        }
	        
	        return smsbodycontent;
	    }

		
	   private static  void spiltMsgWithHeader(
	            String msgWithHeader,SMSBodyContent smsbodycontent,int mDcs)
	    {
	        final String headerLen = msgWithHeader.substring(0, 2);
	        final int    totLen    = Integer.parseInt(headerLen, 16);

	      String  mUdh = msgWithHeader.substring(0, ((totLen * 2) + 2));
	        smsbodycontent.setmUdh(mUdh);


	        final String msgHex = msgWithHeader.substring(mUdh.length(), msgWithHeader.length());

	        if ((mDcs == ItextosSmppConstants.DCS_INVALID) || (mDcs == ItextosSmppConstants.DCS_16) || (mDcs == ItextosSmppConstants.DCS_ZERO) || (mDcs == ItextosSmppConstants.DCS_12)
	                || (mDcs == ItextosSmppConstants.DCS_MINUS_16) || (mDcs == ItextosSmppConstants.DCS_4))
	            if (mUdh.toUpperCase().indexOf(ItextosSmppConstants.UDH_158_A) > 0)
	            {
	              String  mMessage  = msgHex;
	              smsbodycontent.setmMessage(mMessage);
	             boolean   mIsHexMsg = true;
	             
	             smsbodycontent.setmIsHexMsg(mIsHexMsg);
	            }
	            else
	            {
	                // mMessage = new String(HexUtil.toByteArray(msgHex));
	            	String mMessage = msgHex;
		              smsbodycontent.setmMessage(mMessage);

	             
	            }
	        else
	        {
	           String mMessage  = msgHex;
	          boolean  mIsHexMsg = true;
              smsbodycontent.setmMessage(mMessage);
	             smsbodycontent.setmIsHexMsg(mIsHexMsg);


	        }

	    }

	   
	   
		 private static  String convertMessageFromObject(SubmitSm mSubmitSm,int mDcs,SessionDetail mSessionDetail)
		    {
		       return  convertMessageFromMessage(mSubmitSm.getShortMessage(),mDcs,mSessionDetail);
		    }
	
		 private  static String convertMessageFromMessage(
	            byte[] aMessageInBytes,int              mDcs,SessionDetail    mSessionDetail)
	    {
	    	
	    	String           mMessage =null;
	        String charset = "";

	        if ((mDcs == 3) || (mDcs == 1))
	            charset = SmppCharset.ISO_8859_1.getKey();
	        else
	            charset = mSessionDetail.getSmppCharSet();

	        
	   

	        Charset           toUserCharSet = CharsetUtil.CHARSET_ISO_8859_1;

	        final SmppCharset lCharset      = SmppCharset.getCharset(charset);

	        switch (lCharset)
	        {
	            case GSM:
	                toUserCharSet = CharsetUtil.CHARSET_GSM;
	                break;

	            case ISO_8859_1:
	                toUserCharSet = CharsetUtil.CHARSET_ISO_8859_1;
	                break;

	            case ISO_8859_15:
	                toUserCharSet = CharsetUtil.CHARSET_ISO_8859_15;
	                break;

	            case UTF_8:
	                toUserCharSet = CharsetUtil.CHARSET_UTF_8;
	                break;

	            case UCS_2:
	                toUserCharSet = CharsetUtil.CHARSET_UCS_2;
	                break;

	            case GSM8:
	                toUserCharSet = CharsetUtil.CHARSET_GSM8;
	                break;

	            case GSM7:
	                toUserCharSet = CharsetUtil.CHARSET_GSM7;
	                break;

	            default:
	                toUserCharSet = CharsetUtil.CHARSET_ISO_8859_1;
	                break;
	        }

	  
	        mMessage = CharsetUtil.decode(aMessageInBytes, toUserCharSet);

	       return mMessage;
	    }

}
