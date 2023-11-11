package com.itextos.beacon.platform.ch;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.itextos.beacon.commonlib.constants.Component;
import com.itextos.beacon.commonlib.kafka.processor.ProcessorInfo;

public class StartApplication
{

    private static final Log       log            = LogFactory.getLog(StartApplication.class);
    private static final Component THIS_COMPONENT = Component.CH;

    public static void main(
            String[] args)
    {
        if (log.isDebugEnabled())
            log.debug("Starting the application " + THIS_COMPONENT);

        try
        {
        	
        	String cluster=System.getProperty("pf.cluster");
        	
        	 if(cluster==null) {
                 
             	System.setProperty("pf.cluster", System.getenv("cluster"));
             }
            final ProcessorInfo lProcessor = new ProcessorInfo(THIS_COMPONENT);
            
            lProcessor.process();
        }
        catch (final Exception e)
        {
            log.error("Exception while starting component '" + THIS_COMPONENT + "'", e);
            System.exit(-1);
        }
    }

}