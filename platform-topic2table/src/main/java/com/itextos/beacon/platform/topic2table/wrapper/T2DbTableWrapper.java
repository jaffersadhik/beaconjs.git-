package com.itextos.beacon.platform.topic2table.wrapper;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.itextos.beacon.commonlib.constants.Component;
import com.itextos.beacon.commonlib.constants.Table2DBInserterId;
import com.itextos.beacon.commonlib.constants.exception.ItextosRuntimeException;
import com.itextos.beacon.commonlib.message.BaseMessage;
import com.itextos.beacon.commonlib.utility.Table2DBExecutorPoolSingleton;
import com.itextos.beacon.commonlib.utility.timer.ITimedProcess;
import com.itextos.beacon.inmemory.loader.InmemoryLoaderCollection;
import com.itextos.beacon.inmemory.loader.process.InmemoryId;
import com.itextos.beacon.platform.topic2table.dbinfo.TableInserterInfo;
import com.itextos.beacon.platform.topic2table.dbinfo.TableInserterInfoCollection;
import com.itextos.beacon.platform.topic2table.inserter.DynamicTableInserter;
import com.itextos.beacon.platform.topic2table.inserter.ITableInserter;
import com.itextos.beacon.platform.topic2table.inserter.StaticTableInserter;
import com.itextos.beacon.smslog.Table2DBLog;

public class T2DbTableWrapper
        implements
        ITimedProcess,Runnable
{

    private static final Log                 log                    = LogFactory.getLog(T2DbTableWrapper.class);

    private static final int                 MAX_QUEUE_SIZE         = 10000;
    private static final int                 DEFAULT_SLEEP_TIME_SEC = 1;
    private static final int                 DEFAULT_BATCH_SIZE     = 1000;

    private final Component                  mComponent;
    private final Table2DBInserterId         mTableInsertId;
    private final BlockingQueue<BaseMessage> messagesInmemQueue     = new LinkedBlockingQueue<>(MAX_QUEUE_SIZE);
  //  private final ScheduledTimedProcessorForSpleepOfEachExecution             timedProcessor;

    private boolean                          canContinue            = true;
    private int                              mSleepTimeSecs         = DEFAULT_SLEEP_TIME_SEC;
    private int                              mBatchSize             = DEFAULT_BATCH_SIZE;
    private boolean                          isStaticTableInserter  = true;

    public T2DbTableWrapper(
            Component aComponent,
            Table2DBInserterId aTableInsertId)
    {
        super();
        mComponent     = aComponent;
        mTableInsertId = aTableInsertId;

        loadBasicInfo();
/*
        timedProcessor = new ScheduledTimedProcessorForSpleepOfEachExecution(mComponent.getKey(), this, mSleepTimeSecs);
        timedProcessor.start();
  */
    
    //    ScheduledTimedProcessorForSpleepOfEachExecution.getInstance().start(mComponent.getKey(), this, mSleepTimeSecs);
    
        Table2DBExecutorPoolSingleton.getInstance().addTask(this, mComponent.getKey());
        
    }

    private void loadBasicInfo()
    {
        final TableInserterInfoCollection lTableInserterInfoCollection = (TableInserterInfoCollection) InmemoryLoaderCollection.getInstance().getInmemoryCollection(InmemoryId.TABLE_INSERTER_INFO);
        final TableInserterInfo           lTableInserterInfo           = lTableInserterInfoCollection.getTableInserterInfo(mTableInsertId);

        if (lTableInserterInfo == null)
            throw new ItextosRuntimeException("Unable to find the table insert information for component '" + mComponent + "' and table inserter id '" + mTableInsertId + "'");

        mSleepTimeSecs        = lTableInserterInfo.getSleepSecs();
        mBatchSize            = lTableInserterInfo.getBatchSize();
        isStaticTableInserter = lTableInserterInfo.isStaticTableInserter();

        if (log.isDebugEnabled())
        {
            log.debug("Table Inserter component  " + mComponent);
            log.debug("Table Inserter Id         " + mTableInsertId);
            log.debug("Table Inserter Sleep Sec  " + mSleepTimeSecs);
            log.debug("Table Inserter Batch Size " + mBatchSize);
            log.debug("Table Inserter is Static  " + isStaticTableInserter);
        }
    }

    public void addMessage(
            BaseMessage aBaseMessage)
            throws InterruptedException
    {
        messagesInmemQueue.put(aBaseMessage);

        if (mBatchSize < messagesInmemQueue.size())
            process(true);
    }

    public void run() {
    	
    	long startTime=System.currentTimeMillis();
    	int loopcount=0;
    	while(true) {
    		loopcount++;
    		Table2DBLog.getInstance(mTableInsertId.toString()).log(mTableInsertId.toString() + " : Date : "+new Date());
    
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
    private void process(
            boolean aOnSize)
    {
        List<BaseMessage> toProcess = null;

        int               size      = 1;

        if (aOnSize)
        {
            toProcess = new ArrayList<>(mBatchSize);
            size      = mBatchSize;
        }
        else
        {
            final int qsize = messagesInmemQueue.size();
            size      = qsize;
            toProcess = new ArrayList<>(qsize);
        }

        messagesInmemQueue.drainTo(toProcess, size);

        if (!toProcess.isEmpty())
        {
            if (log.isDebugEnabled())
                log.debug("Messages to process " + toProcess.size());

            ITableInserter inserter = null;
            if (isStaticTableInserter)
                inserter = new StaticTableInserter(mComponent, mTableInsertId, toProcess);
            else
                inserter = new DynamicTableInserter(mComponent, mTableInsertId, toProcess);

            if (log.isDebugEnabled())
                log.debug("Calling process method in Processor '" + inserter.getClass().getName() + "'");

            inserter.process();

            if (log.isDebugEnabled())
                log.debug("Completed processing the records");
        }
        /*
         * else
         * if (log.isDebugEnabled())
         * log.debug("No messages to process");
         */
    }

    @Override
    public boolean canContinue()
    {
        return canContinue;
    }

    @Override
    public boolean processNow()
    {
        process(false);
        return !messagesInmemQueue.isEmpty();
    }

    @Override
    public void stopMe()
    {
        canContinue = false;
    }

}