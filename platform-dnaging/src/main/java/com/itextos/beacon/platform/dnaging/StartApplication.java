package com.itextos.beacon.platform.dnaging;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.itextos.beacon.commonlib.componentconsumer.processor.ProcessorInfo;
import com.itextos.beacon.commonlib.constants.Component;

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
            
    
            

            if (log.isDebugEnabled())
                log.debug("Starting the application " + Component.ADNP);

            final ProcessorInfo lAgingDnProcessor = new ProcessorInfo(Component.ADNP, false);
            lAgingDnProcessor.process();

         

      
      
           
        }
        catch (final Exception e)
        {
            log.error("Exception occer while processing the DLR Services.. Hence stop the service..", e);
            System.exit(-1);
        }
    }

}
