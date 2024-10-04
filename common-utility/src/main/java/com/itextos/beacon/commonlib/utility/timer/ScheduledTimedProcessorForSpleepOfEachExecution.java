package com.itextos.beacon.commonlib.utility.timer;


import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.itextos.beacon.commonlib.constants.TimerIntervalConstant;

public class ScheduledTimedProcessorForSpleepOfEachExecution {
    private static final Log log = LogFactory.getLog(ScheduledTimedProcessorForSpleepOfEachExecution.class);
    private static final long DEFAULT_SLEEP_TIME_MILLIS = 1000;

    private static ScheduledTimedProcessorForSpleepOfEachExecution instance;
    private final ScheduledExecutorService scheduler;

    private final String mThreadName;
    private final ITimedProcess mTimedProcess;
    private long mSleepTimeInMilliSeconds = DEFAULT_SLEEP_TIME_MILLIS;
    private boolean mStoppedExternally = false;

    // Private constructor for Singleton
    private ScheduledTimedProcessorForSpleepOfEachExecution() {
        // Create a ScheduledExecutorService with a pool size of 4 (can be adjusted as needed)
        scheduler = Executors.newScheduledThreadPool(5);
        mThreadName = null; // Default value; can be set when the task is scheduled
        mTimedProcess = null; // Placeholder to be set per task
    }

    // Public method to get the Singleton instance
    public static synchronized ScheduledTimedProcessorForSpleepOfEachExecution getInstance() {
        if (instance == null) {
            instance = new ScheduledTimedProcessorForSpleepOfEachExecution();
        }
        return instance;
    }

    // Method to start scheduling a task
    public void start(String threadName, ITimedProcess timedProcess, TimerIntervalConstant timerIntervalConstant) {
        final long sleepTimeSecs = TimerProcesorIntervalProvider.getInstance().getTimerIntervalInSecs(timerIntervalConstant);
        mSleepTimeInMilliSeconds = sleepTimeSecs <= 0 ? DEFAULT_SLEEP_TIME_MILLIS : (sleepTimeSecs * 1000L);
        
        scheduleTask(threadName, timedProcess, mSleepTimeInMilliSeconds);
    }

    @Deprecated
    public void start(String threadName, ITimedProcess timedProcess, int timerIntervalInSecs) {
        final int sleepSecs = timerIntervalInSecs <= 0 ? 30 : timerIntervalInSecs;
        mSleepTimeInMilliSeconds = sleepSecs * 1000L;
        
        scheduleTask(threadName, timedProcess, mSleepTimeInMilliSeconds);
    }

    private void scheduleTask(String threadName, ITimedProcess timedProcess, long sleepTimeInMillis) {
        if (log.isInfoEnabled()) {
            log.info("TimedProcessor for " + threadName + " is started with the sleep time " + sleepTimeInMillis + " milliseconds.");
        }

        // Schedule the task at a fixed rate
        scheduler.scheduleAtFixedRate(() -> {
            if (mStoppedExternally) {
                return;
            }

            boolean canContinue = true;
            boolean continueNextExecutionWithoutSleep = false;

            synchronized (timedProcess) {
                continueNextExecutionWithoutSleep = timedProcess.processNow();
            }

            canContinue = timedProcess.canContinue() && !mStoppedExternally;

            if (!canContinue) {
                stopReaper();
                return;
            }

           
        }, 0, sleepTimeInMillis, TimeUnit.MILLISECONDS);
    }

    // Stop the scheduler
    public void stopReaper() {
    	
    	try {
        mStoppedExternally = true;
        scheduler.shutdown();
        if (log.isInfoEnabled()) {
            log.info("TimedProcessor has been stopped.");
        }
    	}catch(Exception e) {
    		
    	}
    }
}
