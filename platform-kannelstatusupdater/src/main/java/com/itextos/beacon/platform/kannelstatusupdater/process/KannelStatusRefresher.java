package com.itextos.beacon.platform.kannelstatusupdater.process;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.itextos.beacon.commonlib.constants.TimerIntervalConstant;
import com.itextos.beacon.commonlib.utility.timer.ITimedProcess;
import com.itextos.beacon.commonlib.utility.timer.ScheduledTimedProcessorForSpleepOfEachExecution;

public class KannelStatusRefresher
        implements
        ITimedProcess
{

    private static final Log log = LogFactory.getLog(KannelStatusRefresher.class);

    private static class SingletonHolder
    {

        @SuppressWarnings("synthetic-access")
        static final KannelStatusRefresher INSTANCE = new KannelStatusRefresher();

    }

    public static KannelStatusRefresher getInstance()
    {
        return SingletonHolder.INSTANCE;
    }

  //  private ScheduledTimedProcessorForSpleepOfEachExecution mTimedProcessor = null;
    private boolean        canContinue     = true;

    private KannelStatusRefresher()
    {
    	
        final int timeInterval = 10;
        /*
        mTimedProcessor = new ScheduledTimedProcessorForSpleepOfEachExecution("KannelStatusRefresher", this, TimerIntervalConstant.KANNEL_STATUS_REFRESH);
       // mTimedProcessor.start();
        Thread virtualThreadInstance = Thread.ofVirtual().start(mTimedProcessor);
	*/
        ScheduledTimedProcessorForSpleepOfEachExecution.getInstance().start("KannelStatusRefresher", this, TimerIntervalConstant.KANNEL_STATUS_REFRESH);
        if (log.isInfoEnabled())
            log.info("Kannel Status Refresher started with " + timeInterval + " time interval.");
    }

    @Override
    public boolean canContinue()
    {
        return canContinue;
    }

    @Override
    public boolean processNow()
    {

        try
        {
            DataCollector.getKannelStatusData();
            if (log.isDebugEnabled())
                log.debug("Kannel Refresh completed.");
        }
        catch (final Exception e)
        {
            log.error("Exception while refreshing the Kannel Status", e);
        }
        return false;
    }

    @Override
    public void stopMe()
    {
        canContinue = false;
    }

}