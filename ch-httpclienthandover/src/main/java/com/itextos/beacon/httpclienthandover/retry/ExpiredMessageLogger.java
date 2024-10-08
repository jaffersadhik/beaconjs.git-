package com.itextos.beacon.httpclienthandover.retry;

import java.util.Date;
import java.util.List;
import java.util.UUID;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.itextos.beacon.commonlib.constants.MiddlewareConstant;
import com.itextos.beacon.commonlib.constants.TimerIntervalConstant;
import com.itextos.beacon.commonlib.message.BaseMessage;
import com.itextos.beacon.commonlib.utility.CommonUtility;
import com.itextos.beacon.commonlib.utility.timer.ITimedProcess;
import com.itextos.beacon.commonlib.utility.tp.ExecutorCore;
import com.itextos.beacon.commonlib.utility.tp.ScheduledTimedProcessor;
import com.itextos.beacon.httpclienthandover.utils.LogStatusEnum;
import com.itextos.beacon.httpclienthandover.utils.TopicSenderUtility;
import com.itextos.beacon.smslog.Table2DBLog;

public class ExpiredMessageLogger
        implements
        ITimedProcess,Runnable
{

    public static final Log      log         = LogFactory.getLog(ExpiredMessageLogger.class);

    //
    private final String         clientId;
    private final boolean        isCustomerSpecific;
//    private final ScheduledTimedProcessorForSpleepOfEachExecution timeProcessor;
    private boolean              canContinue = true;

    public ExpiredMessageLogger(
            boolean aIsCustSpecific,
            String aCustID)
    {
        isCustomerSpecific = aIsCustSpecific;
        clientId           = aCustID;
        
       ExecutorCore.getInstance().addTask(this, aCustID); 
    }

    @Override
    public boolean canContinue()
    {
        return canContinue;
    }

  public void run() {
    	
    	while(true) {
    	
    
    		boolean status=processNow();
    		
    		if(status) {
    			
    			continue;
    			
    		}else {
    			
                CommonUtility.sleepForAWhile( TimerIntervalConstant.DLR_HTTP_HANDOVER_EXPIRED_MESSAGE_LOG_INTERVAL.getDurationInSecs());

                continue;
    		}
    	}
    }
    
    @Override
    public boolean processNow()
    {
        final List<String> midList = RetryDataHolder.getInstance().getExpiredMessages(RetryUtils.DB_IN_QUERY_LIMIT, clientId);

        if (!midList.isEmpty())
        {
            final List<BaseMessage> baseMessages = RetryDBHelper.getBaseMessagesForMIDs(midList);

            if (!baseMessages.isEmpty())
            {
                logExpiredMessages(baseMessages);
                RetryDBHelper.deleteForMids(midList);
            }
            RedisHelper.deleteInProcessMessage(baseMessages, clientId);
        }

        return RetryDataHolder.getInstance().expiredMessagesCount() > RetryUtils.DB_IN_QUERY_LIMIT;
    }

    private void logExpiredMessages(
            List<BaseMessage> aBaseMessages)
    {
        final UUID        uniqueId   = UUID.randomUUID();
        final BaseMessage masterData = aBaseMessages.get(0).getClonedObject();
        masterData.putValue(MiddlewareConstant.MW_CLIENT_HANDOVER_UNIQUE_ID, "" + uniqueId);
        masterData.putValue(MiddlewareConstant.MW_CLIENT_HANDOVER_LOG_STATUS, LogStatusEnum.RETRY_EXPIRED.name());

        aBaseMessages.stream().forEach(baseMessage -> {

            if (isCustomerSpecific)
            {
                baseMessage.putValue(MiddlewareConstant.MW_CLIENT_HANDOVER_UNIQUE_ID, "" + uniqueId);

                TopicSenderUtility.sendToMasterLogQueue(masterData);
                TopicSenderUtility.sendToChildLogQueue(baseMessage);
            }
            else
            {
                final BaseMessage tempMasterData = baseMessage.getClonedObject();

                final UUID        tempUniqueId   = UUID.randomUUID();
                tempMasterData.putValue(MiddlewareConstant.MW_CLIENT_HANDOVER_UNIQUE_ID, "" + tempUniqueId);
                tempMasterData.putValue(MiddlewareConstant.MW_CLIENT_HANDOVER_LOG_STATUS, LogStatusEnum.RETRY_EXPIRED.name());

                baseMessage.putValue(MiddlewareConstant.MW_CLIENT_HANDOVER_UNIQUE_ID, "" + tempUniqueId);
                TopicSenderUtility.sendToMasterLogQueue(tempMasterData);
                TopicSenderUtility.sendToChildLogQueue(baseMessage);
            }
        });
    }

    @Override
    public void stopMe()
    {
        canContinue = false;
    }

}
