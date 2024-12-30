package com.itextos.beacon.smpp.tlv;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.cloudhopper.smpp.SmppConstants;
import com.cloudhopper.smpp.pdu.SubmitSm;
import com.cloudhopper.smpp.pdu.SubmitSmResp;
import com.itextos.beacon.commonlib.constants.ConfigParamConstants;
import com.itextos.beacon.commonlib.constants.InterfaceStatusCode;
import com.itextos.beacon.commonlib.utility.CommonUtility;
import com.itextos.beacon.smpp.dto.SmppMessageRequest;
import com.itextos.beacon.smpp.utils.AccountDetails;
import com.itextos.beacon.smpp.utils.ItextosSmppUtil;

public class TLVValidate {

    private static final Log    log                    = LogFactory.getLog(TLVValidate.class);

    public static String getEntityId(SubmitSm aSubmitSm) {
    	
    	return getOptionalParameter(aSubmitSm, TLV.ENTITYID);
    }
    
    public static String getTemplateId(SubmitSm aSubmitSm) {
    	
    	return getOptionalParameter(aSubmitSm, TLV.TEMPLATEID);
    }
    
    public static String getTelemarketerId(SubmitSm aSubmitSm) {
    	
    	return getOptionalParameter(aSubmitSm, TLV.TELEMARKETERID);
    }
    
    private static String getOptionalParameter(
            SubmitSm aSubmitSm,
            String aOptionalParamter)
    {
        String       returnValue = null;
        final String optParam    = CommonUtility.nullCheck(aOptionalParamter, true);

        if (log.isDebugEnabled())
            log.debug("Optional Param: " + optParam);

        if (!optParam.isBlank())
            try
            {
                final short tlvParam = Short.parseShort(optParam, 16);

                if (log.isDebugEnabled())
                    log.debug("TLV Param :" + tlvParam);

                if ((aSubmitSm.hasOptionalParameter(tlvParam)) && (aSubmitSm.getOptionalParameter(tlvParam) != null))
                {
                    returnValue = aSubmitSm.getOptionalParameter(tlvParam).getValueAsString();
                }
            }
            catch (final Exception e)
            {
            }
        return returnValue;
    }
    
    
	   public static void validateDltEntityId(
	            SmppMessageRequest aSmppMessageRequest,
	            SubmitSmResp aSubmitSmResp)
	    {
	        final String aDltEntityId = aSmppMessageRequest.getDltEntityId();

	        if ((aDltEntityId != null) && !aDltEntityId.isEmpty())
	            if (!validateNumaricAndLength(aDltEntityId))
	            {
	                if (log.isDebugEnabled())
	                    log.debug("Invalid DLT Entity Id :" + aDltEntityId);
	                aSubmitSmResp.setCommandStatus(SmppConstants.STATUS_DELIVERYFAILURE);
	                aSmppMessageRequest.setInterfaceErrorCode(InterfaceStatusCode.INVALID_DLT_ENTITY_ID.getStatusCode());
	            }
	    }
	   
	   public static boolean validateNumaricAndLength(
	            String aValue)
	    {
	        final int lMinValue = CommonUtility.getInteger(AccountDetails.getConfigParamsValueAsString(ConfigParamConstants.DLT_PARAM_MIN_LENGTH));
	        final int lMaxValue = CommonUtility.getInteger(AccountDetails.getConfigParamsValueAsString(ConfigParamConstants.DLT_PARAM_MAX_LENGTH));

	        if (ItextosSmppUtil.isNumaric(aValue) && ((aValue.length() >= lMinValue) && (aValue.length() <= lMaxValue)))
	            return true;

	        return false;
	    }
	   
	   public static void validateDltTemplateId(
	            SmppMessageRequest aSmppMessageRequest,

	            SubmitSmResp aSubmitSmResp)
	    {
	        final String aDltTemplateId = aSmppMessageRequest.getDltTemplateId();

	        if ((aDltTemplateId != null) && !aDltTemplateId.isEmpty())
	            if (!validateNumaricAndLength(aDltTemplateId))
	            {
	                if (log.isDebugEnabled())
	                    log.debug("Invalid DLT Template  Id :" + aDltTemplateId);
	                aSubmitSmResp.setCommandStatus(SmppConstants.STATUS_DELIVERYFAILURE);
	                aSmppMessageRequest.setInterfaceErrorCode(InterfaceStatusCode.INVALID_DLT_TEMPLATE_ID.getStatusCode());
	            }
	    }
}
