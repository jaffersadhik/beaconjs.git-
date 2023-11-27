package com.itextos.beacon.platform.rc.process;

import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.itextos.beacon.commonlib.constants.ConfigParamConstants;
import com.itextos.beacon.commonlib.constants.HeaderSubType;
import com.itextos.beacon.commonlib.constants.HeaderType;
import com.itextos.beacon.commonlib.constants.PlatformStatusCode;
import com.itextos.beacon.commonlib.constants.RouteConstants;
import com.itextos.beacon.commonlib.constants.RouteLogic;
import com.itextos.beacon.commonlib.messageobject.MessageRequest;
import com.itextos.beacon.commonlib.messageobject.utility.MessageUtil;
import com.itextos.beacon.commonlib.pattern.PatternCache;
import com.itextos.beacon.commonlib.pattern.PatternCheckCategory;
import com.itextos.beacon.commonlib.utility.CommonUtility;
import com.itextos.beacon.inmemory.routeinfo.cache.RouteConfigInfo;
import com.itextos.beacon.inmemory.routeinfo.util.IntlRUtils;
import com.itextos.beacon.inmemory.routeinfo.util.RouteUtil;
import com.itextos.beacon.platform.rc.util.RCProducer;
import com.itextos.beacon.platform.rc.util.RCUtil;

public class RCIntlProcessor
{

    private static final Log     log = LogFactory.getLog(RCIntlProcessor.class);

    private final MessageRequest mMessageRequest;

    public RCIntlProcessor(
            MessageRequest aMessageRequest)
    {
        this.mMessageRequest = aMessageRequest;
    }

    public void doProcess()
    {
        boolean      isRouteFound              = false;
        boolean      isFailedListNumber        = false;
        boolean      isExpired                 = false;
        boolean      isInvalidIntlMobileLength = false;
        boolean      isCCNotInRange            = false;
        boolean      isMessageInGlobalTemplate = true;

        final String lBaseMessageId            = mMessageRequest.getBaseMessageId();

        isFailedListNumber = RCUtil.checkIntlFaillistNumber(mMessageRequest);

        if (log.isDebugEnabled())
            log.debug("Is Fail List number  : '" + isFailedListNumber + "'");

        if (isFailedListNumber)
            isMessageInGlobalTemplate = IntlRUtils.canProcessMessageByGlobalTemplate(mMessageRequest);

        if (log.isDebugEnabled())
            log.debug("Is Message By GlobalTemplate for MID :'" + lBaseMessageId + "' - " + isMessageInGlobalTemplate);

        if (isMessageInGlobalTemplate)
        {
            isRouteFound = IntlRUtils.setRouteUsingMobileNumber(mMessageRequest);

            if (log.isDebugEnabled())
                log.debug("Route found based on intl_mobile_routes : '" + isRouteFound + "'");

            if (!isRouteFound)
            {
                final boolean[] result = IntlRUtils.checkForSeriesBasedLookup(mMessageRequest);
                isRouteFound              = result[0];
                isInvalidIntlMobileLength = result[1];
                isCCNotInRange            = result[2];

                if (log.isDebugEnabled())
                    log.debug("Route found based on route_intl : '" + isRouteFound + "'");

                if (isRouteFound)
                    IntlRUtils.setRouteBasesdOnOtherCriteria(mMessageRequest);
                else
                    if (log.isDebugEnabled())
                        log.debug("No need to check again here for the Fail List number. Use the same variable:::::: Is Fail List number : '" + isFailedListNumber + "'");
            } // end of the mobile number route found.

            if (isRouteFound)
            {
                final String lRouteId = CommonUtility.nullCheck(mMessageRequest.getRouteId(), true);

                if (log.isDebugEnabled())
                    log.debug("Route found : '" + isRouteFound + "' Route ID '" + lRouteId + "'");

                if (lRouteId != null)
                {
                    if (lRouteId.equalsIgnoreCase(RouteConstants.EXPIRED))
                        isExpired = true;
                    else
                        if (lRouteId.equalsIgnoreCase(RouteConstants.ECONOMY))
                        {
                            final String economyrouteid = mMessageRequest.getIntlEconomicRouteId();
                            mMessageRequest.setRouteId(economyrouteid);
                        }
                        else
                            if (lRouteId.equalsIgnoreCase(RouteConstants.STANDARD))
                                mMessageRequest.setRouteId(mMessageRequest.getIntlStandardRouteId());
                }
                else
                {
                    if (log.isDebugEnabled())
                        log.debug("Since route found based on the intl_route_config, setting the standard route as the Route id for this message. MessageId : '" + lBaseMessageId + "'");

                    mMessageRequest.setRouteId(mMessageRequest.getIntlStandardRouteId());
                }
            }
        }

        if (log.isDebugEnabled())
            log.debug("Route ID '" + mMessageRequest.getRouteId() + "', FoundRoute: '" + isRouteFound + "', isFailedListNumber: '" + isFailedListNumber + "', isExpired: '" + isExpired
                    + "', isInvalidIntlMobileLength : '" + isInvalidIntlMobileLength + "', isCCNotInRange : '" + isCCNotInRange + "'");

        splitAndSendMessage(isFailedListNumber, isRouteFound, isExpired, isInvalidIntlMobileLength, isCCNotInRange);
    }

    private void splitAndSendMessage(
            boolean aIsFailedListNumber,
            boolean aIsRouteFound,
            boolean aIsExpired,
            boolean aIsInvalidIntlMobileLength,
            boolean aIsCCNotInRange)

    {
        final String       lBaseMessageId = mMessageRequest.getBaseMessageId();

        PlatformStatusCode lErrorCode     = checkForCCRange(lBaseMessageId, aIsCCNotInRange, aIsFailedListNumber, aIsRouteFound);

        if (lErrorCode == null)
            lErrorCode = checkForInvalidIntlMNumber(lBaseMessageId, aIsInvalidIntlMobileLength, aIsFailedListNumber, aIsRouteFound);

        setRouteBasedOnRouteGroup(mMessageRequest);

        if (lErrorCode == null)
            lErrorCode = checkExpiry(lBaseMessageId, aIsExpired, aIsFailedListNumber, aIsRouteFound);

        if (lErrorCode == null)
            lErrorCode = checkForRouteConfig(lBaseMessageId, aIsFailedListNumber);

        if (lErrorCode == null)
            lErrorCode = checkForRouteNotFoundCase(lBaseMessageId, aIsRouteFound, aIsFailedListNumber);

        if (lErrorCode == null)
            lErrorCode = checkforAbosulteRouteFailList(lBaseMessageId, aIsFailedListNumber, aIsRouteFound);

        if (lErrorCode == null)
            lErrorCode = checkforAbosulteRouteIntlRoute(lBaseMessageId, aIsFailedListNumber, aIsRouteFound);

        if (lErrorCode == null)
            lErrorCode = checkHeader(lBaseMessageId, aIsFailedListNumber, aIsRouteFound);

        setCarrierSupportHeader(mMessageRequest);

        if (lErrorCode == null)
            setCarrierDTimeFormat(mMessageRequest);

        if (lErrorCode == null)
            RCProducer.sendToCarrierHandover(mMessageRequest);
        else
            sendToBillerQueue(lErrorCode, mMessageRequest);

        if (log.isDebugEnabled())
            log.debug("Sent to Route " + mMessageRequest.getRouteId() + " Success " + mMessageRequest);
    }

    private void setCarrierDTimeFormat(
            MessageRequest aMessageRequest)
    {
        final RouteConfigInfo lRouteConfigMap = RouteUtil.getRouteConfiguration(mMessageRequest.getRouteId());

        if (lRouteConfigMap != null)
        {
            if (lRouteConfigMap.getSmscid() != null)
                mMessageRequest.setSmscId(lRouteConfigMap.getSmscid());
            if (lRouteConfigMap.getDtimeFormat() != null)
                mMessageRequest.setCarrierDateTimeFormat(lRouteConfigMap.getDtimeFormat());
        }
    }

    private PlatformStatusCode checkHeader(
            String aMessageId,
            boolean aIsFailedListNumber,
            boolean aIsRouteFound)
    {
        if (log.isDebugEnabled())
            log.debug("Checking for checkforAbosulteRouteHeader. MID : " + aMessageId);

        final boolean isHeader = setHeader(mMessageRequest);

        if (log.isDebugEnabled())
            log.debug("Is Valid INTL Header [ '" + mMessageRequest.getHeader() + "'] ?" + isHeader);

        boolean isValidHeaderLength = true;

        if (isHeader)
        {
            final String lHeaderRegEx = CommonUtility.nullCheck(IntlRUtils.getIntlHeaderInfo(mMessageRequest.getCountry()), true);

            if (log.isDebugEnabled())
                log.debug("Header Regex Value : " + lHeaderRegEx);

            isValidHeaderLength = isValidHeaderLength(mMessageRequest, lHeaderRegEx);

            // isValidHeaderLength = isValidHeaderLength(mMessageRequest);

            if (log.isDebugEnabled())
                log.debug("Header Length Validation Status : " + isValidHeaderLength);
        }

        if (!isHeader || !isValidHeaderLength)
        {
            if (log.isDebugEnabled())
                log.debug("Valid header Length ::  " + isValidHeaderLength);

            if (aIsFailedListNumber)
            {
                setDummyRouteAndHeader(mMessageRequest, aIsRouteFound);

                final boolean isAbsoluteRoute = isAbsoluteRoute(mMessageRequest);

                if (!isAbsoluteRoute)
                    return PlatformStatusCode.INVALID_ROUTE_ID;
            }
            else
                return PlatformStatusCode.INVALID_HEADER;
        }
        return null;
    }

    private PlatformStatusCode checkforAbosulteRouteIntlRoute(
            String aMessageId,
            boolean aIsFailedListNumber,
            boolean aIsRouteFound)
    {
        if (log.isDebugEnabled())
            log.debug("Checking for checkforAbosulteRouteIntlRoute. MID : " + aMessageId);

        final boolean isIntlRoute = isINTLRoute(mMessageRequest);

        if (!isIntlRoute)
        {
            if (log.isDebugEnabled())
                log.debug("Is Intl Route : " + isIntlRoute);

            if (aIsFailedListNumber)
            {
                setDummyRouteAndHeader(mMessageRequest, aIsRouteFound);
                final boolean isAbsoluteRoute = isAbsoluteRoute(mMessageRequest);
                if (!isAbsoluteRoute)
                    return PlatformStatusCode.INVALID_ROUTE_ID;
            }
            else
                return PlatformStatusCode.INTL_INVALID_ROUTE;
        }
        return null;
    }

    private PlatformStatusCode checkforAbosulteRouteFailList(
            String aMessageId,
            boolean aIsFailedListNumber,
            boolean aIsRouteFound)
    {
        if (log.isDebugEnabled())
            log.debug("Checking for checkforAbosulteRouteFailList. MID : " + aMessageId);

        boolean isAbsoluteRoute = isAbsoluteRoute(mMessageRequest);

        if (!isAbsoluteRoute)
        {
            if (log.isDebugEnabled())
                log.debug("Absolute Route Status : " + isAbsoluteRoute);

            if (aIsFailedListNumber)
            {
                setDummyRouteAndHeader(mMessageRequest, aIsRouteFound);

                isAbsoluteRoute = isAbsoluteRoute(mMessageRequest);
                if (!isAbsoluteRoute)
                    return PlatformStatusCode.INVALID_ROUTE_ID;
            }
            else
                return PlatformStatusCode.INVALID_ROUTE_ID;
        }
        return null;
    }

    private PlatformStatusCode checkForInvalidIntlMNumber(
            String aMessageId,
            boolean aIsInvalidIntlMobileLength,
            boolean aIsFailedListNumber,
            boolean aIsRouteFound)
    {
        if (log.isDebugEnabled())
            log.debug("Checking for Invalid Intl M Number. MessageId : " + aMessageId);

        if (aIsInvalidIntlMobileLength)
        {
            if (log.isDebugEnabled())
                log.debug("Invalid Mobile Length ...." + aIsInvalidIntlMobileLength);

            if (aIsFailedListNumber)
                setDummyRouteAndHeader(mMessageRequest, aIsRouteFound);
            else
                return PlatformStatusCode.INTL_INVALID_MOBILE_LENGTH;
        }
        return null;
    }

    private PlatformStatusCode checkForCCRange(
            String aMessageId,
            boolean aIsCCNotInRange,
            boolean aIsFailedListNumber,
            boolean aIsRouteFound)
    {
        if (log.isDebugEnabled())
            log.debug("Checking for CC Not in Range. MessageId : " + aMessageId);

        if (aIsCCNotInRange)
        {
            if (log.isDebugEnabled())
                log.debug("CC Not in Range : " + aIsCCNotInRange);

            if (aIsFailedListNumber)
                setDummyRouteAndHeader(mMessageRequest, aIsRouteFound);
            else
                return PlatformStatusCode.INTL_COUNTRY_CODE_RANGE_NOT_AVAILABLE;
        }
        return null;
    }

    private PlatformStatusCode checkForRouteNotFoundCase(
            String aMessageId,
            boolean aIsRouteFound,
            boolean aIsFailedListNumber)
    {
        if (log.isDebugEnabled())
            log.debug("Checking for Record Not Found. MessageId : " + aMessageId);

        if (!aIsRouteFound)
        {
            if (log.isDebugEnabled())
                log.debug("Route Not found  MessageId : " + aIsRouteFound);

            if (aIsFailedListNumber)
                setDummyRouteAndHeader(mMessageRequest, false);
            else
                return PlatformStatusCode.INTL_ROUTE_EXPIRED;
        }
        return null;
    }

    private PlatformStatusCode checkForRouteConfig(
            String aMessageId,
            boolean aIsFailedListNumber)
    {
        if (log.isDebugEnabled())
            log.debug("Setting Route Type. MessageId : " + aMessageId);

        final RouteConfigInfo lRouteConfigMap = RouteUtil.getRouteConfiguration(mMessageRequest.getRouteId());

        if (lRouteConfigMap != null)
        {
            if (log.isDebugEnabled())
                log.debug("Found Route Config. MessageId : " + aMessageId);

            mMessageRequest.setRouteType(lRouteConfigMap.getRouteType());
        }
        else
        {
            log.error("When the route config is not found then treat it as Route Not found case and send to Biller");
            if (aIsFailedListNumber)
                setDummyRouteAndHeader(mMessageRequest, false);
            else
                return PlatformStatusCode.INVALID_ROUTE_ID;
        }
        return null;
    }

    private PlatformStatusCode checkExpiry(
            String aMessageId,
            boolean aIsExpired,
            boolean aIsFailedListNumber,
            boolean aIsRouteFound)
    {
        if (log.isDebugEnabled())
            log.debug("Check for expired message");

        if (aIsExpired)
        {
            if (log.isDebugEnabled())
                log.debug("Expired message. MessageId : " + aMessageId);

            if (aIsFailedListNumber)
                setDummyRouteAndHeader(mMessageRequest, aIsRouteFound);
            else
                return PlatformStatusCode.INTL_ROUTE_EXPIRED;
        }
        return null;
    }

    private static void setRouteBasedOnRouteGroup(
            MessageRequest aMessageRequest)
    {
        if (log.isDebugEnabled())
            log.debug("Setting route based on route group.");

        final String lGroupBasedRouteId = IntlRUtils.getRouteBasedOnRouteGroupID(aMessageRequest);

        if (lGroupBasedRouteId != null)
            aMessageRequest.setRouteId(lGroupBasedRouteId);
    }

    private static void setDummyRouteAndHeader(
            MessageRequest aMessageRequest,
            boolean aHeader)
    {
        setDummyRoute(aMessageRequest);
        // this will set header if routeid already found - so that it will be shown in
        // client in reporting
        if (aHeader)
            setHeader(aMessageRequest);
    }

    private static void setDummyRoute(
            MessageRequest aMessageRequest)
    {
        String     lDummyRouteid = null;
        RouteLogic lLogicId      = null;

        try
        {
            String lIdentifier = aMessageRequest.getIntlClientFaillistCheck();

            if (lIdentifier == null)
            {
                lIdentifier = aMessageRequest.getIntlGlobalFaillistCheck();

                if (lIdentifier != null)
                {
                    lDummyRouteid = RCUtil.getAppConfigValueAsString(ConfigParamConstants.INTL_GLOBAL_FAILIST_ROUTE_ID);
                    lLogicId      = RouteLogic.INTL_CLIENT_FAILLIST_LOGICID;
                }
            }
            else
            {
                lDummyRouteid = RCUtil.getAppConfigValueAsString(ConfigParamConstants.INTL_CLIENT_FAILLIST_ROUTE_ID);
                lLogicId      = RouteLogic.INTL_GLOBAL_FAILLIST_LOGICID;
            }

            if (log.isDebugEnabled())
                log.debug("DummyRouteid Details : '" + lDummyRouteid + "'");

            if (lDummyRouteid != null)
            {
                final RouteConfigInfo lRouteConfigInfo = RouteUtil.getRouteConfiguration(lDummyRouteid);

                if (log.isDebugEnabled())
                    log.debug("Route Config Info for Dummy Route :'" + lDummyRouteid + "' is : '" + lRouteConfigInfo + "'" + " logicId:" + lLogicId);

                aMessageRequest.setRouteId(lDummyRouteid);
                if ((lRouteConfigInfo != null) && (lRouteConfigInfo.getRouteType() != null))
                    aMessageRequest.setRouteType(lRouteConfigInfo.getRouteType());
            }
            else
                log.error("Dummy Routeid is not configured in app_configuration for the key");

            if (lLogicId != null)
                aMessageRequest.setRouteLogicId(CommonUtility.getInteger(lLogicId.getKey()));
        }
        catch (final Exception e)
        {
            log.error("Excception while setting dummyRouteid for the Messaage : '" + aMessageRequest + "'");
        }
    }

    private static boolean setHeader(
            MessageRequest aMessageRequest)
    {
        final String lClientHeader = CommonUtility.nullCheck(aMessageRequest.getIntlClientHeader(), true);

        if (!lClientHeader.isEmpty())
        {
            MessageUtil.setHeaderId(aMessageRequest, lClientHeader);
            return true;
        }

        final String              key             = CommonUtility.combine(aMessageRequest.getCountry(), aMessageRequest.getRouteId());
        final Map<String, String> lIntlHeaderInfo = IntlRUtils.getIntlRouteHeaderInfo(key);

        if (log.isDebugEnabled())
            log.debug("For the Country '" + aMessageRequest.getCountry() + "' and Route ID '" + aMessageRequest.getRouteId() + "' the Header logic is : '" + lIntlHeaderInfo + "'");

        HeaderType    lHeaderType    = HeaderType.DYNAMIC;
        HeaderSubType lHeaderSubType = HeaderSubType.NA;
        String        lStaticHeader  = "";

        if ((lIntlHeaderInfo != null) && (!lIntlHeaderInfo.isEmpty()))
        {
            lHeaderType    = HeaderType.getHeaderType(lIntlHeaderInfo.get("header_type"));
            lHeaderSubType = HeaderSubType.getHeaderSubType(lIntlHeaderInfo.get("header_sub_type"));
            lStaticHeader  = lIntlHeaderInfo.get("default_header");
        }
        else
        {
            final HeaderType    lDefaultHeaderType    = HeaderType.getHeaderType(aMessageRequest.getIntlDefaultHeaderType());
            final HeaderSubType lDefaultHeaderSubType = HeaderSubType.getHeaderSubType(aMessageRequest.getIntlHeaderSubType());
            final String        lDefaultHeader        = aMessageRequest.getIntlDefaultHeader();

            if (log.isDebugEnabled())
                log.debug("DefaultSenderidType : '" + lDefaultHeaderType + "', DefaultHeader : '" + lDefaultHeader + "'");

            if (lDefaultHeaderType != null)
                lHeaderType = lDefaultHeaderType;

            if (lDefaultHeaderSubType != null)
                lHeaderSubType = lDefaultHeaderSubType;

            if (lDefaultHeader != null)
                lStaticHeader = lDefaultHeader;
        }

        if (log.isDebugEnabled())
            log.debug("After setting HeaderType : '" + lHeaderType + "' - staticHeader '" + lStaticHeader + "'");

        if (HeaderType.STATIC == lHeaderType)
        {

            if (!CommonUtility.nullCheck(lStaticHeader, true).isEmpty())
            {
                MessageUtil.setHeaderId(aMessageRequest, lStaticHeader);
                return true;
            }
            return false;
        }

        final String lHeader = CommonUtility.nullCheck(MessageUtil.getHeaderId(aMessageRequest), true);

        // Validate List Header
        if (HeaderType.LIST == lHeaderType)
        {
            boolean lHeaderStatus = false;

            if (RCUtil.isValidUserHeader(aMessageRequest.getClientId(), lHeader))
            {

                switch (lHeaderSubType)
                {
                    case NUMARIC:
                        lHeaderStatus = validateNumaric(aMessageRequest, lHeader);
                        break;

                    case ALPHABET:
                        lHeaderStatus = validateAlphabet(aMessageRequest, lHeader);
                        break;

                    case ALPHANUMARIC:
                        lHeaderStatus = validateAlphaNumaric(aMessageRequest, lHeader);
                        break;

                    case NA:
                    case ALPHANUMARIC_SPECIAL_CHAR:
                        lHeaderStatus = true;
                        break;

                    default:
                        lHeaderStatus = false;
                }

                return lHeaderStatus;
            }
            return lHeaderStatus;
        }

        // Validate Dynamic Header
        if (HeaderType.DYNAMIC == lHeaderType)
        {
            boolean lHeaderStatus = false;

            switch (lHeaderSubType)
            {
                case NUMARIC:
                    lHeaderStatus = validateNumaric(aMessageRequest, lHeader);
                    break;

                case ALPHABET:
                    lHeaderStatus = validateAlphabet(aMessageRequest, lHeader);
                    break;

                case ALPHANUMARIC:
                    lHeaderStatus = validateAlphaNumaric(aMessageRequest, lHeader);
                    break;

                case NA:
                case ALPHANUMARIC_SPECIAL_CHAR:
                    lHeaderStatus = true;
                    break;

                default:
                    lHeaderStatus = false;
            }
            return lHeaderStatus;
        }

        /*
         * final String lHeader =
         * CommonUtility.nullCheck(getVaidateUserHeader(aMessageRequest), true);
         * if (RouteConstants.NUMERIC_HEADER.equalsIgnoreCase(lHeaderType))
         * {
         * if (StringUtils.isNumeric(lHeader))
         * {
         * MessageUtil.setHeaderId(aMessageRequest, lHeader);
         * return true;
         * }
         * return false;
         * }
         */

        if (lHeader.isEmpty())
            return false;

        /*
         * MessageUtil.setHeaderId(aMessageRequest, lHeader);
         */
        return true;
    }

    private static boolean validateAlphaNumaric(
            MessageRequest aMessageRequest,
            String aHeader)
    {

        if (StringUtils.isAlphanumeric(aHeader))
        {
            MessageUtil.setHeaderId(aMessageRequest, aHeader);
            return true;
        }
        return false;
    }

    private static boolean validateAlphabet(
            MessageRequest aMessageRequest,
            String aHeader)
    {

        if (StringUtils.isAlpha(aHeader))
        {
            MessageUtil.setHeaderId(aMessageRequest, aHeader);
            return true;
        }
        return false;
    }

    private static boolean validateNumaric(
            MessageRequest aMessageRequest,
            String aHeader)
    {

        if (StringUtils.isNumeric(aHeader))
        {
            MessageUtil.setHeaderId(aMessageRequest, aHeader);
            return true;
        }
        return false;
    }

    private static boolean isINTLRoute(
            MessageRequest aMessageRequest)
    {
        return RouteUtil.getRouteConfiguration(aMessageRequest.getRouteId()).isIntlRoute();
    }

    private static boolean isAbsoluteRoute(
            MessageRequest aMessageRequest)
    {
        return RouteUtil.isRouteAvailable(aMessageRequest.getRouteId());
    }

    private static boolean isValidHeaderLength(
            MessageRequest aMessageRequest,
            String aRegEx)
    {
        final String lHeader = MessageUtil.getHeaderId(aMessageRequest);

        return PatternCache.getInstance().isPatternMatch(PatternCheckCategory.HEADER_CHECK, aRegEx, lHeader);
    }

    /*
     * private static boolean isValidHeaderLength(
     * MessageRequest aMessageRequest)
     * {
     * final String lHeader = MessageUtil.getHeaderId(aMessageRequest);
     * if (log.isDebugEnabled())
     * log.debug("INTL Header min length :'" + RCUtil.INTL_HEADER_MIN_LENGTH +
     * "' max length : '" + RCUtil.INTL_HEADER_MAX_LENGTH + "'");
     * return ((lHeader.length() > RCUtil.INTL_HEADER_MIN_LENGTH) &&
     * (lHeader.length() < RCUtil.INTL_HEADER_MAX_LENGTH));
     * }
     */
    private static void sendToBillerQueue(
            PlatformStatusCode aPlatformStatusCode,
            MessageRequest aMessageRequest)
    {
        aMessageRequest.setSubOriginalStatusCode(aPlatformStatusCode.getStatusCode());
        if (log.isDebugEnabled())
            log.debug("sendToPlatformRejection" + aMessageRequest);
        RCProducer.sendToPlatformRejection(aMessageRequest);
    }

    private static void setCarrierSupportHeader(
            MessageRequest aMessageRequest)
    {
        final String lKey = CommonUtility.combine(CommonUtility.nullCheck(aMessageRequest.getRouteId(), true).toUpperCase(), MessageUtil.getHeaderId(aMessageRequest).toLowerCase());

        if (IntlRUtils.getCarrierSupportHeaders().containsKey(lKey))
        {
            MessageUtil.setHeaderId(aMessageRequest, IntlRUtils.getCarrierSupportHeaders().get(lKey));
            if (log.isDebugEnabled())
                log.debug("CarrierSupportHeader found for the key:" + lKey + " swapped carrier Header:" + MessageUtil.getHeaderId(aMessageRequest));
        }
    }

    private static String getVaidateUserHeader(
            MessageRequest aMessageRequest)
    {
        if (log.isDebugEnabled())
            log.debug("getVaidateUserHeader() Message Id" + aMessageRequest.getBaseMessageId());

        final boolean isHeaderChk = aMessageRequest.isDefailtHeaderEnable();
        final String  lAccHeader  = CommonUtility.nullCheck(aMessageRequest.getClientDefaultHeader(), true);
        final String  lHeader     = CommonUtility.nullCheck(MessageUtil.getHeaderId(aMessageRequest), true);
        final String  lClientId   = aMessageRequest.getClientId();

        if (!lHeader.isEmpty())
            return lHeader;

        if (isHeaderChk && !lHeader.isEmpty() && RCUtil.isValidUserHeader(lClientId, lHeader))
        {
            if (log.isDebugEnabled())
                log.debug("getVaidateUserHeader Success");
            return lHeader;
        }

        if (log.isDebugEnabled())
        {
            log.debug("getVaidateUserHeader false");
            log.debug("Default Header : " + lAccHeader);
        }
        if (lAccHeader.isEmpty())
            return null;

        return lAccHeader;
    }

}
