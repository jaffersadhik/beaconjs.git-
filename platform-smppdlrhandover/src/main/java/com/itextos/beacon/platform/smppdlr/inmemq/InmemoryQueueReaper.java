package com.itextos.beacon.platform.smppdlr.inmemq;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.itextos.beacon.commonlib.constants.ClusterType;
import com.itextos.beacon.commonlib.constants.TimerIntervalConstant;
import com.itextos.beacon.commonlib.message.DeliveryObject;
import com.itextos.beacon.commonlib.utility.timer.ITimedProcess;
import com.itextos.beacon.commonlib.utility.timer.TimedProcessor;
import com.itextos.beacon.platform.smppdlr.util.SmppDlrUtil;

public class InmemoryQueueReaper
        implements
        ITimedProcess
{

    private static final Log     log         = LogFactory.getLog(InmemoryQueueReaper.class);

    private final TimedProcessor mTimedProcessor;
    private final ClusterType    mCluster;
    private boolean              canContinue = true;

    public InmemoryQueueReaper(
            ClusterType aCluster)
    {
        mCluster        = aCluster;
        mTimedProcessor = new TimedProcessor("TimerThread-InmemoryReaper", this, TimerIntervalConstant.SMPP_DLR_INMEM_PROCESS_INTERVAL);
        mTimedProcessor.start();
    }

    @Override
    public boolean processNow()
    {
        final List<DeliveryObject> lDeliveryObjectList = InmemoryQueue.getInstance().getRecords();

        if ((lDeliveryObjectList != null) && (lDeliveryObjectList.size() > 0))
        {
            if (log.isDebugEnabled())
                log.debug("Drained a list>>>" + lDeliveryObjectList.size());

            SmppDlrUtil.smppDeliveryProcess(lDeliveryObjectList, mCluster);
        }

        return false;
    }

    @Override
    public boolean canContinue()
    {
        return canContinue;
    }

    @Override
    public void stopMe()
    {
        canContinue = false;
    }

}
