package com.itextos.beacon.http.interfacefallback.inmem;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.itextos.beacon.commonlib.constants.TimerIntervalConstant;
import com.itextos.beacon.commonlib.message.IMessage;
import com.itextos.beacon.commonlib.utility.CommonUtility;
import com.itextos.beacon.commonlib.utility.timer.ITimedProcess;
import com.itextos.beacon.commonlib.utility.timer.ScheduledTimedProcessorForSpleepOfEachExecution;
import com.itextos.beacon.http.interfacefallback.dao.FallBackDao;

public class FallbackQReaper
        implements
        ITimedProcess,Runnable
{

    private static final Log log = LogFactory.getLog(FallbackQReaper.class);

    private static class SingletonHolder
    {

        @SuppressWarnings("synthetic-access")
        static final FallbackQReaper INSTANCE = new FallbackQReaper();

    }

    public static FallbackQReaper getInstance()
    {
        return SingletonHolder.INSTANCE;
    }

    private boolean              canContinue = true;
  //  private final ScheduledTimedProcessorForSpleepOfEachExecution mTimedProcessor;

    private FallbackQReaper()
    {
    	/*
        mTimedProcessor = new ScheduledTimedProcessorForSpleepOfEachExecution("FallbackTableInserter", this, TimerIntervalConstant.INTERFACE_FALLBACK_TABLE_INSERTER);
      //  mTimedProcessor.start();
        Thread virtualThreadInstance = Thread.ofVirtual().start(mTimedProcessor);
		*/
    
    	ScheduledTimedProcessorForSpleepOfEachExecution.getInstance().start("FallbackTableInserter", this, TimerIntervalConstant.INTERFACE_FALLBACK_TABLE_INSERTER);
    }

    private static boolean process()
    {
        boolean hasRecord = false;

        try
        {
            final List<IMessage> lRecords      = FallbackQ.getInstance().getMessage();
            final int            lTotalDrained = lRecords.size();
            hasRecord = lTotalDrained > 0;

            if (log.isInfoEnabled())
                log.info("Total Messages Drained - " + lTotalDrained);

            if (hasRecord)
            {
                boolean done = false;

                while (!done)
                {
                    if (log.isDebugEnabled())
                        log.debug("Inserting into Database.");

                    done = insertData(lRecords);
                }
            }
        }
        catch (final Exception e)
        {
            log.error("ERROR >>>> Exception in FallbackQReaper Reaper thread \n", e);
        }
        return hasRecord;
    }

  public void run() {
    	
    	long startTime=System.currentTimeMillis();
    	int loopcount=0;
    	while(true) {
    		loopcount++;
    
    		boolean status=processNow();
    		
    		if(status) {
    			
    			if((System.currentTimeMillis()-startTime)>500||loopcount>10) {
    				
    				break;
    			}
    			
    		}else {
    			
    			break;
    			
    		}
    	}
    }
  
    private static boolean insertData(
            List<IMessage> aRecords)
    {

        try
        {
            FallBackDao.storeFalbackData(aRecords);

            if (log.isDebugEnabled())
                log.debug("DB Inserted succesfully...." + aRecords.size());

            return true;
        }
        catch (final Exception exp)
        {
            log.error("problem inserting interface input data retrying after 10 sec.....", exp);
            CommonUtility.sleepForAWhile(10 * 1000L);
        }
        return false;
    }

    @Override
    public boolean canContinue()
    {
        return canContinue;
    }

    @Override
    public boolean processNow()
    {
        return process();
    }

    @Override
    public void stopMe()
    {
        canContinue = false;
    }

}