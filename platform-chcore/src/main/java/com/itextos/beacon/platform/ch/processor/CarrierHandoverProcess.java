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
import com.itextos.beacon.commonlib.constants.ErrorMessage;
import com.itextos.beacon.commonlib.constants.MessageType;
import com.itextos.beacon.commonlib.constants.PlatformStatusCode;
import com.itextos.beacon.commonlib.httpclient.BasicHttpConnector;
import com.itextos.beacon.commonlib.httpclient.HttpResult;
import com.itextos.beacon.commonlib.kafkaservice.consumer.ConsumerInMemCollection;
import com.itextos.beacon.commonlib.message.BaseMessage;
import com.itextos.beacon.commonlib.message.IMessage;
import com.itextos.beacon.commonlib.message.MessageRequest;
import com.itextos.beacon.commonlib.message.SubmissionObject;
import com.itextos.beacon.commonlib.message.utility.MessageUtil;
import com.itextos.beacon.commonlib.prometheusmetricsutil.PrometheusMetrics;
import com.itextos.beacon.commonlib.utility.CommonUtility;
import com.itextos.beacon.commonlib.utility.DateTimeUtility;
import com.itextos.beacon.commonlib.utility.Name;
import com.itextos.beacon.inmemory.carrierhandover.RouteKannelInfo;
import com.itextos.beacon.inmemory.carrierhandover.util.ICHUtil;
import com.itextos.beacon.platform.carrierhandoverutility.util.CHUtil;
import com.itextos.beacon.platform.carrierhandoverutility.util.GenerateDNUrl;
import com.itextos.beacon.platform.ch.util.CHProcessUtil;
import com.itextos.beacon.platform.ch.util.CHProducer;
import com.itextos.beacon.platform.dnpayloadutil.PayloadProcessor;
import com.itextos.beacon.platform.kannelstatusupdater.process.response.KannelStatsCollector;

import io.prometheus.client.Histogram.Timer;

public class CarrierHandoverProcess
        extends
        AbstractKafkaComponentProcessor
{


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

     
    	lMessageRequest.getLogBuffer().append("\n").append(Name.getLineNumber()).append("\t").append(Name.getClassName()).append("\t").append(Name.getCurrentMethodName()).append("\t").append(lMessageRequest.getBaseMessageId()+" : Message Received ");

        CarrierHandoverProcess.forCH(lMessageRequest,mPlatformCluster);
    }

    public static void forCH(MessageRequest lMessageRequest,ClusterType mPlatformCluster) {
    	
    	   try
           {
               final Timer   lPlatformRejection = PrometheusMetrics.componentMethodStartTimer(Component.CH, mPlatformCluster, "platformRejection");
               final boolean isHexMsg           = lMessageRequest.isHexMessage();

               if (isHexMsg)
               {
                   final boolean isValidHexMessage = CHUtil.isValidHexMessage(lMessageRequest.getLongMessage());

                   if (!isValidHexMessage)
                   {
                   	lMessageRequest.getLogBuffer().append("\n").append(Name.getLineNumber()).append("\t").append(Name.getClassName()).append("\t").append(Name.getCurrentMethodName()).append("\t").append(lMessageRequest.getBaseMessageId()+" :: Invalid HEX Message : ");

                       sendToPlatfromRejection(lMessageRequest, PlatformStatusCode.INVALID_HEX_MESSAGE);
                       PrometheusMetrics.componentMethodEndTimer(Component.CH, lPlatformRejection);
                       return;
                   }
               }

               final String lFeatureCode = CommonUtility.nullCheck(lMessageRequest.getFeatureCode(), true);
               final String lRouteId     = CommonUtility.nullCheck(lMessageRequest.getRouteId(), true);

               if (lRouteId.isEmpty())
               {
               	lMessageRequest.getLogBuffer().append("\n").append(Name.getLineNumber()).append("\t").append(Name.getClassName()).append("\t").append(Name.getCurrentMethodName()).append("\t").append(lMessageRequest.getBaseMessageId()+" :: Unable to find out the Route Id : ");

                   sendToPlatfromRejection(lMessageRequest, PlatformStatusCode.EMPTY_ROUTE_ID);
                   PrometheusMetrics.componentMethodEndTimer(Component.CH, lPlatformRejection);
                   return;
               }

               if (lFeatureCode.isBlank())
               {
            	   
                  	lMessageRequest.getLogBuffer().append("\n").append(Name.getLineNumber()).append("\t").append(Name.getClassName()).append("\t").append(Name.getCurrentMethodName()).append("\t").append(lMessageRequest.getBaseMessageId()+" :: Unable to find out the Feature Code : ");

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
                  
                 	lMessageRequest.getLogBuffer().append("\n").append(Name.getLineNumber()).append("\t").append(Name.getClassName()).append("\t").append(Name.getCurrentMethodName()).append("\t").append(lMessageRequest.getBaseMessageId()+" :: Message Expired :" + isExpired);

                   sendToPlatfromRejection(lMessageRequest, PlatformStatusCode.EXPIRED_MESSAGE);
                   return;
               }

               final Timer           lDeliveryRouteInfo = PrometheusMetrics.componentMethodStartTimer(Component.CH, mPlatformCluster, "getDeliveryRouteInfo");
               final RouteKannelInfo lKannelRouteInfo   = ICHUtil.getDeliveryRouteInfo(lRouteId, lFeatureCode);
               PrometheusMetrics.componentMethodEndTimer(Component.CH, lDeliveryRouteInfo);

               if (lKannelRouteInfo == null)
               {
                	lMessageRequest.getLogBuffer().append("\n").append(Name.getLineNumber()).append("\t").append(Name.getClassName()).append("\t").append(Name.getCurrentMethodName()).append("\t").append(lMessageRequest.getBaseMessageId()+" :: Unable to find  Route Kannel Template for  route : " + lRouteId + " feature cd : " + lFeatureCode );

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
                  
                   try
                   {

                       if (isFirstPartFailed && !isPartialSuccess)
                       {
                           // log.fatal("First part carrier handover is failed, Hence ignoring the remining
                           // part messages...Message Id :" + lSubmissionObject.getMessageId());
                       	lMessageRequest.getLogBuffer().append("\n").append(Name.getLineNumber()).append("\t").append(Name.getClassName()).append("\t").append(Name.getCurrentMethodName()).append("\t").append(lMessageRequest.getBaseMessageId()+" :; First part carrier handover is failed, Hence ignoring the remining part messages...Message Id :" + lSubmissionObject.getMessageId() );

                           return;
                       }

                       if (isPartialSuccess && isFirstPartFailed)
                       {
                           	lMessageRequest.getLogBuffer().append("\n").append(Name.getLineNumber()).append("\t").append(Name.getClassName()).append("\t").append(Name.getCurrentMethodName()).append("\t").append(lMessageRequest.getBaseMessageId()+" :: Unable to process the Multipart request to kannel for the route '" + lRouteId + ", partially failed' , Hence rejecting the request.." );

                           sendToPlatfromRejection(lSubmissionObject, PlatformStatusCode.PARTIALLY_CARRIER_HANDOVER_FAILED);
                           continue;
                       }

                       final String lMessageId = lSubmissionObject.getMessageId();

                       String       lUdh       = CommonUtility.nullCheck(lSubmissionObject.getUdh(), true);

                       if (!lUdh.isEmpty())
                       {
                          

                          	lMessageRequest.getLogBuffer().append("\n").append(Name.getLineNumber()).append("\t").append(Name.getClassName()).append("\t").append(Name.getCurrentMethodName()).append("\t").append(lMessageRequest.getBaseMessageId()+" :: Udh Value : " + lUdh );

                           if (CHUtil.isValidUDH(lUdh))
                               lUdh = CHUtil.addKannelSpecCharToHex(lUdh);
                           else
                           {
                             	lMessageRequest.getLogBuffer().append("\n").append(Name.getLineNumber()).append("\t").append(Name.getClassName()).append("\t").append(Name.getCurrentMethodName()).append("\t").append(lMessageRequest.getBaseMessageId()+" :: Udh Value : " + lUdh +" ::: Invalid UDH : ");

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
                       	sendDummyRoute(lSubmissionObject,lKannelRouteInfo,lMessageRequest,mPlatformCluster);
                       	continue;
                       }

                       final boolean isKannelAvailable = CHProcessUtil.isKannelAvailable(lRouteId);

        
                     	lMessageRequest.getLogBuffer().append("\n").append(Name.getLineNumber()).append("\t").append(Name.getClassName()).append("\t").append(Name.getCurrentMethodName()).append("\t").append(lMessageRequest.getBaseMessageId()+" :: Kannel Available Status : " + isKannelAvailable );

                       final String lActualRouteId = lMessageRequest.getActualRouteId();

                       if ((lSubmissionObject.getMtMessageRetryIdentifier() == null) || !lRouteId.equals(lActualRouteId))
                           setCallBackUrl(lMessageRequest, lSubmissionObject);

                       final String lKannelUrl = getKannelUrl(lKannelRouteInfo, lSubmissionObject, lMessageRequest, lUdh, lRetryAttempt, isDLTEnable);

                    

                    	lMessageRequest.getLogBuffer().append("\n").append(Name.getLineNumber()).append("\t").append(Name.getClassName()).append("\t").append(Name.getCurrentMethodName()).append("\t").append(lMessageRequest.getBaseMessageId()+" :: kannel URL--->" + lKannelUrl );

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

                      
                   	lMessageRequest.getLogBuffer().append("\n").append(Name.getLineNumber()).append("\t").append(Name.getClassName()).append("\t").append(Name.getCurrentMethodName()).append("\t").append(lMessageRequest.getBaseMessageId()+" :: kannel URL--->" + lKannelUrl + "', Response : '" + lResponse + "'");

                       setKannelResponseTime(lMessageRequest,lKannelUrl, lRouteId, lResponse);

                       if (lResponse)
                       {
                           isPartialSuccess = true;
                           lRetryAttempt    = lSubmissionObject.getRetryAttempt();
                           final String lRoute_Id = lMessageRequest.getRouteId();
                           	lMessageRequest.getLogBuffer().append("\n").append(Name.getLineNumber()).append("\t").append(Name.getClassName()).append("\t").append(Name.getCurrentMethodName()).append("\t").append(lMessageRequest.getBaseMessageId()+" :: Route ID : " + lRoute_Id);

                           if (lRetryAttempt != 0)
                           {
                               // retry msg send to INTERIM_FAILURE topic
                               CHProducer.sendToInterim(lSubmissionObject);
                               
                              	lMessageRequest.getLogBuffer().append("\n").append(Name.getLineNumber()).append("\t").append(Name.getClassName()).append("\t").append(Name.getCurrentMethodName()).append("\t").append(lMessageRequest.getBaseMessageId()+" :: send to interm queue success :"  );

                            
                           }

                           lSubmissionObject.setSubOriginalStatusCode(PlatformStatusCode.SUCCESS.getStatusCode());

                   
                         	lMessageRequest.getLogBuffer().append("\n").append(Name.getLineNumber()).append("\t").append(Name.getClassName()).append("\t").append(Name.getCurrentMethodName()).append("\t").append(lMessageRequest.getBaseMessageId()+" :: Retry Attempt Count : " + lRetryAttempt  );

                           if (lRetryAttempt == 0)
                           {
                               CHProducer.sendToSubBilling(lSubmissionObject);
                               
                            	lMessageRequest.getLogBuffer().append("\n").append(Name.getLineNumber()).append("\t").append(Name.getClassName()).append("\t").append(Name.getCurrentMethodName()).append("\t").append(lMessageRequest.getBaseMessageId()+" :: Sent to submission biller topic.. success"  );

                           
                           }
                       }
                       else
                       {
                          

                       	lMessageRequest.getLogBuffer().append("\n").append(Name.getLineNumber()).append("\t").append(Name.getClassName()).append("\t").append(Name.getCurrentMethodName()).append("\t").append(lMessageRequest.getBaseMessageId()+"  url : " + lKannelUrl + " : \n response : " + lResponse );

                           isFirstPartFailed = true;

                           if (isPartialSuccess && isFirstPartFailed)
                           {
                             
                              
                              	lMessageRequest.getLogBuffer().append("\n").append(Name.getLineNumber()).append("\t").append(Name.getClassName()).append("\t").append(Name.getCurrentMethodName()).append("\t").append(lMessageRequest.getBaseMessageId()+" :: Unable to process the Multipart request to kannel for the route '" + lRouteId + ", partially failed' , Hence rejecting the request.." );

                               
                               sendToPlatfromRejectionWithRemovePayload(lSubmissionObject, PlatformStatusCode.PARTIALLY_CARRIER_HANDOVER_FAILED);
                           }
                           else
                           {
                           
                               if (isFirstPartFailed)
                               {
                                   	lMessageRequest.getLogBuffer().append("\n").append(Name.getLineNumber()).append("\t").append(Name.getClassName()).append("\t").append(Name.getCurrentMethodName()).append("\t").append(lMessageRequest.getBaseMessageId()+" :: First part failed ..." );

                                   if (canDoMsgRetry)
                                   {
                                  
                                      	lMessageRequest.getLogBuffer().append("\n").append(Name.getLineNumber()).append("\t").append(Name.getClassName()).append("\t").append(Name.getCurrentMethodName()).append("\t").append(lMessageRequest.getBaseMessageId()+" :: Message Retry Enabled & First part failed ...:'" + isFirstPartFailed + "', Hence sending to Message Retry.." );

                                       doMessageRetry(lMessageRequest, lSubmissionObject);
                                   }
                                   else
                                   {
                                  
                                     	lMessageRequest.getLogBuffer().append("\n").append(Name.getLineNumber()).append("\t").append(Name.getClassName()).append("\t").append(Name.getCurrentMethodName()).append("\t").append(lMessageRequest.getBaseMessageId()+" :: Message Retry Disabled, Unable to send the Multipart request to kannel for the route '" + lRouteId + "' , Hence rejecting the request.." );

                                       sendToPlatfromRejectionWithRemovePayload(lSubmissionObject, lMessageRequest, PlatformStatusCode.CARRIER_HANDOVER_FAILED);
                                   }
                               }
                           }
                       }
                    	lMessageRequest.getLogBuffer().append("\n").append(Name.getLineNumber()).append("\t").append(Name.getClassName()).append("\t").append(Name.getCurrentMethodName()).append("\t").append(lMessageRequest.getBaseMessageId()+" udh : " + lUdh );

                   }
                   catch (final Exception e2)
                   {
                      
                   	lMessageRequest.getLogBuffer().append("\n").append(Name.getLineNumber()).append("\t").append(Name.getClassName()).append("\t").append(Name.getCurrentMethodName()).append("\t").append(lMessageRequest.getBaseMessageId()+" :: Exception occer while processing Carrier Handover ..."+ErrorMessage.getStackTraceAsString(e2) );

                       
                       isFirstPartFailed = true;

                       if (isPartialSuccess && isFirstPartFailed)
                       {
                           lSubmissionObject.setSubOriginalStatusCode(PlatformStatusCode.PARTIALLY_CARRIER_HANDOVER_FAILED.getStatusCode());
                           lSubmissionObject.setAdditionalErrorInfo("CH :" + e2.getMessage());
                           CHProducer.sendToNextLevel(lSubmissionObject, lMessageRequest, true);
                       }
                       else
                       {
                        
                           if (isFirstPartFailed)
                           {
                           	lMessageRequest.getLogBuffer().append("\n").append(Name.getLineNumber()).append("\t").append(Name.getClassName()).append("\t").append(Name.getCurrentMethodName()).append("\t").append(lMessageRequest.getBaseMessageId()+" :: Due to exception sending to PRC..., Base Mid :'" );

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

              	lMessageRequest.getLogBuffer().append("\n").append(Name.getLineNumber()).append("\t").append(Name.getClassName()).append("\t").append(Name.getCurrentMethodName()).append("\t").append(lMessageRequest.getBaseMessageId()+" :: Exception occer while processing Carrier Handover ..."+ErrorMessage.getStackTraceAsString(e) );
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
    private static void sendDummyRoute(final SubmissionObject lSubmissionObject,final RouteKannelInfo lKannelRouteInfo,final MessageRequest lMessageRequest,ClusterType mPlatformCluster) {
		

        lSubmissionObject.setCarrierFullDn(lKannelRouteInfo.getCarrierFullDn());

        final Timer setCarrierSMTime = PrometheusMetrics.componentMethodStartTimer(Component.CH, mPlatformCluster, "SetCarrierSMTime");

        // Set the Retry-attempt
        GenerateDNUrl.setDlrUrl(lSubmissionObject, lMessageRequest.getRetryAttempt());

        PrometheusMetrics.componentMethodEndTimer(Component.CH, setCarrierSMTime);

        final Timer dummyHandover = PrometheusMetrics.componentMethodStartTimer(Component.CH, mPlatformCluster, "DummyHandover");

 
      	lMessageRequest.getLogBuffer().append("\n").append(Name.getLineNumber()).append("\t").append(Name.getClassName()).append("\t").append(Name.getCurrentMethodName()).append("\t").append(lMessageRequest.getBaseMessageId()+" Sending to dummy route q:" + lSubmissionObject.getMessageId() + " retry attempt:" + lMessageRequest.getRetryAttempt() );

   
           CHProducer.sendToDummyRoute(lSubmissionObject);
       

        if (lMessageRequest.getRetryAttempt() == 0)
        {
            lSubmissionObject.setSubOriginalStatusCode(PlatformStatusCode.SUCCESS.getStatusCode());
            CHProducer.sendToSubBilling(lSubmissionObject);
        }

        PrometheusMetrics.componentMethodEndTimer(Component.CH, dummyHandover);


		
	}

	private static void setKannelResponseTime(
            MessageRequest lMessageRequest, String aKannelUrl,
            String aRouteId,
            boolean aResponse)
    {

        try
        {
            final long    lKannelHitStartTime = System.currentTimeMillis();
            final long    lKannelHitEndTime   = System.currentTimeMillis();

            final boolean isResponseCheck     = CommonUtility.isEnabled(CHProcessUtil.getAppConfigValueAsString(ConfigParamConstants.KANNEL_CONN_RESP_CHK));

        

          	lMessageRequest.getLogBuffer().append("\n").append(Name.getLineNumber()).append("\t").append(Name.getClassName()).append("\t").append(Name.getCurrentMethodName()).append("\t").append(lMessageRequest.getBaseMessageId()+"  :; Check Respone Time '" + isResponseCheck + "'" );

            if (isResponseCheck)
                KannelStatsCollector.getInstance().verifyKannelResponseTime(aKannelUrl, aRouteId, lKannelHitStartTime, lKannelHitEndTime, aResponse);
        }
        catch (final Exception e)
        {
        	
          	lMessageRequest.getLogBuffer().append("\n").append(Name.getLineNumber()).append("\t").append(Name.getClassName()).append("\t").append(Name.getCurrentMethodName()).append("\t").append(lMessageRequest.getBaseMessageId()+" :: Some error in calculating the times. :: "+ErrorMessage.getStackTraceAsString(e) );

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


      	aMessageRequest.getLogBuffer().append("\n").append(Name.getLineNumber()).append("\t").append(Name.getClassName()).append("\t").append(Name.getCurrentMethodName()).append("\t").append(aMessageRequest.getBaseMessageId()+" :: additionalInfoString===>" + additionalInfoString );

        final String encodedAdditionalInfo = URLEncoder.encode(additionalInfoString, Constants.ENCODER_FORMAT);
        lDlrUrl = CHUtil.generateCallBackUrl(lClusterDNReceiverInfo, encodedAdditionalInfo);
       
      	aMessageRequest.getLogBuffer().append("\n").append(Name.getLineNumber()).append("\t").append(Name.getClassName()).append("\t").append(Name.getCurrentMethodName()).append("\t").append(aMessageRequest.getBaseMessageId()+" :: Kannel dn URL--->" + lDlrUrl );

        aSubmissionObject.setCallBackUrl(lDlrUrl);
    }

    private static void removeAndStorePayload(
            SubmissionObject aSubmissionObject)
            throws InterruptedException
    {
        String lPayloadRid = null;

        aSubmissionObject.getLogBuffer().append("\n").append(Name.getLineNumber()).append("\t").append(Name.getClassName()).append("\t").append(Name.getCurrentMethodName()).append("\t").append(aSubmissionObject.getBaseMessageId()+" :; Attempting to remove payload...." );

        PayloadProcessor.removePayload(aSubmissionObject);
  
        aSubmissionObject.getLogBuffer().append("\n").append(Name.getLineNumber()).append("\t").append(Name.getClassName()).append("\t").append(Name.getCurrentMethodName()).append("\t").append(aSubmissionObject.getBaseMessageId()+" :; Attempting to remove payload.... finished" );


        while (lPayloadRid == null)
        {
            lPayloadRid = PayloadProcessor.storePayload(aSubmissionObject);

            if (lPayloadRid == null)
            {
               
                aSubmissionObject.getLogBuffer().append("\n").append(Name.getLineNumber()).append("\t").append(Name.getClassName()).append("\t").append(Name.getCurrentMethodName()).append("\t").append(aSubmissionObject.getBaseMessageId()+" :: payload rid null retrying after 100 millis..." );

                Thread.sleep(100);
            }
        }

        aSubmissionObject.getLogBuffer().append("\n").append(Name.getLineNumber()).append("\t").append(Name.getClassName()).append("\t").append(Name.getCurrentMethodName()).append("\t").append(aSubmissionObject.getBaseMessageId()+" :: payload storing to redis succesful for payloadrid=" + lPayloadRid );

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

              

                aSubmissionObject.getLogBuffer().append("\n").append(Name.getLineNumber()).append("\t").append(Name.getClassName()).append("\t").append(Name.getCurrentMethodName()).append("\t").append(aSubmissionObject.getBaseMessageId()+" :: Altered Message :" + lReplaceMsg );

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
         aMessageRequest.getLogBuffer().append("\n").append(Name.getLineNumber()).append("\t").append(Name.getClassName()).append("\t").append(Name.getCurrentMethodName()).append("\t").append(aMessageRequest.getBaseMessageId()+" :: Sending to retry queue due to kannel down/storesize/latency route:" + aMessageRequest.getRouteId());


        try
        {
            aSubmissionObject.setMtMessageRetryIdentifier(Constants.ENABLED);
            PayloadProcessor.removePayload(aSubmissionObject);
        }
        catch (final Exception e)
        {
            
            aMessageRequest.getLogBuffer().append("\n").append(Name.getLineNumber()).append("\t").append(Name.getClassName()).append("\t").append(Name.getCurrentMethodName()).append("\t").append(aMessageRequest.getBaseMessageId()+" :: Exception while removing payload .. :: "+ErrorMessage.getStackTraceAsString(e));

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
            
            aSubmissionObject.getLogBuffer().append("\n").append(Name.getLineNumber()).append("\t").append(Name.getClassName()).append("\t").append(Name.getCurrentMethodName()).append("\t").append(aSubmissionObject.getBaseMessageId()+" Exception while removing payload .. :: "+ErrorMessage.getStackTraceAsString(e));

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
            
            aSubmissionObject.getLogBuffer().append("\n").append(Name.getLineNumber()).append("\t").append(Name.getClassName()).append("\t").append(Name.getCurrentMethodName()).append("\t").append(aSubmissionObject.getBaseMessageId()+" Exception while removing payload .. :: "+ErrorMessage.getStackTraceAsString(e));

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