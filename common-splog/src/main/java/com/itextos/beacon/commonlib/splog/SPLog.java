package com.itextos.beacon.commonlib.splog;

import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


public class SPLog {

    private static final Log                                                log                           = LogFactory.getLog(SPLog.class);

    public static void log(String data) {
    	
    	log.error(data);
    }
}
