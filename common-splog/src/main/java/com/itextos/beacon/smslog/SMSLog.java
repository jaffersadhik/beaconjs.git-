package com.itextos.beacon.smslog;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


public class SMSLog {

    private static final Log                                                log                           = LogFactory.getLog(SMSLog.class);

    public static void log(String string) {
    	
    	if(log.isDebugEnabled()) {
    		
        	log.debug(string);

    	}
    	
    }
}
