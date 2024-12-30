package com.itextos.beacon.smpp.schedule;

import java.util.Date;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.cloudhopper.smpp.SmppConstants;
import com.cloudhopper.smpp.pdu.SubmitSm;
import com.cloudhopper.smpp.pdu.SubmitSmResp;
import com.itextos.beacon.commonlib.constants.DateTimeFormat;
import com.itextos.beacon.commonlib.constants.InterfaceStatusCode;
import com.itextos.beacon.commonlib.constants.MessageType;
import com.itextos.beacon.commonlib.constants.RouteType;
import com.itextos.beacon.commonlib.timezoneutility.TimeZoneUtility;
import com.itextos.beacon.commonlib.utility.CommonUtility;
import com.itextos.beacon.commonlib.utility.DateTimeUtility;
import com.itextos.beacon.smpp.dto.SessionDetail;
import com.itextos.beacon.smpp.dto.SmppMessageRequest;

public class ScheduleValidate {

    private static final Log    log                    = LogFactory.getLog(ScheduleValidate.class);

	private ScheduleValidate() {}
	
	
	 public static String getScheduleDelivery(
	            SubmitSm aSubmitSm,
	            SessionDetail aSessionDetails)
	    {
		 
		 String           mScheduleTime=null;

	        /**
	         * Fetching the schedule delivery time.
	         */

	        try
	        {
	            // the full smpp format is yymmddhhmmsstnnp.ignore last 6
	            final String origDelTS = aSubmitSm.getScheduleDeliveryTime();
	       
	            if ((origDelTS != null) && !origDelTS.isEmpty() && (origDelTS.length() > 10))
	            {
	                final String deliveryTS = origDelTS.substring(0, 10);

	         
	                mScheduleTime = DateTimeUtility.getFormattedDateTime(DateTimeUtility.getDateFromString(deliveryTS, DateTimeFormat.NO_SEPARATOR_YY_MM_DD_HH_MM),
	                        DateTimeFormat.DEFAULT_YYYY_MM_DD_HH_MM);
	                /*
	                 * final SimpleDateFormat smppDateFormat = new
	                 * SimpleDateFormat(SmppUtilConstants.SMPP_DATE_FORMAT);
	                 * smppDateFormat.setLenient(false);
	                 * final Date date = smppDateFormat.parse(deliveryTS);
	                 * if (log.isInfoEnabled())
	                 * log.info("sdf_1.parse(deliveryTS)=>>" + date);
	                 * final SimpleDateFormat dateFormat = new
	                 * SimpleDateFormat(SmppUtilConstants.DATE_FORMAT);
	                 * dateFormat.setLenient(false);
	                 * mScheduleTime = dateFormat.format(date);
	                 */

	                  
	                
	                mScheduleTime= scheduleValidity(mScheduleTime,CommonUtility.nullCheck(aSessionDetails.getAccountTimeZone()));
	            }
	            else
	                mScheduleTime = null;
	        }
	        catch (final Exception e)
	        {
	        }
	        
	        return mScheduleTime;
	    }

	
	 public static void checkTimeOfDelivery(
	            SmppMessageRequest aSmppMessageReq,
	            SubmitSmResp aSubmitResponse,
	            SessionDetail aSessionDetails)
	    {

	        try
	        {
	            final String lScheduleTime = aSmppMessageReq.getScheduleTime();
	            if (log.isDebugEnabled())
	                log.debug("Schedule Time : " + lScheduleTime);

	            final Date lToDate = (lScheduleTime == null) ? new Date() : DateTimeUtility.getDateFromString(lScheduleTime, DateTimeFormat.DEFAULT);

	            if (lToDate != null)
	            {
	                final MessageType lMsgType = aSessionDetails.getMessageType();

	                if ((MessageType.PROMOTIONAL == lMsgType) && ( aSmppMessageReq.getRouteType()!=null && RouteType.DOMESTIC == aSmppMessageReq.getRouteType()))
	                {
	                    final boolean lTraBlockoutStatus = TraiBlockoutCheck.isValidTraiBlockOut(lToDate, aSessionDetails, aSmppMessageReq, aSubmitResponse);

	                    if (log.isDebugEnabled())
	                        log.debug("Tra blockout status : " + lTraBlockoutStatus);
	                }
	            }
	        }
	        catch (final Exception e)
	        {
	            aSubmitResponse.setCommandStatus(SmppConstants.STATUS_SYSERR);
	            aSmppMessageReq.setInterfaceErrorCode(InterfaceStatusCode.INTERNAL_SERVER_ERROR.getStatusCode());
	            log.error("Problem processing pdu scheduled time due to ...", e);
	        }
	    }

	 
	 private static String scheduleValidity(String mScheduleTime,String timeZone)
	    {
	        if (log.isDebugEnabled())
	            log.debug("Schedule Validity Period ............");

	        mScheduleTime = CommonUtility.nullCheck(mScheduleTime, true);

	        if (!mScheduleTime.isEmpty())
	        {
	            String       deliveryTime = DateTimeUtility.getFormattedDateTime(DateTimeUtility.getDateFromString(mScheduleTime, DateTimeFormat.DEFAULT_YYYY_MM_DD_HH_MM), DateTimeFormat.DEFAULT);

	          //  final      = f

	            if (log.isDebugEnabled())
	                log.debug("Time Zone Value :" + timeZone + ", Delivery time:'" + deliveryTime + "'");

	            if (!timeZone.isBlank())
	                try
	                {
	                    // Converting schedule time from UTC into IST timezone
	                    final Date lScheduleDate = TimeZoneUtility.getDateBasedOnTimeZone(deliveryTime, DateTimeFormat.DEFAULT, timeZone);
	                    if (log.isDebugEnabled())
	                        log.debug("After convert Timezone sctime :" + lScheduleDate);

	                    final String deliveryTimeInTz = DateTimeUtility.getFormattedDateTime(lScheduleDate, DateTimeFormat.DEFAULT);

	                    if (log.isDebugEnabled())
	                        log.debug("Formated Timezone sctime :" + deliveryTimeInTz);

	                    if (deliveryTimeInTz != null)
	                        deliveryTime = deliveryTimeInTz;
	                }
	                catch (final Exception ignore)
	                {}
	            mScheduleTime = deliveryTime;

	            
	            if (log.isDebugEnabled())
	                log.debug("Schedule time after timezone set : " + mScheduleTime);
	            
	            
	        }
	        
	        return mScheduleTime;
	    }

}
