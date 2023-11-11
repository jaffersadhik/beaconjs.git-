package com.itextos.beacon.platform.interimfailuers;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.itextos.beacon.commonlib.constants.Component;
import com.itextos.beacon.commonlib.kafka.processor.ProcessorInfo;

public class StartApplication
{

    private static final Log log = LogFactory.getLog(StartApplication.class);

    public static void main(
            String[] args)
    {

        try
        {
            if (log.isDebugEnabled())
                log.debug("Starting the application " + Component.T2DB_INTERIM_FAILUERS);

            final ProcessorInfo lFullMsgProcessor = new ProcessorInfo(Component.T2DB_INTERIM_FAILUERS);
            lFullMsgProcessor.process();
        }
        catch (final Exception e)
        {
            log.error("Exception occer while starting Platform Interim Failuers component ..", e);
            System.exit(-1);
        }
    }

}
