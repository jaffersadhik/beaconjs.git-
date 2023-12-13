package com.itextos.beacon.platform.vcprocess;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.itextos.beacon.commonlib.constants.BillType;
import com.itextos.beacon.commonlib.constants.Component;
import com.itextos.beacon.commonlib.constants.MiddlewareConstant;
import com.itextos.beacon.commonlib.message.MessageRequest;
import com.itextos.beacon.commonlib.utility.CommonUtility;
import com.itextos.beacon.platform.vcprocess.util.VCProducer;

public class MsgVerifyProcessor
        extends
        CommonProcess
{

    private static final Log log = LogFactory.getLog(MsgVerifyProcessor.class);

    public MsgVerifyProcessor(
            Component aSourceComponent,
            MessageRequest aMessageRequest)
    {
        super(aSourceComponent, aMessageRequest);
    }

    public void process()
    {

        try
        {
            if (log.isDebugEnabled())
                log.debug("Message received : " + mMessageRequest);

            if (!doDuplicateCheck())
                return;

            if (!doTimeBoundCheck())
                return;

            if (mMessageRequest.isIsIntl())
            {
                final IntlMsgVerifyProcessor intlMsgProcess = new IntlMsgVerifyProcessor(mSourceComponent, mMessageRequest);
                intlMsgProcess.messageProcess();
            }
            else
                sendToNextComponent(mSourceComponent, mMessageRequest);
        }
        catch (final Exception e)
        {
            log.error("Exception occer while processing the message in Verify Consumer ....", e);

            VCProducer.sendToErrorLog(mSourceComponent, mMessageRequest, e);
        }
    }

    private static void sendToNextComponent(
            Component aSourceComponent,
            MessageRequest aMessageRequest)
    {
        final boolean isCreditCheckEnabled = CommonUtility.isEnabled(aMessageRequest.getValue(MiddlewareConstant.MW_CREDIT_CHECK));

        if (log.isDebugEnabled())
            log.debug("Credit Check Enabled : " + isCreditCheckEnabled);

        final BillType lBillType = BillType.getBillType(Integer.toString(aMessageRequest.getBillType()));

        if ((BillType.PRE_PAID == lBillType) || isCreditCheckEnabled)
        {
            VCProducer.sendToNextComponent(aSourceComponent, Component.WC, aMessageRequest);

            if (log.isDebugEnabled())
                log.debug("Message sendToPrepaidComponent: Successfully");
        }
        else
        {
            VCProducer.sendToNextComponent(aSourceComponent, Component.RC, aMessageRequest);

            if (log.isDebugEnabled())
                log.debug("Message  sendToRouterComponent: Successfully");
        }
    }

}
