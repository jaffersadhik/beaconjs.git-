package com.itextos.beacon.platform.dnpcore.dlrclienthandover;

import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.itextos.beacon.commonlib.constants.Component;
import com.itextos.beacon.commonlib.constants.DlrHandoverMode;
import com.itextos.beacon.commonlib.message.DeliveryObject;
import com.itextos.beacon.commonlib.message.utility.MessageUtil;
import com.itextos.beacon.commonlib.utility.CommonUtility;
import com.itextos.beacon.commonlib.utility.ItextosClient;
import com.itextos.beacon.inmemory.clidlrpref.ClientDlrAdminDelivery;
import com.itextos.beacon.inmemory.clidlrpref.ClientDlrConfig;
import com.itextos.beacon.inmemory.clidlrpref.ClientDlrConfigUtil;

public class DlrClientHandover
{

    private static final Log log = LogFactory.getLog(DlrClientHandover.class);

    private DlrClientHandover()
    {}

    public static void processClientHandover(
            DeliveryObject aDeliveryObject,
            Map<Component, DeliveryObject> lNextComponent)
    {
        if (log.isDebugEnabled())
            log.debug("Message Received to Client Handover : " + aDeliveryObject);

        try
        {
            generateClientReq(aDeliveryObject, lNextComponent);

            if (log.isDebugEnabled())
                log.debug("Next Queue Info : " + lNextComponent);
        }
        catch (final Exception e)
        {
            log.error("Exception occer while processing the Client Handover Request : ", e);

            e.printStackTrace();
        }
    }

    private static void generateClientReq(
            DeliveryObject aDeliveryObject,
            Map<Component, DeliveryObject> aNextComponentMap)
            throws Exception
    {

        try
        {
            if (aDeliveryObject == null)
                return;
            if (log.isDebugEnabled())
                log.debug(aDeliveryObject.getMessageId()+" : Start Processing Message : " + aDeliveryObject);

            // The following has been implemented in biller while inserting into
            // submissions, same logic written here to have same for dlr's
            // middleare rejections set as 0
            /*
             * if (aNunMessage.getValue(MiddlewareConstant.MW_MSG_TOTAL_PARTS) == null)
             * {
             * aNunMessage.putValue(MiddlewareConstant.MW_MSG_TOTAL_PARTS, "0");
             * aNunMessage.putValue(MiddlewareConstant.MW_MSG_PART_NUMBER, "0");
             * }
             * else
             * // single part msg - set as 1 for dlr handover's, checking
             * // BYPASS_FINAL_DN here because the record might come back again
             * // from final dn processor with SPLIT_MSG_PARTS as "0"
             * if ((aNunMessage.getValue(MiddlewareConstant.MW_BYPASS_PROCESS_FINAL_DN) ==
             * null) &&
             * "0".equals(aNunMessage.getValue(MiddlewareConstant.MW_MSG_TOTAL_PARTS)))
             * {
             * aNunMessage.putValue(MiddlewareConstant.MW_MSG_TOTAL_PARTS, "1");
             * aNunMessage.putValue(MiddlewareConstant.MW_MSG_PART_NUMBER, "1");
             * }
             */
            final ClientDlrConfig lClientDlrConfig = ClientDlrConfigUtil.getDlrHandoverConfig(aDeliveryObject.getClientId(), "sms", aDeliveryObject.getInterfaceType(),
                    aDeliveryObject.isDlrRequestFromClient());

            if (lClientDlrConfig == null)
                return;

            if (log.isDebugEnabled())
                log.debug(aDeliveryObject.getMessageId()+" : lient Dlr Preferences :: " + lClientDlrConfig.toString());

            DeliveryObject lNewDeliveryObject = null;
            /*
             * final DlrErrorDescType lDlrErrorDescType =
             * lClientMedisPrefs.getDlrErrorDescType();
             * final String lPlatform = lDlrErrorDescType.getKey(); // GEN/CST
             * aNunMessage.putValue(MiddlewareConstant.MW_PLATFORM, lPlatform);
             * if (log.isDebugEnabled())
             * log.debug(" status_id::" +
             * aNunMessage.getValue(MiddlewareConstant.MW_STATUS_ID));
             * Object lTempErrorCodes =
             * DlrErrorUtil.getErrorCodeInfo(aNunMessage.getValue(MiddlewareConstant.
             * MW_STATUS_ID));
             * final HandoverErrorCode lHandoverErrorCode = new HandoverErrorCode();
             * if (lTempErrorCodes != null)
             * {
             * final ErrorCodeInfo lErrorCodeInfo = (ErrorCodeInfo) lTempErrorCodes;
             * lHandoverErrorCode.setOriginalErrorCode(lErrorCodeInfo.getErrorCode());
             * lHandoverErrorCode.setErrorCode(lErrorCodeInfo.getErrorCode());
             * lHandoverErrorCode.setErrorDesc(lErrorCodeInfo.getDisplayError());
             * lHandoverErrorCode.setErrorCategory(lErrorCodeInfo.getErrorCategory());
             * lHandoverErrorCode.setStatusFlag(lErrorCodeInfo.getStatusFlag());
             * lTempErrorCodes = null;
             * }
             * if (log.isDebugEnabled())
             * log.debug(" Final Error Code::" + lHandoverErrorCode.toString());
             * boolean lDlrSendAllowed = false;
             * BaseMessage lNewNunMessage = null;
             * if (lHandoverErrorCode.getErrorCategory() != null)
             * switch (lHandoverErrorCode.getErrorCategory())
             * {
             * case CARRIER:
             * if ((lClientMedisPrefs.isDlrSuccess() &&
             * lHandoverErrorCode.getErrorCode().contentEquals(PlatformStatusCode.
             * DEFAULT_CARRIER_STATUS_ID.getStatusCode()))
             * || lClientMedisPrefs.isDlrOperationFailure())
             * lDlrSendAllowed = true;
             * else
             * lDlrSendAllowed = false;
             * break;
             * case PLATFORM:
             * if (lClientMedisPrefs.isDlrPlatformFailReject())
             * lDlrSendAllowed = true;
             * else
             * lDlrSendAllowed = false;
             * break;
             * case INTERIM:
             * lDlrSendAllowed = false;
             * break;
             * default:
             * break;
             * }
             * if (!lDlrSendAllowed)
             * return;
             * switch (lClientMedisPrefs.getDlrErrorDescType())
             * {
             * case GENERIC:
             * final String modifiedErrorkey =
             * lClientMedisPrefs.getDlrErrorDescType().getKey() + ":" +
             * aNunMessage.getValue(MiddlewareConstant.MW_STATUS_ID);
             * lTempErrorCodes = DlrErrorUtil.getModifyErrorCodeInfo(modifiedErrorkey);
             * if (lTempErrorCodes != null)
             * {
             * final ModifiedErrorCodeInfo meci = (ModifiedErrorCodeInfo) lTempErrorCodes;
             * lHandoverErrorCode.setOriginalErrorCode(meci.getErrorCode());
             * lHandoverErrorCode.setErrorCode(meci.getModifiedErrorCode());
             * lHandoverErrorCode.setErrorDesc(meci.getModifiedErrorDesc());
             * lHandoverErrorCode.setStatusFlag(meci.getStatusFlag());
             * }
             * break;
             * case CUSTOM:
             * final String lClientId =
             * aNunMessage.getValue(MiddlewareConstant.MW_CLIENT_ID);
             * final String lStatusId =
             * aNunMessage.getValue(MiddlewareConstant.MW_STATUS_ID);
             * final ItextosClient lClient = new ItextosClient(lClientId);
             * String customErrorKey = lClient.getClientId() + ":" + lStatusId;
             * lTempErrorCodes = DlrErrorUtil.getClientErrorCodeInfo(customErrorKey);
             * if (lTempErrorCodes == null)
             * {
             * customErrorKey = lClient.getAdmin() + ":" + lStatusId;
             * lTempErrorCodes = DlrErrorUtil.getClientErrorCodeInfo(customErrorKey);
             * if (lTempErrorCodes == null)
             * customErrorKey = lClient.getSuperAdmin() + ":" + lStatusId;
             * lTempErrorCodes = DlrErrorUtil.getClientErrorCodeInfo(customErrorKey);
             * }
             * if (lTempErrorCodes != null)
             * {
             * final ClientErrorCodeInfo ceci = (ClientErrorCodeInfo) lTempErrorCodes;
             * lHandoverErrorCode.setOriginalErrorCode(ceci.getErrorCode());
             * lHandoverErrorCode.setErrorCode(ceci.getClientErrorCode());
             * lHandoverErrorCode.setErrorDesc(ceci.getClientErrorDesc());
             * lHandoverErrorCode.setStatusFlag(ceci.getStatusFlag());
             * }
             * break;
             * default:
             * break;
             * }
             */

            String         lHeader            = MessageUtil.getHeaderId(aDeliveryObject);
            final String   lMaskedHeader      = CommonUtility.nullCheck(aDeliveryObject.getMaskedHeader(), true);

            if (!lMaskedHeader.isEmpty())
                // header will be set in routing when header masked
                lHeader = aDeliveryObject.getClientHeader();

            MessageUtil.setHeaderId(aDeliveryObject, lHeader);

            final ClientDlrAdminDelivery lDlrToSu = lClientDlrConfig.getDlrToSu();
            lNewDeliveryObject = aDeliveryObject.getClonedDeliveryObject();

            switch (lDlrToSu)
            {
                case ADMIN_USER:
                    setParentUser(lNewDeliveryObject, ClientDlrAdminDelivery.ADMIN_USER);
                    break;

                case SUPER_USER:
                    setParentUser(lNewDeliveryObject, ClientDlrAdminDelivery.SUPER_USER);
                    break;

                case NONE:
                default:
                    break;
            }

            final DlrHandoverMode lDlrHandoverMode = lClientDlrConfig.getDlrHandoverMode();

            if (log.isDebugEnabled())
                log.debug(aDeliveryObject.getMessageId()+" : Dlr Handover Mode : " + lDlrHandoverMode);

            switch (lDlrHandoverMode)
            {
                case API:
                    if (log.isDebugEnabled())
                        log.debug(" Processing HTTP_DLR  for Message Id " + aDeliveryObject.getMessageId());

                    aNextComponentMap.put(Component.HTTP_DLR, lNewDeliveryObject);
                    break;

                case SMPP:
                    final DeliveryObject lSmppDLRObj = DlrSmppGenerator.generateDlrQueue(lNewDeliveryObject);

                    if (log.isDebugEnabled())
                        log.debug("Process SMPP_DLR  for Message Id : '" + aDeliveryObject.getMessageId() + "' and  Message : " + lSmppDLRObj.toString());

                    aNextComponentMap.put(Component.SMPP_DLR, lSmppDLRObj);
                    break;

                case FTP:
                case NODLR:
                default:
                    break;
            }

            if (lClientDlrConfig.isDlrQueryEnabled())
            {
                if (log.isDebugEnabled())
                    log.debug(" Processing DLR_QUERY_DN_TOPIC for Message Id " + aDeliveryObject.getMessageId());
                aNextComponentMap.put(Component.DLRQDN, aDeliveryObject);
            }
        }
        catch (final Exception e1)
        {
            log.error(" Exception while process message:", e1);
            e1.printStackTrace();
            throw e1;
        }
    }

    private static void setParentUser(
            DeliveryObject aDeliveryObject,
            ClientDlrAdminDelivery flag)
    {

        try
        {
            final String        lCliemtId = aDeliveryObject.getClientId();
            final ItextosClient lClient   = new ItextosClient(lCliemtId);

            if (flag == ClientDlrAdminDelivery.SUPER_USER)
                aDeliveryObject.setClientId(lClient.getSuperAdmin() + "00000000");
            else
                aDeliveryObject.setClientId(lClient.getAdmin() + "0000");

            log.info("changeClientAsRequired is ::" + aDeliveryObject.getClientId());
        }
        catch (final Exception e)
        {
            e.printStackTrace();
            throw e;
        }
    }

}
