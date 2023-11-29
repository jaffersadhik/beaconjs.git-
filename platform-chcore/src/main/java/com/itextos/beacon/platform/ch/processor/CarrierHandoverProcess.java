package com.itextos.beacon.platform.ch.processor;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.MessageFormat;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.itextos.beacon.commonlib.componentconsumer.processor.AbstractKafkaComponentProcessor;
import com.itextos.beacon.commonlib.constants.ClusterType;
import com.itextos.beacon.commonlib.constants.Component;
import com.itextos.beacon.commonlib.constants.ConfigParamConstants;
import com.itextos.beacon.commonlib.constants.Constants;
import com.itextos.beacon.commonlib.constants.DateTimeFormat;
import com.itextos.beacon.commonlib.constants.MessageType;
import com.itextos.beacon.commonlib.constants.PlatformStatusCode;
import com.itextos.beacon.commonlib.httpclient.BasicHttpConnector;
import com.itextos.beacon.commonlib.httpclient.HttpResult;
import com.itextos.beacon.commonlib.kafka.service.consumer.ConsumerInMemCollection;
import com.itextos.beacon.commonlib.messageobject.BaseMessage;
import com.itextos.beacon.commonlib.messageobject.IMessage;
import com.itextos.beacon.commonlib.messageobject.MessageRequest;
import com.itextos.beacon.commonlib.messageobject.SubmissionObject;
import com.itextos.beacon.commonlib.messageobject.utility.MessageUtil;
import com.itextos.beacon.commonlib.prometheusmetricsutil.PrometheusMetrics;
import com.itextos.beacon.commonlib.utility.CommonUtility;
import com.itextos.beacon.commonlib.utility.DateTimeUtility;
import com.itextos.beacon.inmemory.carrierhandover.RouteKannelInfo;
import com.itextos.beacon.inmemory.carrierhandover.util.ICHUtil;
import com.itextos.beacon.platform.ch.util.CHProcessUtil;
import com.itextos.beacon.platform.ch.util.CHProducer;
import com.itextos.beacon.platform.chutility.util.CHUtil;
import com.itextos.beacon.platform.chutility.util.GenerateDNUrl;
import com.itextos.beacon.platform.dnpayloadutil.PayloadProcessor;
import com.itextos.beacon.platform.kannelstatusupdater.process.response.KannelStatsCollector;

import io.prometheus.client.Histogram.Timer;

public class CarrierHandoverProcess
        extends
        AbstractKafkaComponentProcessor
{

    private static final Log log = LogFactory.getLog(CarrierHandoverProcess.class);

    public CarrierHandoverProcess(
            String aThreadName,
            Component aComponent,
            ClusterType aPlatformCluster,
            String aTopicName,
            ConsumerInMemCollection aConsumerInMemCollection,
            int aSleepInMillis)
    {
        super(aThreadName, aComponent, aPlatformCluster, aTopicName, aConsumerInMemCollection, aSleepInMillis);
    }

    @Override
    public void doProcess(
            BaseMessage aBaseMessage)
    {
        final MessageRequest lMessageRequest = (MessageRequest) aBaseMessage;

        if (log.isDebugEnabled())
            log.debug("HC Received Object .. " + lMessageRequest.getJsonString());

        try
        {
            final Timer   lPlatformRejection = PrometheusMetrics.componentMethodStartTimer(Component.CH, mPlatformCluster, "platformRejection");
            final boolean isHexMsg           = lMessageRequest.isHexMessage();

            if (isHexMsg)
            {
                final boolean isValidHexMessage = CHUtil.isValidHexMessage(lMessageRequest.getLongMessage());

                if (!isValidHexMessage)
                {
                    log.error("Invalid HEX Message : " + lMessageRequest);
                    sendToPlatfromRejection(lMessageRequest, PlatformStatusCode.INVALID_HEX_MESSAGE);
                    PrometheusMetrics.componentMethodEndTimer(Component.CH, lPlatformRejection);
                    return;
                }
            }

            final String lFeatureCode = CommonUtility.nullCheck(lMessageRequest.getFeatureCode(), true);
            final String lRouteId     = CommonUtility.nullCheck(lMessageRequest.getRouteId(), true);

            if (lRouteId.isEmpty())
            {
                log.error("Unable to find out the Route Id : " + lMessageRequest);
                sendToPlatfromRejection(lMessageRequest, PlatformStatusCode.EMPTY_ROUTE_ID);
                PrometheusMetrics.componentMethodEndTimer(Component.CH, lPlatformRejection);
                return;
            }

            if (lFeatureCode.isBlank())
            {
                log.error("Unable to find out the Feature Code : " + lMessageRequest);
                sendToPlatfromRejection(lMessageRequest, PlatformStatusCode.EMPTY_FEATURE_CODE);
                PrometheusMetrics.componentMethodEndTimer(Component.CH, lPlatformRejection);
                return;
            }
            PrometheusMetrics.componentMethodEndTimer(Component.CH, lPlatformRejection);

            final Timer   lBlockoutTimer = PrometheusMetrics.componentMethodStartTimer(Component.CH, mPlatformCluster, "isMessageBlockout");
            final boolean isBlockout     = CHProcessUtil.isMessageBlockout(lMessageRequest);
            PrometheusMetrics.componentMethodEndTimer(Component.CH, lBlockoutTimer);

            if (isBlockout)
                return;

            final Timer   lExpiredtimer = PrometheusMetrics.componentMethodStartTimer(Component.CH, mPlatformCluster, "isExpired");
            final boolean isExpired     = CHUtil.isExpired(lMessageRequest);
            PrometheusMetrics.componentMethodEndTimer(Component.CH, lExpiredtimer);

            if (isExpired)
            {
                log.error("Message Expired :" + lMessageRequest);
                sendToPlatfromRejection(lMessageRequest, PlatformStatusCode.EXPIRED_MESSAGE);
                return;
            }

            final Timer           lDeliveryRouteInfo = PrometheusMetrics.componentMethodStartTimer(Component.CH, mPlatformCluster, "getDeliveryRouteInfo");
            final RouteKannelInfo lKannelRouteInfo   = ICHUtil.getDeliveryRouteInfo(lRouteId, lFeatureCode);
            PrometheusMetrics.componentMethodEndTimer(Component.CH, lDeliveryRouteInfo);

            if (lKannelRouteInfo == null)
            {
                log.error("Unable to find  Route Kannel Template for  route : " + lRouteId + " feature cd : " + lFeatureCode + " BaseMessage = : \t" + lMessageRequest);
                sendToPlatfromRejection(lMessageRequest, PlatformStatusCode.KANNEL_TEMPLATE_NOT_FOUND);
                return;
            }

            lMessageRequest.setRouteType(lKannelRouteInfo.getRouteType());

            final List<BaseMessage> lBaseMessageList  = lMessageRequest.getSubmissions();

            boolean                 isFirstPartFailed = false;
            boolean                 isPartialSuccess  = false;

            final boolean           canDoMsgRetry     = CHUtil.canMsgRetry(lMessageRequest);

            for (final BaseMessage baseMssage : lBaseMessageList)
            {
                final SubmissionObject lSubmissionObject = (SubmissionObject) baseMssage;
                if (log.isDebugEnabled())
                    log.debug("Splited Message Object : " + lSubmissionObject);

                try
                {

                    if (isFirstPartFailed && !isPartialSuccess)
                    {
                        // log.fatal("First part carrier handover is failed, Hence ignoring the remining
                        // part messages...Message Id :" + lSubmissionObject.getMessageId());
                        log.fatal("First part carrier handover is failed, Hence ignoring the remining part messages...Message Id :" + lSubmissionObject.getMessageId());
                        return;
                    }

                    if (isPartialSuccess && isFirstPartFailed)
                    {
                        if (log.isDebugEnabled())
                            log.debug("Unable to process the Multipart request to kannel for the route '" + lRouteId + ", partially failed' , Hence rejecting the request..");
                        sendToPlatfromRejection(lSubmissionObject, PlatformStatusCode.PARTIALLY_CARRIER_HANDOVER_FAILED);
                        continue;
                    }

                    final String lMessageId = lSubmissionObject.getMessageId();

                    String       lUdh       = CommonUtility.nullCheck(lSubmissionObject.getUdh(), true);

                    if (!lUdh.isEmpty())
                    {
                        if (log.isDebugEnabled())
                            log.debug("Udh Value : " + lUdh);

                        if (CHUtil.isValidUDH(lUdh))
                            lUdh = CHUtil.addKannelSpecCharToHex(lUdh);
                        else
                        {
                            log.error("Invalid UDH : " + lSubmissionObject);
                            sendToPlatfromRejection(lSubmissionObject, PlatformStatusCode.INVALID_UDH);
                            continue;
                        }
                    }

                    // msg_replace_chk value from account table.
                    final boolean isDLTEnable = CommonUtility.isEnabled(CHUtil.getAppConfigValueAsString(ConfigParamConstants.DLT_ENABLE));

                    if (!isDLTEnable)
                        messageReplaceCheck(lSubmissionObject, lMessageRequest, lRouteId);

                    int           lRetryAttempt          = lMessageRequest.getRetryAttempt();
               
                    if (lKannelRouteInfo.isDummyRoute() && (!lKannelRouteInfo.getCarrierFullDn().isEmpty()))
                    {
                    	sendDummyRoute(lSubmissionObject,lKannelRouteInfo,lMessageRequest);
                    	continue;
                    }

                    final boolean isKannelAvailable = CHProcessUtil.isKannelAvailable(lRouteId);

                    if (log.isDebugEnabled())
                        log.debug("Kannel Available Status : " + isKannelAvailable);

                    final String lActualRouteId = lMessageRequest.getActualRouteId();

                    if ((lSubmissionObject.getMtMessageRetryIdentifier() == null) || !lRouteId.equals(lActualRouteId))
                        setCallBackUrl(lMessageRequest, lSubmissionObject);

                    final String lKannelUrl = getKannelUrl(lKannelRouteInfo, lSubmissionObject, lMessageRequest, lUdh, lRetryAttempt, isDLTEnable);

                    if (log.isDebugEnabled())
                        log.debug("kannel URL--->" + lKannelUrl);

                    if (!isKannelAvailable && canDoMsgRetry)
                    {
                        // Set the isFirstPartFailed flag to 'true' for Multipart Request.
                        if (lMessageRequest.getMessageTotalParts() > 1)
                            isFirstPartFailed = true;

                        doMessageRetry(lMessageRequest, lSubmissionObject);
                        continue;
                    }

                    final Timer      lKannelConnect = PrometheusMetrics.componentMethodStartTimer(Component.CH, mPlatformCluster, "KannelConnect");

                    final HttpResult lHttpResult    = BasicHttpConnector.connect(lKannelUrl);
                    final boolean    lResponse      = lHttpResult.isSuccess();

                    PrometheusMetrics.componentMethodEndTimer(Component.CH, lKannelConnect);

                    if (log.isDebugEnabled())
                        log.debug("URL : '" + lKannelUrl + "', Response : '" + lResponse + "'");

                    setKannelResponseTime(lKannelUrl, lRouteId, lResponse);

                    if (lResponse)
                    {
                        isPartialSuccess = true;
                        lRetryAttempt    = lSubmissionObject.getRetryAttempt();
                        final String lRoute_Id = lMessageRequest.getRouteId();
                        if (log.isDebugEnabled())
                            log.debug("Route ID : " + lRoute_Id);

                        if (lRetryAttempt != 0)
                        {
                            // retry msg send to INTERIM_FAILURE topic
                            CHProducer.sendToInterim(lSubmissionObject);
                            if (log.isDebugEnabled())
                                log.debug("send to interm queue success mid:" + lMessageId);
                        }

                        lSubmissionObject.setSubOriginalStatusCode(PlatformStatusCode.SUCCESS.getStatusCode());

                        if (log.isDebugEnabled())
                            log.debug("Retry Attempt Count : " + lRetryAttempt);

                        if (lRetryAttempt == 0)
                        {
                            CHProducer.sendToSubBilling(lSubmissionObject);
                            if (log.isDebugEnabled())
                                log.debug("Sent to submission biller topic.. success");
                        }
                    }
                    else
                    {
                        if (log.isDebugEnabled())
                            log.debug("\n url : " + lKannelUrl + " : \n response : " + lResponse);

                        isFirstPartFailed = true;

                        if (isPartialSuccess && isFirstPartFailed)
                        {
                            if (log.isDebugEnabled())
                                log.debug("Unable to process the Multipart request to kannel for the route '" + lRouteId + ", partially failed' , Hence rejecting the request..");
                            sendToPlatfromRejectionWithRemovePayload(lSubmissionObject, PlatformStatusCode.PARTIALLY_CARRIER_HANDOVER_FAILED);
                        }
                        else
                        {
                            if (log.isDebugEnabled())
                                log.debug("");

                            if (isFirstPartFailed)
                            {
                                if (log.isDebugEnabled())
                                    log.debug("First part failed ...");

                                if (canDoMsgRetry)
                                {
                                    if (log.isDebugEnabled())
                                        log.debug("Message Retry Enabled & First part failed ...:'" + isFirstPartFailed + "', Hence sending to Message Retry..");

                                    doMessageRetry(lMessageRequest, lSubmissionObject);
                                }
                                else
                                {
                                    if (log.isDebugEnabled())
                                        log.debug("Message Retry Disabled, Unable to send the Multipart request to kannel for the route '" + lRouteId + "' , Hence rejecting the request..");

                                    sendToPlatfromRejectionWithRemovePayload(lSubmissionObject, lMessageRequest, PlatformStatusCode.CARRIER_HANDOVER_FAILED);
                                }
                            }
                        }
                    }
                    if (log.isDebugEnabled())
                        log.debug("Message Id : " + lMessageId + " udh : " + lUdh);
                }
                catch (final Exception e2)
                {
                    log.error("Exception occer while processing Carrier Handover ...", e2);
                    isFirstPartFailed = true;

                    if (isPartialSuccess && isFirstPartFailed)
                    {
                        lSubmissionObject.setSubOriginalStatusCode(PlatformStatusCode.PARTIALLY_CARRIER_HANDOVER_FAILED.getStatusCode());
                        lSubmissionObject.setAdditionalErrorInfo("CH :" + e2.getMessage());
                        CHProducer.sendToNextLevel(lSubmissionObject, lMessageRequest, true);
                    }
                    else
                    {
                        if (log.isDebugEnabled())
                            log.debug("");

                        if (isFirstPartFailed)
                        {
                            if (log.isDebugEnabled())
                                log.debug("Due to exception sending to PRC..., Base Mid :'" + lMessageRequest.getBaseMessageId() + "'");

                            lMessageRequest.setSubOriginalStatusCode(PlatformStatusCode.CARRIER_HANDOVER_FAILED.getStatusCode());
                            lMessageRequest.setAdditionalErrorInfo("CH :" + e2.getMessage());

                            CHProducer.sendToNextLevel(lSubmissionObject, lMessageRequest, false);
                        }
                    }
                }
            }
        }
        catch (final Exception e)
        {
            log.error("Exception occer while processing Carrier Handover ...", e);

            try
            {
                CHProducer.sendToErrorLog(lMessageRequest, e);
            }
            catch (final Exception e1)
            {
                e1.printStackTrace();
            }
        }
    }

    private void sendDummyRoute(final SubmissionObject lSubmissionObject,final RouteKannelInfo lKannelRouteInfo,final MessageRequest lMessageRequest) {
		

        lSubmissionObject.setCarrierFullDn(lKannelRouteInfo.getCarrierFullDn());

        final Timer setCarrierSMTime = PrometheusMetrics.componentMethodStartTimer(Component.CH, mPlatformCluster, "SetCarrierSMTime");

        // Set the Retry-attempt
        GenerateDNUrl.setDlrUrl(lSubmissionObject, lMessageRequest.getRetryAttempt());

        PrometheusMetrics.componentMethodEndTimer(Component.CH, setCarrierSMTime);

        final Timer dummyHandover = PrometheusMetrics.componentMethodStartTimer(Component.CH, mPlatformCluster, "DummyHandover");

        if (log.isDebugEnabled())
            log.debug("Sending to dummy route q:" + lSubmissionObject.getMessageId() + " retry attempt:" + lMessageRequest.getRetryAttempt());

   
            if (log.isInfoEnabled())
                log.info("Request sending to dummy route....");
            CHProducer.sendToDummyRoute(lSubmissionObject);
       

        if (lMessageRequest.getRetryAttempt() == 0)
        {
            lSubmissionObject.setSubOriginalStatusCode(PlatformStatusCode.SUCCESS.getStatusCode());
            CHProducer.sendToSubBilling(lSubmissionObject);
        }

        PrometheusMetrics.componentMethodEndTimer(Component.CH, dummyHandover);


		
	}

	private static void setKannelResponseTime(
            String aKannelUrl,
            String aRouteId,
            boolean aResponse)
    {

        try
        {
            final long    lKannelHitStartTime = System.currentTimeMillis();
            final long    lKannelHitEndTime   = System.currentTimeMillis();

            final boolean isResponseCheck     = CommonUtility.isEnabled(CHProcessUtil.getAppConfigValueAsString(ConfigParamConstants.KANNEL_CONN_RESP_CHK));

            if (log.isDebugEnabled())
                log.debug("Check Respone Time '" + isResponseCheck + "'");

            if (isResponseCheck)
                KannelStatsCollector.getInstance().verifyKannelResponseTime(aKannelUrl, aRouteId, lKannelHitStartTime, lKannelHitEndTime, aResponse);
        }
        catch (final Exception e)
        {
            log.error("Some error in calculating the times.", e);
        }
    }

    private static void setCallBackUrl(
            MessageRequest aMessageRequest,
            SubmissionObject aSubmissionObject)
            throws UnsupportedEncodingException,
            InterruptedException
    {
        final String lClusterDNReceiverInfo = ICHUtil.getClusterDNReceiverInfo(aMessageRequest.getClusterType().getKey());
        String       lDlrUrl                = null;

        final Timer  removePayloadTimer     = PrometheusMetrics.componentMethodStartTimer(Component.CH, aMessageRequest.getClusterType(), "removeAndStorePayload");
        removeAndStorePayload(aSubmissionObject);
        PrometheusMetrics.componentMethodEndTimer(Component.CH, removePayloadTimer);

        final String additionalInfoString = CHUtil.getCallBackParams(aSubmissionObject);

        if (log.isDebugEnabled())
            log.debug("additionalInfoString===>" + additionalInfoString);

        final String encodedAdditionalInfo = URLEncoder.encode(additionalInfoString, Constants.ENCODER_FORMAT);
        lDlrUrl = CHUtil.generateCallBackUrl(lClusterDNReceiverInfo, encodedAdditionalInfo);
        if (log.isDebugEnabled())
            log.debug("Kannel dn URL--->" + lDlrUrl);
        aSubmissionObject.setCallBackUrl(lDlrUrl);
    }

    private static void removeAndStorePayload(
            SubmissionObject aSubmissionObject)
            throws InterruptedException
    {
        String lPayloadRid = null;

        if (log.isDebugEnabled())
            log.debug("Attempting to remove payload....");
        PayloadProcessor.removePayload(aSubmissionObject);
        if (log.isDebugEnabled())
            log.debug("remove payload.... finished");

        while (lPayloadRid == null)
        {
            lPayloadRid = PayloadProcessor.storePayload(aSubmissionObject);

            if (lPayloadRid == null)
            {
                if (log.isDebugEnabled())
                    log.debug("payload rid null retrying after 100 millis...");
                Thread.sleep(100);
            }
        }
        if (log.isDebugEnabled())
            log.debug("payload storing to redis succesful for payloadrid=" + lPayloadRid);
        aSubmissionObject.setPayloadRedisId(lPayloadRid);
    }

    private static void messageReplaceCheck(
            SubmissionObject aSubmissionObject,
            MessageRequest aMessageRequest,
            String aRouteId)
    {
        final boolean lMsgReplaceChk = CommonUtility.isEnabled(CommonUtility.nullCheck(aMessageRequest.getMsgAlertCheck(), true));

        if (lMsgReplaceChk)
        {
            final String lCarrier = aMessageRequest.getCarrier();
            final String lCircle  = aMessageRequest.getCircle();

            if (ICHUtil.canReplaceKeywordInMessage(aMessageRequest.getClientId(), lCarrier, lCircle, aRouteId))
            {
                final String lReplaceMsg = ICHUtil.getReplacedMessage(aMessageRequest.getClientId(), aSubmissionObject.getMessage());

                if (log.isInfoEnabled())
                    log.info("Altered Message :" + lReplaceMsg);

                aSubmissionObject.setAlterMessage(lReplaceMsg);
            }
        }
    }

    private static String getKannelUrl(
            RouteKannelInfo aKannelRouteInfo,
            SubmissionObject aSubmissionObject,
            MessageRequest aMessageRequest,
            String aUdh,
            int aRetryAttempt,
            boolean isDLTEnable)
            throws UnsupportedEncodingException
    {
        final String   lMessage   = CHUtil.getMessage(aSubmissionObject);
        final String   lClientId  = aMessageRequest.getClientId();

        String[]       lUrlparams = new String[]
        { URLEncoder.encode(aKannelRouteInfo.getKannelIp(), Constants.ENCODER_FORMAT), // 0
                URLEncoder.encode(aKannelRouteInfo.getKannelPort(), Constants.ENCODER_FORMAT), // 1
                URLEncoder.encode(aKannelRouteInfo.getSmscId(), Constants.ENCODER_FORMAT), // 2
                URLEncoder.encode(aSubmissionObject.getMobileNumber(), Constants.ENCODER_FORMAT), // 3
                URLEncoder.encode(ICHUtil.getHeader(aKannelRouteInfo, MessageUtil.getHeaderId(aMessageRequest)), Constants.ENCODER_FORMAT), // 4
                lMessage, // 5
                aUdh, // 6
                URLEncoder.encode(aSubmissionObject.getCallBackUrl(), Constants.ENCODER_FORMAT), // 7
                // chn
                // kannel
                // dlr_url
                // or
                // hexcode
                // value

                URLEncoder.encode(CHUtil.getPriority(aMessageRequest), Constants.ENCODER_FORMAT), // 8
                URLEncoder.encode((String.valueOf(aMessageRequest.getMaxValidityInSec() / 60)), Constants.ENCODER_FORMAT), // 9
                URLEncoder.encode(lClientId, Constants.ENCODER_FORMAT), // 10
                URLEncoder.encode(DateTimeUtility.getFormattedDateTime(aSubmissionObject.getMessageReceivedDate(), DateTimeFormat.DEFAULT_DATE_ONLY), Constants.ENCODER_FORMAT), // 11
                URLEncoder.encode(aMessageRequest.getMessagePriority().getKey(), Constants.ENCODER_FORMAT), // 12
                URLEncoder.encode(aMessageRequest.getMessagePriority().getKey(), Constants.ENCODER_FORMAT), // 13
                URLEncoder.encode(String.valueOf(aRetryAttempt), Constants.ENCODER_FORMAT) // 14

        };

        final String[] lDltParams = new String[2];

        if (isDLTEnable)
        {
            lDltParams[0] = URLEncoder.encode(CommonUtility.nullCheck(aMessageRequest.getDltEntityId(), true), Constants.ENCODER_FORMAT);
            lDltParams[1] = URLEncoder.encode(CommonUtility.nullCheck(aMessageRequest.getDltTemplateId(), true), Constants.ENCODER_FORMAT);

            final String[] lMerged = new String[lUrlparams.length + lDltParams.length];
            System.arraycopy(lUrlparams, 0, lMerged, 0, lUrlparams.length);
            System.arraycopy(lDltParams, 0, lMerged, lUrlparams.length, lDltParams.length);
            lUrlparams = lMerged;
        }

        return MessageFormat.format(aKannelRouteInfo.getUrlTemplate(), lUrlparams);
    }

    private static void doMessageRetry(
            MessageRequest aMessageRequest,
            SubmissionObject aSubmissionObject)
    {
        if (log.isDebugEnabled())
            log.debug("Message request Object : " + aMessageRequest);

        log.error("Sending to retry queue due to kannel down/storesize/latency route:" + aMessageRequest.getRouteId());

        try
        {
            aSubmissionObject.setMtMessageRetryIdentifier(Constants.ENABLED);
            PayloadProcessor.removePayload(aSubmissionObject);
        }
        catch (final Exception e)
        {
            log.error("Exception while removing payload ..", e);
        }

        CHProducer.sendToRetryRoute(aMessageRequest);
    }

    private static void sendToPlatfromRejectionWithRemovePayload(
            SubmissionObject aSubmissionObject,
            PlatformStatusCode aStatusId)
    {

        try
        {
            aSubmissionObject.setMtMessageRetryIdentifier(Constants.ENABLED);
            PayloadProcessor.removePayload(aSubmissionObject);
        }
        catch (final Exception e)
        {
            log.error("Exception while removing payload ..", e);
        }

        sendToPlatfromRejection(aSubmissionObject, aStatusId);
    }

    private static void sendToPlatfromRejectionWithRemovePayload(
            SubmissionObject aSubmissionObject,
            MessageRequest aMessageRequest,
            PlatformStatusCode aStatusId)
    {

        try
        {
            aSubmissionObject.setMtMessageRetryIdentifier(Constants.ENABLED);
            PayloadProcessor.removePayload(aSubmissionObject);
        }
        catch (final Exception e)
        {
            log.error("Exception while removing payload ..", e);
        }

        sendToPlatfromRejection(aMessageRequest, aStatusId);
    }

    private static void sendToPlatfromRejection(
            SubmissionObject aSubmissionObject,
            PlatformStatusCode aStatusId)
    {
        aSubmissionObject.setSubOriginalStatusCode(aStatusId.getStatusCode());
        CHProducer.sendToPlatfromRejection(aSubmissionObject);
    }

    private static void sendToPlatfromRejection(
            MessageRequest aMessageRequest,
            PlatformStatusCode aStatusId)
    {
        aMessageRequest.setSubOriginalStatusCode(aStatusId.getStatusCode());
        CHProducer.sendToPlatfromRejection(aMessageRequest);
    }

    @Override
    public void doCleanup()
    {
        // TODO Auto-generated method stub
    }

    @Override
    protected void updateBeforeSendBack(
            IMessage aMessage)
    {
        // TODO Auto-generated method stub
    }

}