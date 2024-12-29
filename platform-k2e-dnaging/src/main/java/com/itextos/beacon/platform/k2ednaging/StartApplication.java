package com.itextos.beacon.platform.k2ednaging;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.itextos.beacon.commonlib.componentconsumer.processor.ProcessorInfo;
import com.itextos.beacon.commonlib.constants.Component;
import com.itextos.beacon.commonlib.utility.CommonUtility;

public class StartApplication
{

    private static final Log    log          = LogFactory.getLog(StartApplication.class);

    private static final String DLR_MT       = "dlrmt";
    private static final String DLR_DN       = "dlrdn";
    private static final String AGING        = "age";
    private static final String AGING_UPDATE = "ageupdate";

    public static void main(
            String[] args)
    {

        try
        {
         
             if (log.isDebugEnabled())
                    log.debug("Starting the application " + Component.AGIN);

                final ProcessorInfo lAgingInsertProcessor = new ProcessorInfo(Component.AGIN, false);
                lAgingInsertProcessor.process();
          
        }
        catch (final Exception e)
        {
            log.error("Exception while starting the T2E application", e);
            System.exit(-1);
        }
    }

}
