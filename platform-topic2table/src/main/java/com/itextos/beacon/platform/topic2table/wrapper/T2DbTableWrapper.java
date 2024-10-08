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
import com.itextos.beacon.commonlib.utility.CommonUtility;
import com.itextos.beacon.commonlib.utility.timer.ITimedProcess;
import com.itextos.beacon.commonlib.utility.tp.ExecutorTable2DB;
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

  

    private final Component                  mComponent;
    private final Table2DBInserterId         mTableInsertId;
  //  private final ScheduledTimedProcessorForSpleepOfEachExecution             timedProcessor;

    private boolean                          canContinue            = true;
 
    private TableQueue tableQueue;
    public T2DbTableWrapper(
            Component aComponent,
            Table2DBInserterId aTableInsertId)
    {
        super();
        mComponent     = aComponent;
        mTableInsertId = aTableInsertId;

        tableQueue=TableQueue.getInstance(mComponent,mTableInsertId);
        
        //loadBasicInfo();
/*
        timedProcessor = new ScheduledTimedProcessorForSpleepOfEachExecution(mComponent.getKey(), this, mSleepTimeSecs);
        timedProcessor.start();
  */
    
    //    ScheduledTimedProcessorForSpleepOfEachExecution.getInstance().start(mComponent.getKey(), this, mSleepTimeSecs);
    
        ExecutorTable2DB.getInstance().addTask(this, mComponent.getKey());
        
    }

  

   

   public void run() {
    	
     	while(true) {
   
     		Table2DBLog.getInstance(mTableInsertId.toString()).log(mTableInsertId.toString() + " : Date : "+new Date());
    
    		boolean status=processNow();
    		
    		if(status) {
    		
    			continue;
    			
    		}else {
    			
                CommonUtility.sleepForAWhile(100);

    			break;
    			
    		}
    	}
    }
  
    @Override
    public boolean canContinue()
    {
        return canContinue;
    }

    @Override
    public boolean processNow()
    {
    	
    	TableQueue.getInstance(mComponent, mTableInsertId).process(false);
        return !TableQueue.getInstance(mComponent, mTableInsertId).isQueue();
        
    }

    @Override
    public void stopMe()
    {
        canContinue = false;
    }

}