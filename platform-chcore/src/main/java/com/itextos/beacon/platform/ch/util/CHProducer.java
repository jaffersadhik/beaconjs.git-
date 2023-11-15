package com.itextos.beacon.platform.ch.util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.itextos.beacon.commonlib.constants.ClusterType;
import com.itextos.beacon.commonlib.constants.Component;
import com.itextos.beacon.commonlib.constants.ConfigParamConstants;
import com.itextos.beacon.commonlib.constants.Constants;
import com.itextos.beacon.commonlib.constants.CustomFeatures;
import com.itextos.beacon.commonlib.exception.ItextosException;
import com.itextos.beacon.commonlib.kafka.process.MessageProcessor;
import com.itextos.beacon.commonlib.message.BaseMessage;
import com.itextos.beacon.commonlib.message.MessageRequest;
import com.itextos.beacon.commonlib.message.SubmissionObject;
import com.itextos.beacon.commonlib.utility.CommonUtility;
import com.itextos.beacon.platform.dnprocess.PayloadProcessor;
import com.itextos.beacon.platform.msgflowutil.billing.BillingDatabaseTableIndentifier;
import com.itextos.beacon.platform.msgflowutil.util.PlatformUtil;

public class CHProducer
{

    private static final Log log = LogFactory.getLog(CHProducer.class);

    private CHProducer()
    {}

    public static void sendToBlockout(
            MessageRequest aMessageRequest)
    {

        try
        {
            MessageProcessor.writeMessage(Component.CH, Component.SBC, aMessageRequest);
        }
        catch (final ItextosException e)
        {
            log.error("Exception occer while sending to Schedule/Blockout topic ..", e);
            sendToErrorLog(aMessageRequest, e);
        }
    }

    public static void sendToRetryRoute(
            MessageRequest aMessageRequest)
    {

        try
        {
            MessageProcessor.writeMessage(Component.CH, Component.RCH, aMessageRequest);
        }
        catch (final ItextosException e)
        {
            log.error("Exception occer while sending to Retry Carrier Handover topic ..", e);
            sendToErrorLog(aMessageRequest, e);
        }
    }

    public static void sendToPlatfromRejection(
            SubmissionObject aSubmissionObject)
    {

        try
        {
            aSubmissionObject.setPlatfromRejected(true);
            MessageProcessor.writeMessage(Component.CH, Component.PRC, aSubmissionObject);
        }
        catch (final ItextosException e)
        {
            log.error("Exception occer while sending to Platform Rejection topic ..", e);
            sendToErrorLog(aSubmissionObject, e);
        }
    }

    public static void sendToPlatfromRejection(
            MessageRequest aMessageRequest)
    {

        try
        {
            aMessageRequest.setPlatfromRejected(true);
            MessageProcessor.writeMessage(Component.CH, Component.PRC, aMessageRequest);
        }
        catch (final ItextosException e)
        {
            log.error("Exception occer while sending to Platform Rejection topic ..", e);
            sendToErrorLog(aMessageRequest, e);
        }
    }

    public static void sendToDummyRoute(
            SubmissionObject aSubmissionObject)
    {

        try
        {
            MessageProcessor.writeMessage(Component.CH, Component.DCH, aSubmissionObject);
        }
        catch (final ItextosException e)
        {
            log.error("Exception occer while sending to Dummy Carrier Handover topic ..", e);
            sendToErrorLog(aSubmissionObject, e);
        }
    }

    public static void sendToAgingInsert(
            SubmissionObject aSubmissionObject)
    {

        try
        {
            MessageProcessor.writeMessage(Component.CH, Component.AGIN, aSubmissionObject);
        }
        catch (final ItextosException e)
        {
            log.error("Exception occer while sending to Aging Dlr Insert topic ..", e);
            sendToErrorLog(aSubmissionObject, e);
        }
    }

    public static void sendToAgingProcess(
            SubmissionObject aSubmissionObject)
    {

        try
        {
            MessageProcessor.writeMessage(Component.CH, Component.ADNP, aSubmissionObject);
        }
        catch (final ItextosException e)
        {
            log.error("Exception occer while sending to Aging Dlr Process topic ..", e);
            sendToErrorLog(aSubmissionObject, e);
        }
    }

    public static void sendToInterim(
            SubmissionObject aSubmissionObject)
    {

        try
        {
            identifySuffix(aSubmissionObject);

            MessageProcessor.writeMessage(Component.CH, Component.T2DB_INTERIM_FAILUERS, aSubmissionObject);
        }
        catch (final ItextosException e)
        {
            log.error("Exception occer while sending to Intrim Failure topic ..", e);
            sendToErrorLog(aSubmissionObject, e);
        }
    }

    public static void sendToSubBilling(
            BaseMessage aBaseMessage)
    {

        try
        {
            MessageProcessor.writeMessage(Component.CH, Component.SUBBC, aBaseMessage);
        }
        catch (final ItextosException e)
        {
            log.error("Exception occer while sending to Final Process for MT topic ..", e);
            sendToErrorLog(aBaseMessage, e);
        }
    }

    public static void sendToNextLevel(
            MessageRequest aMessageRequest)
    {
        sendToPlatfromRejection(aMessageRequest);
    }

    public static void sendToNextLevel(
            SubmissionObject aSubmissionObject,
            MessageRequest aMessageRequest,
            boolean isPartial)
    {

        try
        {

            try
            {
                aSubmissionObject.setMtMessageRetryIdentifier(Constants.ENABLED);
                PayloadProcessor.removePayload(aSubmissionObject);
            }
            catch (final Exception exp)
            {}

            final int lRetryAttempt = aSubmissionObject.getRetryAttempt();

            if (lRetryAttempt == 0)
            {
                if (isPartial)
                    sendToPlatfromRejection(aSubmissionObject);
                else
                    sendToPlatfromRejection(aMessageRequest);
            }
            else
            {
                final String  lClientId = aSubmissionObject.getClientId();
                final boolean isAging   = CommonUtility.isEnabled(CHProcessUtil.getCutomFeatureValue(lClientId, CustomFeatures.IS_AGING_ENABLE));
                final boolean isFastDn  = CommonUtility.isEnabled(CHProcessUtil.getCutomFeatureValue(lClientId, CustomFeatures.IS_FASTDN_ENABLE));

                if (isAging || isFastDn)
                {
                    // Final dn consumer will take care of handover to Client & Billing
                    aSubmissionObject.setIndicateFinalDn(Constants.ENABLED);

                    final ClusterType lCluster        = aSubmissionObject.getClusterType();

                    final boolean     isAgingDNReject = CommonUtility.isEnabled(CHProcessUtil.getAppConfigValueAsString(lCluster.getKey() + ConfigParamConstants.PLATFORM_REJ_DLR_HANDOVER));

                    if (isAgingDNReject)
                    {
                        if (log.isDebugEnabled())
                            log.debug("Message sending to Platform Reject  ...");
                        if (isPartial)
                            sendToPlatfromRejection(aSubmissionObject);
                        else
                            sendToPlatfromRejection(aMessageRequest);
                    }
                    else
                        sendToAgingProcess(aSubmissionObject);
                }
                else
                {
                    if (log.isDebugEnabled())
                        log.debug("Message sending to Platform Reject  ...");

                    if (isPartial)
                        sendToPlatfromRejection(aSubmissionObject);
                    else
                        sendToPlatfromRejection(aMessageRequest);
                }
            }
        }
        catch (final Exception e)
        {
            sendToErrorLog(aSubmissionObject, e);
        }
    }

    public static void sendToErrorLog(
            BaseMessage aBaseMessage,
            Exception aErrorMsg)
    {

        try
        {
            PlatformUtil.sendToErrorLog(Component.CH, aBaseMessage, aErrorMsg);
        }
        catch (final Exception e)
        {
            log.error("Exception while sending request to error log. " + aBaseMessage, e);
        }
    }

    private static void identifySuffix(
            BaseMessage aBaseMessage)
    {

        try
        {
            final BillingDatabaseTableIndentifier lBillingDatabaseTableIndentifier = new BillingDatabaseTableIndentifier(aBaseMessage);
            lBillingDatabaseTableIndentifier.identifySuffix();
        }
        catch (final Exception e)
        {
            log.error("Exception occer while identifying table suffix...", e);
        }
    }

}