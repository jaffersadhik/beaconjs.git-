package com.itextos.beacon.platform.vcprocess;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.itextos.beacon.commonlib.constants.Component;
import com.itextos.beacon.commonlib.constants.PlatformStatusCode;
import com.itextos.beacon.commonlib.message.MessageRequest;
import com.itextos.beacon.platform.vcprocess.util.VCProducer;
import com.itextos.beacon.platform.vcprocess.util.VCUtil;

public abstract class CommonProcess
{

    private static final Log       log = LogFactory.getLog(CommonProcess.class);

    protected final MessageRequest mMessageRequest;
    protected final Component      mSourceComponent;

    protected CommonProcess(
            Component aSourceComponent,
            MessageRequest aMessageRequest)
    {
        this.mMessageRequest  = aMessageRequest;
        this.mSourceComponent = aSourceComponent;
    }

    protected boolean doDuplicateCheck()
    {

        if (((mMessageRequest.getVlShortner() == 0) && (mMessageRequest.getUrlSmartlinkEnable() == 0)) && VCUtil.doDuplicateChk(mMessageRequest))
        {
            if (log.isDebugEnabled())
                log.debug("Message Rejected Duplecate Check Failed .. Message Id : " + mMessageRequest.getBaseMessageId());
            mMessageRequest.setSubOriginalStatusCode(PlatformStatusCode.DUPLICATE_CHECK_FAILED.getStatusCode());
            VCProducer.sendToPlatformRejection(mSourceComponent, mMessageRequest);
            return false;
        }
        return true;
    }

    protected boolean doTimeBoundCheck()
    {

        if (!VCUtil.doTimeboundChk(mMessageRequest))
        {
            if (log.isDebugEnabled())
                log.debug("Message Rejected Time Bound Check Failed .. Message Id : " + mMessageRequest.getBaseMessageId());
            mMessageRequest.setSubOriginalStatusCode(PlatformStatusCode.REJECT_TIMEBOUND_CHECK.getStatusCode());
            VCProducer.sendToPlatformRejection(mSourceComponent, mMessageRequest);
            return false;
        }

        return true;
    }

}
