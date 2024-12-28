package com.itextos.beacon.platform.dnnopayloadreceiver;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.itextos.beacon.commonlib.componentconsumer.processor.ProcessorInfo;
import com.itextos.beacon.commonlib.constants.Component;
import com.itextos.beacon.platform.dnnopayloadreceiver.inmem.NoPayloadRetryQReaper;

public class StartApplication
{

    public static final Log log = LogFactory.getLog(StartApplication.class);

    public static void main(
            String[] args)
    {

        try
        {
             
            final String cluster=System.getProperty("cluster");
            
            if(cluster==null) {
            
            	if(System.getenv("cluster")!=null) {
            		System.setProperty("cluster", System.getenv("cluster"));
            	}
            }

            final String modvalue=System.getProperty("modvalue");
            
            if(modvalue==null) {
            	
            	System.setProperty("modvalue", System.getenv("modvalue"));

            }
            
            final ProcessorInfo lDnProcessor = new ProcessorInfo(Component.NPR);
            lDnProcessor.process();

            if (log.isDebugEnabled())
                log.debug("Starting the application " + Component.NPR);

    
         

            
            
            NoPayloadRetryQReaper.getInstance();


      
           
        }
        catch (final Exception e)
        {
            log.error("Exception occer while processing the DLR Services.. Hence stop the service..", e);
            System.exit(-1);
        }
    }

}
