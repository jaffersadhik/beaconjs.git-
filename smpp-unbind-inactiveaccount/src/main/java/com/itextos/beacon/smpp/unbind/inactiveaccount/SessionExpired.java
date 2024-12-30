package com.itextos.beacon.smpp.unbind.inactiveaccount;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.itextos.beacon.smpp.sessioninfo.ItextosSessionManager;

public class SessionExpired {

    private static final Log                                      log             = LogFactory.getLog(SessionExpired.class);

	  public static void unbindInactiveAccount()
	    {

          ItextosSessionManager.getInstance().checkDisabledAccounts();
	    }

}
