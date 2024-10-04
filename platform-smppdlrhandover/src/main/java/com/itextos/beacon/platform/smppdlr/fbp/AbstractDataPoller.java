package com.itextos.beacon.platform.smppdlr.fbp;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.itextos.beacon.commonlib.constants.TimerIntervalConstant;
import com.itextos.beacon.commonlib.message.DeliveryObject;
import com.itextos.beacon.commonlib.utility.timer.ITimedProcess;
import com.itextos.beacon.commonlib.utility.timer.SMPPDLRScheduledTimedProcessor;
import com.itextos.beacon.platform.smppdlr.dao.SmppDlrFallBackDao;
import com.itextos.beacon.platform.smppdlr.util.SmppDlrUtil;

public abstract class AbstractDataPoller
        implements
        ITimedProcess
{

    private static final Log     log         = LogFactory.getLog(AbstractDataPoller.class);

 //   private final ScheduledTimedProcessorForSpleepOfEachExecution mTimedProcessor;
    private boolean              canContinue = true;

    protected AbstractDataPoller()
    {
        super();
/*
        mTimedProcessor = new ScheduledTimedProcessorForSpleepOfEachExecution("SmppDlrFallbackTableReader", this, TimerIntervalConstant.SMPP_DLR_FALLBACK_TABLE_READER);
    //    mTimedProcessor.start();
        Thread virtualThreadInstance = Thread.ofVirtual().start(mTimedProcessor);
*/
        SMPPDLRScheduledTimedProcessor.getInstance().start("SmppDlrFallbackTableReader", this, TimerIntervalConstant.SMPP_DLR_FALLBACK_TABLE_READER);
    }

    @Override
    public boolean canContinue()
    {
        return canContinue;
    }

    @Override
    public boolean processNow()
    {
        doProcess();
        return false;
    }

    private static void doProcess()
    {

        try
        {
            final Map<Long, DeliveryObject> lRecords  = SmppDlrFallBackDao.getRecordsFromTable();
            final List<Long>                toDelete  = new ArrayList<>(lRecords.keySet());
            final List<DeliveryObject>      toProcess = new ArrayList<>(lRecords.values());

            if(log.isDebugEnabled()) {
            log.debug("SmppDlrFallBackDao.getRecordsFromTable() : data size : "+ lRecords.size());
            }
            SmppDlrUtil.smppDeliveryProcess(toProcess, null);

            SmppDlrFallBackDao.deleteRecordsFromTable(toDelete);
        }
        catch (final Exception e)
        {
            log.error("Exception while sending the message to Client wise DN Redis..", e);
        }
    }

    @Override
    public void stopMe()
    {
        canContinue = false;
    }

}