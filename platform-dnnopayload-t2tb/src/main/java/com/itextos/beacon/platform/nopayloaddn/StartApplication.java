package com.itextos.beacon.platform.nopayloaddn;

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
        if (log.isDebugEnabled())
            log.debug("Starting the application " + Component.T2DB_NO_PAYLOAD_DN);

        try
        {
            final ProcessorInfo lErrorProcessor = new ProcessorInfo(Component.T2DB_NO_PAYLOAD_DN);
            lErrorProcessor.process();
        }
        catch (final Exception e)
        {
            log.error("Exception occer while starting Platform '" + Component.T2DB_NO_PAYLOAD_DN + "' component ..", e);
            System.exit(-1);
        }
    }

}