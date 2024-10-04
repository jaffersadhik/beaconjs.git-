package com.itextos.beacon.platform.kannelstatusupdater.process.response;

import com.itextos.beacon.commonlib.constants.TimerIntervalConstant;
import com.itextos.beacon.commonlib.utility.timer.ITimedProcess;
import com.itextos.beacon.commonlib.utility.timer.ScheduledTimedProcessorForSpleepOfEachExecution;
import com.itextos.beacon.platform.kannelstatusupdater.utility.Utility;

public class KannelStatsRedisUpdater
        implements
        ITimedProcess
{

    private boolean        canContinue     = true;
  //  private ScheduledTimedProcessorForSpleepOfEachExecution mTimedProcessor = null;

    private static class SingletonHolder
    {

        @SuppressWarnings("synthetic-access")
        static final KannelStatsRedisUpdater INSTANCE = new KannelStatsRedisUpdater();

    }

    public static KannelStatsRedisUpdater getInstance()
    {
        return SingletonHolder.INSTANCE;
    }

    private KannelStatsRedisUpdater()
    {
    	/*
        mTimedProcessor = new ScheduledTimedProcessorForSpleepOfEachExecution("KannelStatsUpdateReaper", this, TimerIntervalConstant.KANNEL_RESPONSE_REFRESH);
     //   mTimedProcessor.start();
        Thread virtualThreadInstance = Thread.ofVirtual().start(mTimedProcessor);
		*/
    	ScheduledTimedProcessorForSpleepOfEachExecution.getInstance().start("KannelStatsUpdateReaper", this, TimerIntervalConstant.KANNEL_RESPONSE_REFRESH);
    }

    @Override
    public boolean canContinue()
    {
        return canContinue;
    }

    @Override
    public boolean processNow()
    {
        Utility.calculateTimeAndUpdateRedis();
        Utility.removeOldEntries();

        return false;
    }

    @Override
    public void stopMe()
    {
        canContinue = false;
    }

}
