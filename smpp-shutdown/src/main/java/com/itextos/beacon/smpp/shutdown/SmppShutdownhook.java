package com.itextos.beacon.smpp.shutdown;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.itextos.beacon.smpp.interfaces.StartApplication;

public class SmppShutdownhook
        extends
        Thread
{

    private static final Log       log = LogFactory.getLog(SmppShutdownhook.class);


    public SmppShutdownhook( )
    {
       
    }

    @Override
    public void run()
    {

        ServerShutDown.shutdown();
    }

}
