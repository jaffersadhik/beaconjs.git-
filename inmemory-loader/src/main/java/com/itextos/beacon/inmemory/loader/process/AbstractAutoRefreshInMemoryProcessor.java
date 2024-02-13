package com.itextos.beacon.inmemory.loader.process;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.itextos.beacon.commonlib.utility.timer.ITimedProcess;
import com.itextos.beacon.commonlib.utility.timer.TimedProcessor;

public abstract class AbstractAutoRefreshInMemoryProcessor
        extends
        InmemoryProcessor
        implements
        ITimedProcess
{

    private static final Log     log          = LogFactory.getLog(AbstractAutoRefreshInMemoryProcessor.class);

    private final TimedProcessor mTimedProcessor;
    private boolean              mCanContinue = true;

    protected AbstractAutoRefreshInMemoryProcessor(
            InmemoryInput aInmemoryInputDetail)
    {
        super(aInmemoryInputDetail);
        mTimedProcessor = new TimedProcessor("TimerThread-" + mInmemoryInput.getInmemoryId(), this, mInmemoryInput.getSleepSec());
        mTimedProcessor.start();
        mCanContinue = mInmemoryInput.isAutoRefreshRequired();
    }

    @Override
    public boolean canContinue()
    {
        return mCanContinue;
    }

    @Override
    public boolean processNow()
    {
    	String module=System.getenv("module");
    	
    	if(module.equals("dbgwejb")) {
    		
    		 getDataFromDB();
    		 
    	}else {
    		
    		getDataFromEJBServer();
    	}
       
        return false;
    }

    @Override
    public void stopMe()
    {
        if (log.isDebugEnabled())
            log.debug("Inmemory process " + mInmemoryInput.getInmemoryId() + " stopped externaly.");
        mCanContinue = false;

        if (mTimedProcessor != null)
            mTimedProcessor.stopReaper();
    }

}
