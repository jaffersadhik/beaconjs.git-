package com.itextos.beacon.platform.sbccore;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.itextos.beacon.commonlib.componentconsumer.processor.ProcessorInfo;
import com.itextos.beacon.commonlib.constants.Component;
import com.itextos.beacon.platform.sbccore.data.InmemBlockoutQReaper;
import com.itextos.beacon.platform.sbccore.data.InmemoryBlockoutReaper;
import com.itextos.beacon.platform.sbccore.data.InmemoryScheduleReaper;

public class StartApplication
{

    private static final Log       log            = LogFactory.getLog(StartApplication.class);
    private static final Component THIS_COMPONENT = Component.SBC;

    public static void main(
            String[] args)
    {
        if (log.isDebugEnabled())
            log.debug("Starting the application " + THIS_COMPONENT);

        try
        {
            startInutialParam();

            final ProcessorInfo lProcessor = new ProcessorInfo(THIS_COMPONENT);
            lProcessor.process();
        }
        catch (final Exception e)
        {
            log.error("Exception while starting component '" + THIS_COMPONENT + "'", e);
            System.exit(-1);
        }
    }

    private static void startInutialParam()
    {
        if (log.isDebugEnabled())
            log.debug("Starting the application InmemReapers ...");

        InmemBlockoutQReaper.getInstance();
        InmemoryBlockoutReaper.getInstance();
        InmemoryScheduleReaper.getInstance();
    }

}
