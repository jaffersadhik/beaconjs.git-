package com.itextos.beacon.smpp.messagevalidityperiod;

import java.util.Date;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.cloudhopper.smpp.SmppConstants;
import com.cloudhopper.smpp.pdu.SubmitSm;
import com.cloudhopper.smpp.pdu.SubmitSmResp;
import com.itextos.beacon.commonlib.constants.ConfigParamConstants;
import com.itextos.beacon.commonlib.constants.CustomFeatures;
import com.itextos.beacon.commonlib.constants.DateTimeFormat;
import com.itextos.beacon.commonlib.utility.CommonUtility;
import com.itextos.beacon.commonlib.utility.DateTimeUtility;
import com.itextos.beacon.smpp.utils.AccountDetails;
import com.itextos.beacon.smpp.utils.SmppDateHandler;

public class MessageValidityPeriod {

    private static final Log log                  = LogFactory.getLog(MessageValidityPeriod.class);

	 public static String getMessageValidityPeriod(
	            SubmitSm aSubmitSm,
	            SubmitSmResp aSubmitSmResp,String mClientId)
	    {

		 String           mMessageExpiry=null;
	        /**
	         * Fetching the validity period, finding the difference from the current
	         * time in minutes.
	         */
	        try
	        {
	            final String origValPeriod = aSubmitSm.getValidityPeriod();
	         
	            if ((origValPeriod != null) && !origValPeriod.isEmpty() && (origValPeriod.length() > 4))
	            {
	                final boolean isValidityPeriodTimeZoneBasedOnApplicationTimeZone = isValidityPeriodTimeZoneBasedOnApplicationTimeZone(String.valueOf(mClientId));

	                if (isValidityPeriodTimeZoneBasedOnApplicationTimeZone)
	                {
	               
	                    // the full smpp format is yymmddhhmmsstnnp.ignore last 4
	                    final String validityPeriod = origValPeriod.substring(0, origValPeriod.length() - 4);

	              
	                    final Date validDate = DateTimeUtility.getDateFromString(validityPeriod, DateTimeFormat.NO_SEPARATOR_YY_MM_DD_HH_MM_SS);
	                    checkAndSetExpiryTime(aSubmitSmResp, validDate);
	                }
	                else
	                {
	                    // Default SmppInterface allowd ValidityPeriod time zone is UTC.
	     
	                    try
	                    {
	                        final Date lValidDate = new SmppDateHandler(origValPeriod).getScheduledTime();

	                        if (lValidDate != null)
	                        	mMessageExpiry = checkAndSetExpiryTime(aSubmitSmResp, lValidDate);
	                        else
	                            mMessageExpiry = null;
	                    }
	                    catch (final Exception e)
	                    {
	                        mMessageExpiry = null;
	                    }
	                }
	            }
	           }
	        catch (final Exception e)
	        {
	        }
	        
	        return mMessageExpiry;
	    }
	 


	    private static String checkAndSetExpiryTime(
	            SubmitSmResp aSubmitSmResp,
	            Date validDate)
	    {
	    	
			 String           mMessageExpiry=null;

	        final int  _MAX_EXPIRY = CommonUtility.getInteger(AccountDetails.getConfigParamsValueAsString(ConfigParamConstants.SMPP_MAX_EXPIRY_MINUTES_ALLOW));
	        final long MAX_EXPIRY  = _MAX_EXPIRY * 60;

	        final Date currentDate = new Date();
	        final long validInSec  = (validDate.getTime() / 1000) - (currentDate.getTime() / 1000);

	        if (log.isDebugEnabled())
	        {
	            log.debug("checkAndSetExpiryTime() - Max Expiry sec :" + MAX_EXPIRY);
	            log.debug("checkAndSetExpiryTime() - Valid in sec :" + validInSec);
	        }

	        if (validInSec > MAX_EXPIRY)
	        {
	            aSubmitSmResp.setCommandStatus(SmppConstants.STATUS_INVEXPIRY);
	         }
	        else
	        {
	            if (validInSec <= 0)
	                mMessageExpiry = null;
	            else
	                mMessageExpiry = String.valueOf(validInSec);

	         }
	        
	        return mMessageExpiry;
	    }

	    
	    private static boolean isValidityPeriodTimeZoneBasedOnApplicationTimeZone(
	            String aClientId)
	    {
	        final String lagacyVPTimeZoneEnabled = AccountDetails.getAccountCustomeFeature(aClientId, CustomFeatures.LEGACY_MESSAGE_EXPIRY_TIME_ZONE);
	        // TODO This need to be done through the properties
	        return CommonUtility.nullCheck(lagacyVPTimeZoneEnabled, true).equalsIgnoreCase("IST");
	    }
}
