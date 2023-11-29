package com.itextos.beacon.platform.kannelstatusupdater;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.itextos.beacon.platform.kannelstatusupdater.process.KannelStatusRefresher;

public class StartApplication
{

    private static final Log log = LogFactory.getLog(StartApplication.class);

    public static void main(
            String[] args)
    {
        if (log.isDebugEnabled())
            log.debug("Starting the application Kannel Status Updater");

        try
        {
            KannelStatusRefresher.getInstance();
        }
        catch (final Exception e)
        {
            log.error("Exception while staring the Kannel status Refresher.", e);
            System.exit(-1);
        }
    }

}
