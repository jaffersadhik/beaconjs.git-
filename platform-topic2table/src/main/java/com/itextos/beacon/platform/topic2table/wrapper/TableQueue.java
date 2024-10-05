package com.itextos.beacon.platform.topic2table.wrapper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.itextos.beacon.commonlib.constants.Component;
import com.itextos.beacon.commonlib.constants.Table2DBInserterId;
import com.itextos.beacon.commonlib.constants.exception.ItextosRuntimeException;
import com.itextos.beacon.commonlib.message.BaseMessage;
import com.itextos.beacon.inmemory.loader.InmemoryLoaderCollection;
import com.itextos.beacon.inmemory.loader.process.InmemoryId;
import com.itextos.beacon.platform.topic2table.dbinfo.TableInserterInfo;
import com.itextos.beacon.platform.topic2table.dbinfo.TableInserterInfoCollection;
import com.itextos.beacon.platform.topic2table.inserter.DynamicTableInserter;
import com.itextos.beacon.platform.topic2table.inserter.ITableInserter;
import com.itextos.beacon.platform.topic2table.inserter.StaticTableInserter;

public class TableQueue {

    private static final Log                 log                    = LogFactory.getLog(TableQueue.class);

    private static final int                 MAX_QUEUE_SIZE         = 10000;
    private static final int                 DEFAULT_SLEEP_TIME_SEC = 1;
    private static final int                 DEFAULT_BATCH_SIZE     = 1000;
    
    private static Map<String,TableQueue> tablequeuemap=new HashMap<>();

    private final Component                  mComponent;
    private final Table2DBInserterId         mTableInsertId;
  //  private final ScheduledTimedProcessorForSpleepOfEachExecution             timedProcessor;

    private boolean                          canContinue            = true;
    private int                              mSleepTimeSecs         = DEFAULT_SLEEP_TIME_SEC;
    private int                              mBatchSize             = DEFAULT_BATCH_SIZE;
    private boolean                          isStaticTableInserter  = true;
    private final BlockingQueue<BaseMessage> messagesInmemQueue     = new LinkedBlockingQueue<>(MAX_QUEUE_SIZE);

	 private TableQueue(Component mComponent, Table2DBInserterId mTableInsertId) {
		
		 this.mComponent=mComponent;
		 this.mTableInsertId=mTableInsertId;
		 loadBasicInfo();
	}

	 private TableQueue() {
		 
		 this.mComponent=null;
		 this.mTableInsertId=null;
	 }
	public void addMessage(
	            BaseMessage aBaseMessage)
	            throws InterruptedException
	    {
	        messagesInmemQueue.put(aBaseMessage);

	        if (mBatchSize < messagesInmemQueue.size())
	            process(true);
	    }
	 
	 
	  public void process(
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


	public static TableQueue getInstance(Component mComponent2, Table2DBInserterId mTableInsertId2) {

		if(tablequeuemap.get(mComponent2.toString()+"~"+mTableInsertId2.toString())==null) {
			
			tablequeuemap.put(mComponent2.toString()+"~"+mTableInsertId2.toString(), new TableQueue(mComponent2,mTableInsertId2));
		}
		return tablequeuemap.get(mComponent2.toString()+"~"+mTableInsertId2.toString());
	}

	public boolean isQueue() {

		return messagesInmemQueue.isEmpty();
	
	}
}



