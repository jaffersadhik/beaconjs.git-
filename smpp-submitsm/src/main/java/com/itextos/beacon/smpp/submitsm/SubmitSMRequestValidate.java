package com.itextos.beacon.smpp.submitsm;


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.cloudhopper.smpp.SmppConstants;
import com.cloudhopper.smpp.pdu.SubmitSm;
import com.cloudhopper.smpp.pdu.SubmitSmResp;
import com.itextos.beacon.commonlib.constants.ClusterType;
import com.itextos.beacon.commonlib.constants.ConfigParamConstants;
import com.itextos.beacon.commonlib.constants.DCS;
import com.itextos.beacon.commonlib.constants.InterfaceStatusCode;
import com.itextos.beacon.commonlib.constants.MessageClass;
import com.itextos.beacon.commonlib.constants.PlatformStatusCode;
import com.itextos.beacon.commonlib.constants.RouteType;
import com.itextos.beacon.commonlib.constants.UdhHeaderInfo;
import com.itextos.beacon.commonlib.message.MessageRequest;
import com.itextos.beacon.commonlib.messageidentifier.MessageIdentifier;
import com.itextos.beacon.commonlib.utility.CommonUtility;
import com.itextos.beacon.commonlib.utility.mobilevalidation.MobileNumberValidator;
import com.itextos.beacon.http.interfaceutil.InterfaceUtil;
import com.itextos.beacon.platform.k2rconcatenatesmpp.process.ConcatenateReceiver;
import com.itextos.beacon.smpp.dto.SessionDetail;
import com.itextos.beacon.smpp.dto.SmppMessageRequest;
import com.itextos.beacon.smpp.dto.SubmitSmRequest;
import com.itextos.beacon.smpp.schedule.ScheduleValidate;
import com.itextos.beacon.smpp.throttle.ThrottleValidate;
import com.itextos.beacon.smpp.tlv.TLVValidate;
import com.itextos.beacon.smpp.utils.AccountDetails;
import com.itextos.beacon.smpp.utils.SmppErrorCodes;
import com.itextos.beacon.smslog.ConcateReceiverLog;

public class SubmitSMRequestValidate
{

    private static final Log    log                    = LogFactory.getLog(SubmitSMRequestValidate.class);

    private static final String ACCOUNT_SMPP_BIND_TYPE = "smpp_bind_type";

    private SubmitSMRequestValidate()
    {}

    public static void validateSubmitSm(
            SubmitSm aSubmitSmRequest,
            SubmitSmResp aSubmitSmResponse,
            SessionDetail aSessionDetail,
            StringBuffer sb)
    {
        final SubmitSmRequest    lSmRequest          = new SubmitSmRequest(aSubmitSmRequest, aSubmitSmResponse, aSessionDetail,sb);

        final SmppMessageRequest lSmppMessageRequest = lSmRequest.getMessageRequest();

        try
        {

            if (aSubmitSmResponse.getCommandStatus() != 0)
            {
             //   lSmppMessageRequest.setRouteType(RouteType.DOMESTIC);
                log.warn("Failed Invalid Expiry with command status=" + aSubmitSmResponse.getCommandStatus());
                return;
            }

            validateUDHForSP(aSubmitSmResponse, lSmppMessageRequest);

            if (aSubmitSmResponse.getCommandStatus() != 0)
            {
                log.warn("Failed Invalid UDH with command status=" + aSubmitSmResponse.getCommandStatus());
                return;
            }

            validateDest(aSubmitSmResponse, aSessionDetail, lSmppMessageRequest);

            if (aSubmitSmResponse.getCommandStatus() != 0)
            {
                log.warn("Failed Invalid Destiation with command status=" + aSubmitSmResponse.getCommandStatus());
                return;
            }

            validateHeader(lSmppMessageRequest, aSubmitSmResponse);

            if (aSubmitSmResponse.getCommandStatus() != 0)
            {
                log.warn("Failed Invalid Header with command status=" + aSubmitSmResponse.getCommandStatus());
                return;
            }

            validateMessage(lSmppMessageRequest, aSubmitSmResponse);

            if (aSubmitSmResponse.getCommandStatus() != 0)
            {
                log.warn("Failed message will not processed with Message Empty, command status=" + aSubmitSmResponse.getCommandStatus());
                return;
            }

            TLVValidate.validateDltEntityId(lSmppMessageRequest, aSubmitSmResponse);

            if (aSubmitSmResponse.getCommandStatus() != 0)
            {
                log.warn("Failed message will not processed with Invalid DLT Entity Id, command status=" + aSubmitSmResponse.getCommandStatus());
                return;
            }

            TLVValidate.validateDltTemplateId(lSmppMessageRequest, aSubmitSmResponse);

            if (aSubmitSmResponse.getCommandStatus() != 0)
            {
                log.warn("Failed message will not processed with Invalid DLT Template Id, command status=" + aSubmitSmResponse.getCommandStatus());
                return;
            }

            ThrottleValidate.throttleMessage(aSessionDetail, aSubmitSmResponse, lSmppMessageRequest);

            if (aSubmitSmResponse.getCommandStatus() != 0)
            {
                log.warn("Failed message will not processed with Message Throttle Failed, command status=" + aSubmitSmResponse.getCommandStatus());
                return;
            }

            ScheduleValidate.checkTimeOfDelivery(lSmppMessageRequest, aSubmitSmResponse, aSessionDetail);

            if (aSubmitSmResponse.getCommandStatus() != 0)
            {
                log.warn("Failed message will not processed with TRA/SCHEDULE Failed, command status=" + aSubmitSmResponse.getCommandStatus());
                return;
            }
        }
        finally
        {
            sendToPlatform(lSmppMessageRequest, aSubmitSmResponse, aSessionDetail,sb);
        }
    }

    private static void validateUDHForSP(
            SubmitSmResp aSubmitSmResponse,
            SmppMessageRequest aSmppMessageRequest)
    {

        if ((aSmppMessageRequest.getMsgClass() == MessageClass.SP_PLAIN_MESSAGE) || (aSmppMessageRequest.getMsgClass() == MessageClass.SP_UNICODE_MESSAGE))
        {
            if (log.isDebugEnabled())
                log.debug("Validate UDH for Special-Port Message udh:'" + aSmppMessageRequest.getUdh() + "'");

            if (!aSmppMessageRequest.getUdh().startsWith(UdhHeaderInfo.CONCAT_PORT_HEADER_PREFIX.getKey()))
            {
            //    aSmppMessageRequest.setRouteType(RouteType.DOMESTIC);
                aSmppMessageRequest.setInterfaceErrorCode(InterfaceStatusCode.INVALID_UDH.getStatusCode());
                aSubmitSmResponse.setCommandStatus(SmppConstants.STATUS_SUBMITFAIL);
            }
        }
    }

    private static void sendToPlatform(
            SmppMessageRequest aSubmitSmRequest,
            SubmitSmResp aSubmitSmResp,
            SessionDetail aSessionDetail,
            StringBuffer sb)
    {
        final String lMessageId = MessageIdentifier.getInstance().getNextId();
        aSubmitSmResp.setMessageId(lMessageId);
        aSubmitSmRequest.setAppInstanceId(MessageIdentifier.getInstance().getAppInstanceId());
        aSubmitSmRequest.setAckid(lMessageId);
        aSubmitSmRequest.setSmppInstance(aSessionDetail.getInstanceId());
        // Push Message
        mwHandover(aSubmitSmRequest, aSessionDetail, aSubmitSmResp,sb);
    }

   
    private static void validateMessage(
            SmppMessageRequest aSmppMessageRequest,
            SubmitSmResp aSubmitSmResp)
    {
        final String lMessage = CommonUtility.nullCheck(aSmppMessageRequest.getMessage(), true);

        if (lMessage.isBlank())
        {
            aSubmitSmResp.setCommandStatus(SmppConstants.STATUS_INVMSGLEN);
            aSmppMessageRequest.setInterfaceErrorCode(InterfaceStatusCode.MESSAGE_EMPTY.getStatusCode());
        }
    }

    private static void validateDest(
            SubmitSmResp aSubmitSmResponse,
            SessionDetail aSessionDetail,
            SmppMessageRequest aSmppMessageRequest)
    {
        final String lMobileNumber = CommonUtility.nullCheck(aSmppMessageRequest.getMobileNumber(), true);

        if (lMobileNumber.isEmpty())
        {
      //      aSmppMessageRequest.setRouteType(RouteType.DOMESTIC);
            aSubmitSmResponse.setCommandStatus(SmppConstants.STATUS_INVDSTADR);
            aSmppMessageRequest.setInterfaceErrorCode(InterfaceStatusCode.DESTINATION_EMPTY.getStatusCode());
            return;
        }

        final String                lCountryCD                        = InterfaceUtil.getDefaultCountryCode();
        final boolean               isConsiderDefaultLengthAsDomestic = aSessionDetail.considerDefaultLengthAsDomesitic();
        final boolean               isDomesticSpecialSeriesAllow      = aSessionDetail.isDomesticSpecialSeriesAllow();
        final boolean               isIntlServiceAllow                = isIntlServiceAllow(aSessionDetail);
        final MobileNumberValidator lMobileValidator                  = InterfaceUtil.validateMobile(lMobileNumber, lCountryCD, isIntlServiceAllow, isConsiderDefaultLengthAsDomestic, false, "",
                isDomesticSpecialSeriesAllow);

        final boolean               isValidMobile                     = lMobileValidator.isValidMobileNumber();

        if (log.isDebugEnabled())
            log.debug("Is Valid MobileNumber : " + isValidMobile);

        if (isValidMobile)
        {
            aSmppMessageRequest.setMobileNumber(lMobileValidator.getMobileNumber());
            RouteType lRouteType = RouteType.DOMESTIC;

            if (lMobileValidator.isIntlMobileNumber())
            {

                if (isIntlServiceAllow)
                    lRouteType = RouteType.INTERNATIONAL;
                else
                {
                    // Reject as INTL Serivce not available.
                    aSubmitSmResponse.setCommandStatus(SmppConstants.STATUS_SUBMITFAIL);
                    aSmppMessageRequest.setInterfaceErrorCode(InterfaceStatusCode.INTL_SERVICE_DISABLED.getStatusCode());
                }
            }
            else
            {
                if (log.isDebugEnabled())
                    log.debug("Domestic Number --'" + lMobileValidator.getMobileNumber() + "'");
                // set special Series domestic info
                if (lMobileValidator.isSpecialSeriesNumber())
                    aSmppMessageRequest.setSpecialSeriesNumber(lMobileValidator.isSpecialSeriesNumber());
            }

            aSmppMessageRequest.setRouteType(lRouteType);
        }
        else
        {
            aSmppMessageRequest.setRouteType(RouteType.DOMESTIC);
            aSubmitSmResponse.setCommandStatus(SmppConstants.STATUS_INVDSTADR);
            aSmppMessageRequest.setInterfaceErrorCode(InterfaceStatusCode.DESTINATION_INVALID.getStatusCode());
        }
    }

    /*
     * private static void canProcessDND(
     * String aMobileNumber,
     * SubmitSmResp aSubmitResponse,
     * SessionDetail aSessionDetail)
     * {
     * final int lDndPref = aSessionDetail.getDndPreferences();
     * if (log.isDebugEnabled())
     * log.debug("Before process DND check MobileNumber - " + aMobileNumber +
     * " DND_PREF : " + lDndPref);
     * try
     * {
     * final boolean isDndRejectEnable = aSessionDetail.isDndRejectYN();
     * if (isDndRejectEnable)
     * {
     * final String dndNcpr = DNDCheck.getDNDInfo(aMobileNumber);
     * if (((dndNcpr != null) && dndNcpr.equalsIgnoreCase("0")) || ((dndNcpr !=
     * null) && (lDndPref == 0)) || ((dndNcpr != null) &&
     * !dndNcpr.contains(String.valueOf(lDndPref))))
     * {
     * if (log.isWarnEnabled())
     * log.warn("Message DND rejeceted...");
     * aSubmitResponse.setCommandStatus(SmppConstants.STATUS_INVDSTADR);
     * }
     * }
     * }
     * catch (final Exception e)
     * {
     * log.error(" DND Check err", e);
     * }
     * }
     */
   
    public static boolean isIntlServiceAllow(
            SessionDetail aSessionDetail)
    {
        return aSessionDetail.isIntlServiceAllowed();
    }

    private static void mwHandover(
            SmppMessageRequest aSmppMessageRequest,
            SessionDetail aSessionDetail,
            SubmitSmResp aSubmitResponse,
            StringBuffer sb)
    {
        boolean            isValidDCS          = true;

        PlatformStatusCode aPlatformStatusCode = null;

        try
        {
            final DCS dcs = DCS.getDcs(aSmppMessageRequest.getDcs());
            dcs.getKey();
        }
        catch (final Exception e)
        {
            isValidDCS = false;
            log.fatal(aSmppMessageRequest.getSystemId() + ", Invalid Data Coding Scheme :'" + aSmppMessageRequest.getDcs() + "'");
            aPlatformStatusCode = PlatformStatusCode.INVALID_DATA_CODING_SCHEME;
        }

        if (aSmppMessageRequest.isConcatMessage() && (aSubmitResponse.getCommandStatus() == 0) && isValidDCS)
        {
            if (log.isDebugEnabled())
                log.debug("Processing multipart request ...........");
            concatTemplateProcess(aSmppMessageRequest, aSessionDetail, aSubmitResponse,sb);
        }
        else
        {
            if (log.isDebugEnabled())
                log.debug("Processing singlepart request ...........");
            send2Platform(aSmppMessageRequest, aSubmitResponse, aSessionDetail, aPlatformStatusCode,sb);
        }
    }

    private static void concatTemplateProcess(
            SmppMessageRequest aSmppMessageRequest,
            SessionDetail aSessionDetail,
            SubmitSmResp aSubmitResponse,
            StringBuffer sb2)
    {

        try
        {
            final int    lMaxSplitAllow = CommonUtility.getInteger(AccountDetails.getConfigParamsValueAsString(ConfigParamConstants.MAX_SPLIT_PART_ALLOW));

            final String lClientId      = aSessionDetail.getClientId();
            final int    lTotalMsgParts = aSmppMessageRequest.getTotalParts();
            final int    lPartNumber    = aSmppMessageRequest.getPartNumber();

            if (log.isDebugEnabled())
                log.debug("Client Id :" + lClientId + " :: Total parts in message :" + lTotalMsgParts + " :: part no:" + lPartNumber);

            if (lTotalMsgParts > lMaxSplitAllow)
            {
                log.warn("Max Split allow parts > TotalMsgParts :" + lTotalMsgParts);

                if (lPartNumber <= lMaxSplitAllow)
                {
                    if (log.isDebugEnabled())
                        log.debug("Max Split allow parts < partnumer :" + lPartNumber);

                    final PlatformStatusCode lPlatformStatusCode = PlatformStatusCode.EXCEED_MAX_SPLIT_PARTS;
                    // accept set error codeas Max Split exceed and send to platform
                    send2Platform(aSmppMessageRequest, aSubmitResponse, aSessionDetail, lPlatformStatusCode,sb2);
                }
                else
                {
                    if (log.isDebugEnabled())
                        log.debug("This is Max Spllit Message, Hence disable the Concatinate Feature...");

                    aSubmitResponse.setCommandStatus(SmppConstants.STATUS_INVMSGLEN);
                }
            }
            else
            {
                if (log.isDebugEnabled())
                    log.debug("Total Parts :'" + lTotalMsgParts + ", PartNumber:'" + lPartNumber + "'");

                if (lTotalMsgParts < lPartNumber)
                {
                    if (log.isDebugEnabled())
                        log.debug("Current part number is more than total parts...");

                    aSmppMessageRequest.setInterfaceErrorCode(InterfaceStatusCode.SMPP_PARTNO_EXCEED_TOTAL_PART_COUNT.getStatusCode());
                    aSubmitResponse.setCommandStatus(SmppErrorCodes.STATUS_INVDMSGPART);
                    send2Platform(aSmppMessageRequest, aSubmitResponse, aSessionDetail, null,sb2);
                }
                else
                {
                    if (log.isDebugEnabled())
                        log.debug("Request sending to concat process.............");
                    sb2.append("Request sending to concat process.............");
                    StringBuffer sb=new StringBuffer();
                    sb.append("\n#################################################\n");
                    sb.append("\nconcate receiver : "+aSmppMessageRequest.getAckid()+"\n");

                    final long lStartTime = System.currentTimeMillis();

                    try
                    {
                        final ClusterType lClusterType = aSessionDetail.getPlatformCluster();
                        aSmppMessageRequest.setInterfaceErrorCode("");
                        ConcatenateReceiver.addSmppMessage(lClusterType, aSmppMessageRequest, true,sb);
                    }
                    catch (final Exception exp)
                    {
                        log.error("pushing concat message failed", exp);

                        aSubmitResponse.setCommandStatus(SmppConstants.STATUS_SYSERR);
                    }

                    if (log.isDebugEnabled())
                        log.debug("Concatinate Message Started at '" + lStartTime + "' Processed Time : " + (System.currentTimeMillis() - lStartTime));
               
                    sb.append("Concatinate Message Started at '" + lStartTime + "' Processed Time : " + (System.currentTimeMillis() - lStartTime)).append("\n");

                    sb.append("\n#################################################\n");

                    ConcateReceiverLog.log(sb.toString());
                
                }
            }
        }
        catch (final Exception e)
        {
            log.error(" Exception while sending the message to concat redis process..", e);
            aSubmitResponse.setCommandStatus(SmppConstants.STATUS_SYSERR);
        }
    }

    private static void send2Platform(
            SmppMessageRequest aSmppMessageRequest,
            SubmitSmResp aSubmitResponse,
            SessionDetail aSessionDetail,
            PlatformStatusCode aPlatformStatusCode,
            StringBuffer sb)
    {

        try
        {
            final MessageRequest lMessageRequest = BuildMessageRequest.getMessageRequest(aSmppMessageRequest, aSessionDetail, aPlatformStatusCode);

            if (log.isDebugEnabled())
                log.debug("Message Request Object sending to Kafka : " + lMessageRequest);

            InterfaceUtil.sendToKafka(lMessageRequest,sb);

            if (aSubmitResponse.getCommandStatus() == 0)
                aSubmitResponse.setCommandStatus(SmppConstants.STATUS_OK);

            if (log.isDebugEnabled())
                log.debug("Successfully send to kafka ........");
        }
        catch (final Exception e)
        {
            log.error("Exception while processing Message Request Object..", e);
            aSubmitResponse.setCommandStatus(SmppConstants.STATUS_SYSERR);
        }
    }

    private static void validateHeader(
            SmppMessageRequest aSmppMessageRequest,
            SubmitSmResp aSubmitSmResp)
    {
        final String lHeader = aSmppMessageRequest.getHeader();

        if (lHeader.isBlank())
        {
            aSubmitSmResp.setCommandStatus(SmppErrorCodes.STATUS_INVDSRC);
            aSmppMessageRequest.setInterfaceErrorCode(InterfaceStatusCode.SENDER_ID_EMPTY.getStatusCode());
        }

        if (lHeader.length() > 15)
        {
            aSubmitSmResp.setCommandStatus(SmppErrorCodes.STATUS_INVDSRC);
            aSmppMessageRequest.setInterfaceErrorCode(InterfaceStatusCode.INVALID_SENDERID.getStatusCode());
        }
    }

 

   

   

}
