package com.itextos.beacon.platform.ic;

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
        if (log.isDebugEnabled())
            log.debug("Starting the application " + Component.IC);

        try
        {
            final ProcessorInfo lProcessor = new ProcessorInfo(Component.IC);
            lProcessor.process();
        }
        catch (final Exception e)
        {
            log.error("Exception while starting the Interface Consumer.", e);
            System.exit(-1);
        }
    }

}