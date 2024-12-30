package com.itextos.beacon.smpp.dto;

import java.math.BigInteger;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.cloudhopper.smpp.pdu.SubmitSm;
import com.cloudhopper.smpp.pdu.SubmitSmResp;
import com.itextos.beacon.commonlib.constants.FeatureCode;
import com.itextos.beacon.commonlib.constants.InterfaceStatusCode;
import com.itextos.beacon.commonlib.constants.MessageClass;
import com.itextos.beacon.commonlib.utility.CommonUtility;
import com.itextos.beacon.smpp.featurecode.PlatformFeatureCode;
import com.itextos.beacon.smpp.messagevalidityperiod.MessageValidityPeriod;
import com.itextos.beacon.smpp.schedule.ScheduleValidate;
import com.itextos.beacon.smpp.smsdecode.SMSBodyContent;
import com.itextos.beacon.smpp.smsdecode.SMSDecode;
import com.itextos.beacon.smpp.tlv.TLVValidate;
import com.itextos.beacon.smpp.utils.ItextosSmppConstants;
import com.itextos.beacon.smpp.utils.ItextosSmppUtil;

public class SubmitSmRequest
        extends
        AbstractSmppRequestObject
{

    private static final Log log                  = LogFactory.getLog(SubmitSmRequest.class);

    private String           mSystemId            = null;
    private String           mMobileNumber        = null;
    private String           mMessage             = null;
    private String           mHeader              = null;
    private String           mScheduleTime        = null;
    private String           mUdh                 = null;
    private String           mDlrReqFromClient    = null;
    private String           mMessageExpiry       = null;
    private long             mReceivedTime        = 0;
    private MessageClass     mMsgclass            = null;
    private String           mUdhi                = null;
    private String           mClientIp            = null;
    private String           mDltEntityId         = null;
    private String           mDltTemplateId       = null;
    private String           mDltTelemarketerId       = null;

    private boolean          mIsHexMsg            = false;
    private int              mDestPort            = 0;
    private SessionDetail    mSessionDetail       = null;
    private SubmitSm         mSubmitSm            = null;
    private String           mEsmClass            = null;
    private String           mClientId            = null;
    private String           mUdhRefNum           = null;
    private int              mTotalMsgParts       = 0;
    private int              mPartNumber          = 0;
    private String           mServiceType         = null;
    private boolean          isConcatMessage      = false;
    private FeatureCode      mFeatureCode         = null;
    private int              mDcs                 = -1;
    private String           mCluster             = null;
    private String           mInterfaceStatusCode = null;
    private String           mClientMid           = null;

    private StringBuffer sb =null;
    public SubmitSmRequest(
            SubmitSm aSubmitSm,
            SubmitSmResp aSubmitSmResp,
            SessionDetail aSessionDetail, StringBuffer sb)
    {
    	this.sb=sb;
        mSubmitSm      = aSubmitSm;
        mSessionDetail = aSessionDetail;
        mSystemId      = mSessionDetail.getSystemId();

        mReceivedTime  = System.currentTimeMillis();
        mServiceType   = mSubmitSm.getServiceType();

        mClientIp      = mSessionDetail.getHost();
        mMobileNumber  = CommonUtility.nullCheck(mSubmitSm.getDestAddress().getAddress(), true);
        mHeader        = CommonUtility.nullCheck(mSubmitSm.getSourceAddress().getAddress(), true);
        mClientId      = mSessionDetail.getClientId();

        updateDcs();
        updateEsmClass();

        updateMessageAndMessageClass();
        
        mScheduleTime=ScheduleValidate.getScheduleDelivery(aSubmitSm,mSessionDetail);
     
        mMessageExpiry=MessageValidityPeriod.getMessageValidityPeriod(aSubmitSm, aSubmitSmResp,mClientId);
        
        if (aSubmitSmResp.getCommandStatus() != 0)
        {
            mInterfaceStatusCode = InterfaceStatusCode.EXPIRY_MINUTES_BEYOUND_TIME_BOUNDRY.getStatusCode();
        }


        if (log.isDebugEnabled())
            log.debug("Registered Delivery Value : " + mSubmitSm.getRegisteredDelivery());

        if (mSubmitSm.getRegisteredDelivery() > 0)
            mDlrReqFromClient = "1";
        else
            mDlrReqFromClient = "0";

        log.info(mSystemId + " Delivery type : " + mDlrReqFromClient);
        
        mDltEntityId    = TLVValidate.getEntityId(aSubmitSm);
        mDltTemplateId  =  TLVValidate.getTemplateId(aSubmitSm);
        mDltTelemarketerId  =TLVValidate.getTelemarketerId(aSubmitSm);
        mClientMid      = TLVValidate.getTelemarketerId(aSubmitSm);

        isConcatMessage = isConcatMessage();
        mCluster        = aSessionDetail.getPlatformCluster().getKey();

        extractUdh();
        mFeatureCode=PlatformFeatureCode.setFeatureCode(     mMsgclass,          isConcatMessage);
    }

  
    private void updateMessageAndMessageClass() {
    	SMSBodyContent smsbodycontent=  	SMSDecode.updateMessageAndMessageClass( mSubmitSm, mDcs, mEsmClass, mSessionDetail);
		
    	mMessage=smsbodycontent.getmMessage();
    	mDestPort=smsbodycontent.getmDestPort();
    	mMsgclass=smsbodycontent.getmMsgclass();
    	mUdh=smsbodycontent.getmUdh();
    	mIsHexMsg=smsbodycontent.ismIsHexMsg();
    	mUdhi=smsbodycontent.getmUdhi();
	}


	private void updateDcs()
    {

        /**
         * Fetching the dcs value. This value will be used to check the message
         * type
         */
        try
        {
            final String dataCoding = Byte.toString(mSubmitSm.getDataCoding());

            log.info(mSystemId + " DataCoding : " + dataCoding);
            mDcs = CommonUtility.getInteger(dataCoding, ItextosSmppConstants.DCS_INVALID);
        }
        catch (final Exception e)
        {
            log.error(mSystemId + "  Error while finding dataCoding.  ", e);
        }
    }

    private void updateEsmClass()
    {
        String esmClass = null;

        /**
         * Fetching the esm value. Esm indicates the UDH present in the message
         * or not
         */
        try
        {
            esmClass = Byte.toString(mSubmitSm.getEsmClass());

            if ((esmClass != null) && (esmClass.length() > 0))
            {
                log.info(mSystemId + " EsmClass : " + esmClass);

                esmClass = ItextosSmppUtil.getHexString(esmClass);
                log.info(mSystemId + " EsmClass (HEX): " + esmClass);
            }
            else
                esmClass = null;

            mEsmClass = esmClass;
        }
        catch (final Exception e)
        {
            log.error(mSystemId + " Error while finding esmClass. ", e);
        }
    }

  

   
   
   
   
    /**
     * This method is used to split the header from the message.
     *
     * @param msgWithHeader
     */
 
    public SmppMessageRequest getMessageRequest()
    {
        final SmppMessageRequest lMessageRequest = new SmppMessageRequest();

        lMessageRequest.setDcs(mDcs);
        lMessageRequest.setUdh(mUdh);
        lMessageRequest.setEsmClass(mEsmClass);
        lMessageRequest.setMsgClass(mMsgclass);
        lMessageRequest.setDltEntityId(CommonUtility.nullCheck(mDltEntityId, true));
        lMessageRequest.setHexMsg(mIsHexMsg);
        lMessageRequest.setUdhReferenceNumber(mUdhRefNum);
        lMessageRequest.setDlrReqFromClient(mDlrReqFromClient);
        lMessageRequest.setDltTemplateId(CommonUtility.nullCheck(mDltTemplateId, true));
        lMessageRequest.setDltTelemarketerId(CommonUtility.nullCheck(mDltTelemarketerId, true));

        lMessageRequest.setTotalParts(mTotalMsgParts);
        lMessageRequest.setConcatMessage(isConcatMessage);
        lMessageRequest.setHeader(CommonUtility.nullCheck(mHeader, true));
        lMessageRequest.setScheduleTime(mScheduleTime);
        lMessageRequest.setReceivedTime(mReceivedTime);
        lMessageRequest.setClientId(mClientId);
        lMessageRequest.setClientIp(mClientIp);
        lMessageRequest.setUdhi(mUdhi);
        lMessageRequest.setMobileNumber(mMobileNumber);
        lMessageRequest.setMessage(mMessage);
        lMessageRequest.setMessageExpiry(mMessageExpiry);
        lMessageRequest.setPartNumber(mPartNumber);
        lMessageRequest.setDestPort(mDestPort);
        lMessageRequest.setFeatureCode(mFeatureCode);
        lMessageRequest.setServicetype(mServiceType);
        lMessageRequest.setCluster(mCluster);
        lMessageRequest.setInterfaceErrorCode(mInterfaceStatusCode);
        lMessageRequest.setSystemId(mSystemId);
        lMessageRequest.setBindType(mSessionDetail.getBindName());
        lMessageRequest.setCustMid(CommonUtility.nullCheck(mClientMid, true));

        return lMessageRequest;
    }

    private boolean isConcatMessage()
    {
        if (log.isDebugEnabled())
            log.debug("UDH value : " + mUdh);

        if (mUdh != null)
            try
            {
                final String lTempUDh = mUdh.substring(0, 6);
                return lTempUDh.equals(ItextosSmppConstants.UDH_CONCATENATE_1) || lTempUDh.equals(ItextosSmppConstants.UDH_CONCATENATE_2);
            }
            catch (final Exception e)
            {
                log.error("Invalid UDH received from customer: '" + mSystemId + "', UDH: '" + mUdh + "'", e);
            }

        return false;
    }

    private void extractUdh()
    {

        if (mUdh != null)
        {
            if (log.isDebugEnabled())
                log.debug("UDH : " + mUdh);

            if (mUdh.startsWith(ItextosSmppConstants.UDH_0500))
            {
                mUdhRefNum     = mUdh.substring(6, 8);
                mTotalMsgParts = new BigInteger(mUdh.substring(8, 10), 16).intValue();
                mPartNumber    = new BigInteger(mUdh.substring(10), 16).intValue();
            }
            else
                if (mUdh.startsWith(ItextosSmppConstants.UDH_0608))
                {
                    mUdhRefNum     = mUdh.substring(6, 10);
                    mTotalMsgParts = new BigInteger(mUdh.substring(10, 12), 16).intValue();
                    mPartNumber    = new BigInteger(mUdh.substring(12), 16).intValue();
                }
        }
    }

   

}
