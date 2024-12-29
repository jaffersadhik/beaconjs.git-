package com.itextos.beacon.platform.dnaginggenerator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.itextos.beacon.platform.dnaginggenerator.process.AgingDlrGenHolder;

public class StartApplication
{

    private static Log log = LogFactory.getLog(StartApplication.class);

    public static void start()
    {

        try
        {
            AgingDlrGenHolder.getInstance();
        }
        catch (final Exception e)
        {
            log.error("Exception occer while starting the SBP...", e);
        }
    }

    public static void main(
            String[] args)
    {
        StartApplication.start();
    }

}
