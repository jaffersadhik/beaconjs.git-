package com.itextos.beacon.r3r.process;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class DataProcessor
{

    private static final Log log = LogFactory.getLog(DataProcessor.class);

    private static class SingletonHolder
    {

        @SuppressWarnings("synthetic-access")
        static final DataProcessor INSTANCE = new DataProcessor();

    }

    public static DataProcessor getInstance()
    {
        return SingletonHolder.INSTANCE;
    }

    private final DBProcessorThread          dbProcessorThread          = new DBProcessorThread();
    private final UrlRequestDetailsProcessor urlRequestDetailsProcessor = new UrlRequestDetailsProcessor();

    private DataProcessor()
    {
        initDBProcessor();
        initRequestDataProcessor();
    }

    private void initDBProcessor()
    {
    	/*
        final Thread dbThread = new Thread(dbProcessorThread, "DB Process Thread");
        dbThread.start();
        */
    	 Thread virtualThread = Thread.ofVirtual().start(dbProcessorThread);

         virtualThread.setName( "DB Process Thread");
        if (log.isDebugEnabled())
            log.debug("DB Processor Thread Started ");
    }

    private void initRequestDataProcessor()
    {
    	/*
        final Thread urlThread = new Thread(urlRequestDetailsProcessor, "URL Request Processor");
        urlThread.start();
		*/
    	 Thread virtualThread = Thread.ofVirtual().start(urlRequestDetailsProcessor);

         virtualThread.setName( "URL Request Processor");
         
        if (log.isDebugEnabled())
            log.debug("URL Request Details Processor Started ");
    }

    public void stopMe()
    {

        try
        {
            dbProcessorThread.stopMe();
            urlRequestDetailsProcessor.stopMe();
        }
        catch (final Exception e)
        {
            log.error("Exception while stopping the dbProcessorThread or urlRequestDetailsProcessor", e);
        }
    }

}