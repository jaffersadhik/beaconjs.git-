package com.itextos.beacon.platform.dnpcore.process;

import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.itextos.beacon.commonlib.constants.Component;
import com.itextos.beacon.commonlib.constants.ConfigParamConstants;
import com.itextos.beacon.commonlib.constants.MessageType;
import com.itextos.beacon.commonlib.message.DeliveryObject;
import com.itextos.beacon.commonlib.utility.CommonUtility;
import com.itextos.beacon.inmemory.customfeatures.pojo.DlrTypeInfo;
import com.itextos.beacon.platform.billing.DeliveryProcess;
import com.itextos.beacon.platform.dnpcore.dlrclienthandover.process.DlrClientHandover;
import com.itextos.beacon.platform.dnpcore.util.DNPUtil;
import com.itextos.beacon.platform.dnpcore.util.DlrConstants;

public class DlrRequestProcess
{

    private static final Log log = LogFactory.getLog(DlrRequestProcess.class);

    private DlrRequestProcess()
    {}

    public static void processDNQueueReq(
            DeliveryObject aDeliveryObject,
            Map<Component, DeliveryObject> nextComponentMap)
            throws Exception
    {
        if (log.isDebugEnabled())
            log.debug(" Begin Message Id:" + aDeliveryObject.getMessageId());
        final DeliveryObject lNewDeliveryObj = aDeliveryObject.getClonedDeliveryObject();
        final String         lMid            = lNewDeliveryObj.getMessageId();

        /*
         * String lDndDeliveryStatus =
         * PlatformUtil.getAppConfigValueAsString(ConfigParamConstants.DELV_DND_STATUS);
         * String lDndDeliveryRouteId =
         * PlatformUtil.getAppConfigValueAsString(ConfigParamConstants.DELV_DND_ROUTE_ID
         * );
         * lDndDeliveryStatus = lDndDeliveryStatus == null ? "" : lDndDeliveryStatus;
         * lDndDeliveryRouteId = lDndDeliveryRouteId == null ? "" : lDndDeliveryRouteId;
         * final String lStatusId =
         * lNewNunMessage.getValue(MiddlewareConstant.MW_DN_ORI_STATUS_CODE);
         * if ((lNewNunMessage.getValue(MiddlewareConstant.MW_PLATFROM_REJECTED) ==
         * null) || ((lNewNunMessage.getValue(MiddlewareConstant.MW_PLATFROM_REJECTED)
         * != null)
         * && lDndDeliveryStatus.equals(lStatusId) &&
         * lDndDeliveryRouteId.equals(lNewNunMessage.getValue(MiddlewareConstant.
         * MW_ROUTE_ID))))
         * {
         * if (log.isDebugEnabled())
         * log.debug(" Adjustments begin mid:" + lMid + " status_id:" + lStatusId);
         * processDeliveryAdjustments(lNewNunMessage);
         * if (log.isDebugEnabled())
         * log.debug(" Adjustments end mid:" + lMid);
         * }
         */
        if (log.isDebugEnabled())
            log.debug(aDeliveryObject.getMessageId() + " : Before DlrDataUpdater the Message Object : " + lNewDeliveryObj.getJsonString());

        // The following logic will be in biller earlier and moved here

        processDeliveryAdjustments(lNewDeliveryObj);

        if (log.isDebugEnabled())
            log.debug(aDeliveryObject.getMessageId() + " : After DlrDataUpdater the Message Object : " + lNewDeliveryObj.getJsonString());

     
        
        final boolean lNoPayloadForPromoMsg = CommonUtility.isEnabled(DNPUtil.getAppConfigValueAsString(ConfigParamConstants.NOPAYLOAD_FOR_PROMO_MSG));

        //        if (lNoPayloadForPromoMsg && (lNewDeliveryObj.getMessageType() != MessageType.PROMOTIONAL)) {

        if (lNoPayloadForPromoMsg) {
        		
        		if (log.isDebugEnabled())
                   log.debug(aDeliveryObject.getMessageId() + " : nextComponentMap set T2DB_DELIVERIES to  : "+Component.T2DB_DELIVERIES );
                		   ;

        	nextComponentMap.put(Component.T2DB_DELIVERIES, lNewDeliveryObj);
        }else {
        	
        	if (log.isDebugEnabled())
                log.debug(aDeliveryObject.getMessageId() + " : nextComponentMap not to set T2DB_DELIVERIES : lNoPayloadForPromoMsg "+lNoPayloadForPromoMsg+" : MessageType :  "+lNewDeliveryObj.getMessageType() );
             
        }

        final DlrTypeInfo lDlrTypeInfo = DNPUtil.getDnTypeInfo(lNewDeliveryObj.getClientId());

        if (log.isDebugEnabled())
            log.debug(aDeliveryObject.getMessageId() +" : DlrType Confiug : " + lDlrTypeInfo);

        int lTotalMsgParts = 0;

        try
        {
            lTotalMsgParts = lNewDeliveryObj.getMessageTotalParts();
        }
        catch (final Exception e)
        {}

        // Interface Rejections & it is Sync request error case will not handover to
        // client.

        boolean isProcessClientHandover = !aDeliveryObject.isSyncRequest();

        if ((aDeliveryObject.getDlrFromInternal() != null) && aDeliveryObject.getDlrFromInternal().equals("dummyroute_dlr_came_from_MW"))
            isProcessClientHandover = true;

        if (isProcessClientHandover || aDeliveryObject.isPlatfromRejected())
        {
            if (log.isDebugEnabled())
                log.debug(aDeliveryObject.getMessageId() +" : Client Handover allows only Carrier Success / Platform Rejections.. ");

            final boolean isRejectedRequest = aDeliveryObject.isPlatfromRejected() || aDeliveryObject.isInterfaceRejected();

            if (log.isDebugEnabled())
                log.debug(aDeliveryObject.getMessageId() + " : Is Rejected Dlr ? '" + isRejectedRequest + "', If TRUE then bypass the Single DN..");

            if ((lDlrTypeInfo != null) && DlrConstants.SINGLE_DN_ENABLE.equals(lDlrTypeInfo.getDnType()) && (lTotalMsgParts > 1) && !isRejectedRequest)
                nextComponentMap.put(Component.SDNP, lNewDeliveryObj);
            else
            {
                if (log.isDebugEnabled())
                    log.debug(aDeliveryObject.getMessageId() + " : Sending request to Client Handover Process..");
                DlrClientHandover.processClientHandover(lNewDeliveryObj, nextComponentMap);
            }
        }
        if (log.isDebugEnabled())
            log.debug(aDeliveryObject.getMessageId() + " End mid:" + lMid + " nextQueueMap ket set:" + (nextComponentMap != null ? nextComponentMap.keySet() : nextComponentMap));
    }

    private static void processDeliveryAdjustments(
            DeliveryObject aDeliveryObject)
    {
        // TimeAdjustmentUtility.maskFailToSuccessCode(aNewNunMessage);
        // TimeAdjustmentUtility.adjustAndSetDTime(aNewNunMessage);

        final DeliveryProcess lDeliveryProcess = new DeliveryProcess(aDeliveryObject);
        lDeliveryProcess.process();
    }

}
