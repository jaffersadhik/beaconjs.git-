package com.itextos.beacon.smpp.featurecode;

import com.itextos.beacon.commonlib.constants.FeatureCode;
import com.itextos.beacon.commonlib.constants.MessageClass;

public class PlatformFeatureCode {

	
	 public static FeatureCode setFeatureCode(MessageClass     mMsgclass,boolean          isConcatMessage)
	    {

		 FeatureCode      mFeatureCode =null;
	        switch (mMsgclass)
	        {
	            case PLAIN_MESSAGE:
	                mFeatureCode = isConcatMessage ? FeatureCode.PLAIN_MESSAGE_MULTI : FeatureCode.PLAIN_MESSAGE_SINGLE;
	                break;

	            case UNICODE_MESSAGE:
	                mFeatureCode = isConcatMessage ? FeatureCode.UNICODE_MULTI : FeatureCode.UNICODE_SINGLE;
	                break;

	            case FLASH_PLAIN_MESSAGE:
	                mFeatureCode = isConcatMessage ? FeatureCode.FLASH_PLAIN_MESSAGE_MULTI : FeatureCode.FLASH_PLAIN_MESSAGE_SINGLE;
	                break;

	            case FLASH_UNICODE_MESSAGE:
	                mFeatureCode = isConcatMessage ? FeatureCode.FLASH_UNICODE_MULTI : FeatureCode.FLASH_UNICODE_SINGLE;
	                break;

	            case SP_PLAIN_MESSAGE:
	                mFeatureCode = isConcatMessage ? FeatureCode.SPECIAL_PORT_PLAIN_MESSAGE_MULTI : FeatureCode.SPECIAL_PORT_PLAIN_MESSAGE_SINGLE;
	                break;

	            case SP_UNICODE_MESSAGE:
	                mFeatureCode = isConcatMessage ? FeatureCode.SPECIAL_PORT_UNICODE_MULTI : FeatureCode.SPECIAL_PORT_UNICODE_SINGLE;
	                break;

	            case BINARY_MESSAGE:
	                mFeatureCode = FeatureCode.BINARY_MSG;
	                break;

	            default:
	                mFeatureCode = FeatureCode.BINARY_MSG;
	                break;
	        }
	        
	        return mFeatureCode;
	    }
}
