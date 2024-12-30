package com.itextos.beacon.platform.k2dbconcatenatesmpp.db;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.itextos.beacon.commonlib.constants.ClusterType;
import com.itextos.beacon.commonlib.constants.TimerIntervalConstant;
import com.itextos.beacon.commonlib.utility.timer.ITimedProcess;
import com.itextos.beacon.commonlib.utility.timer.TimedProcessor;
import com.itextos.beacon.commonlib.utility.tp.ExecutorSheduler;
import com.itextos.beacon.smpp.dto.SmppMessageRequest;

class DbOperationInMemory
        implements
        ITimedProcess
{

    private static final Log                        log                      = LogFactory.getLog(DbOperationInMemory.class);

    private final ClusterType                       mClusterType;
    private final BlockingQueue<SmppMessageRequest> mSmppMessageRequestQueue = new LinkedBlockingQueue<>(5000);
    private final TimedProcessor                   mTimedProcessor;
    private boolean                                 mCanContinue             = true;

    DbOperationInMemory(
            ClusterType aClusterType)
    {
        mClusterType    = aClusterType;
        
        mTimedProcessor = new TimedProcessor("SmppConcateDbInserter-" + aClusterType, this, TimerIntervalConstant.SMPP_DLR_FALLBACK_TABLE_READER);
 
        ExecutorSheduler.getInstance().addTask(mTimedProcessor, "SmppConcateDbInserter-" + aClusterType);
    }

    void addMessage(
            SmppMessageRequest aSmppMessageRequest)
            throws InterruptedException
    {
        mSmppMessageRequestQueue.put(aSmppMessageRequest);
    }

  
    @Override
    public boolean canContinue()
    {
        return mCanContinue;
    }

    private void doDbInsert()
    {
        final int                      readSize = mSmppMessageRequestQueue.size() > 1000 ? 1000 : mSmppMessageRequestQueue.size();
        final List<SmppMessageRequest> list     = new ArrayList<>(readSize);
        mSmppMessageRequestQueue.drainTo(list, readSize);

        try
        {
            DbOperation.dbInsert(list);
        }
        catch (final Exception e)
        {
            returnToInMem(list);
        }
    }

    @Override
    public boolean processNow()
    {
        doDbInsert();
        return false;
    }

   

    private void returnToInMem(
            List<SmppMessageRequest> aList)
    {
        mSmppMessageRequestQueue.addAll(aList);
    }

    @Override
    public void stopMe()
    {
        mCanContinue = false;
    }

}