package com.itextos.beacon.platform.dlrvoiceretry;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.itextos.beacon.commonlib.componentconsumer.processor.ProcessorInfo;
import com.itextos.beacon.commonlib.constants.Component;

public class StartApplication
{

    private static final Log log = LogFactory.getLog(StartApplication.class);

    public static void main(
            String[] args)
    {

        try
        {
          
           if (log.isDebugEnabled())
                log.debug("Starting the application " + Component.VOICE_PROCESS);

            final ProcessorInfo lDlrVoiceRetry = new ProcessorInfo(Component.VOICE_PROCESS, false);
            lDlrVoiceRetry.process();
        }
        catch (final Exception e)
        {
            log.error("Exception while starting the Retry Processor", e);
            System.exit(-1);
        }
    }

}