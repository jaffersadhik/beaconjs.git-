package com.itextos.beacon.splog;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


public class SPLog {

    private static final Log                                                log                           = LogFactory.getLog(SPLog.class);

    public static void log(String data) {
    	
    	System.out.println("SPLOG WRITE");
    	log.error(data);
    }
}
