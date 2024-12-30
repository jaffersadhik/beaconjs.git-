package com.itextos.beacon.smpp.throttle;

import com.cloudhopper.smpp.SmppConstants;
import com.cloudhopper.smpp.pdu.SubmitSmResp;
import com.itextos.beacon.commonlib.constants.InterfaceStatusCode;
import com.itextos.beacon.smpp.redisoperations.Throttler;
import com.itextos.beacon.smpp.dto.SessionDetail;
import com.itextos.beacon.smpp.dto.SmppMessageRequest;

public class ThrottleValidate {

	 public static void throttleMessage(
	            SessionDetail aSessionDetail,
	            SubmitSmResp aSubmitSmResp,
	            SmppMessageRequest aSmppMessageRequest)
	    {
	        final String lClientId = aSessionDetail.getClientId();
	        final int    lMaxSpeed = aSessionDetail.getMaxSpeedAllowded();

	        if ((lMaxSpeed > 0) && !Throttler.canSend(lClientId, lMaxSpeed))
	        {
	            aSubmitSmResp.setCommandStatus(SmppConstants.STATUS_THROTTLED);
	            aSmppMessageRequest.setInterfaceErrorCode(InterfaceStatusCode.SMPP_THROTTLE_LIMIT_EXCEED.getStatusCode());
	        }
	    }

}
