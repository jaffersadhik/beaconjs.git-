package com.itextos.beacon.smpp.shutdown;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.itextos.beacon.commonlib.utility.CommonUtility;
import com.itextos.beacon.platform.smpputil.ISmppInfo;
import com.itextos.beacon.smpp.objects.SmppObjectType;
import com.itextos.beacon.smpp.objects.inmem.InfoCollection;
import com.itextos.beacon.smpp.server.ItextosSmppServer;

public class ServerShutDown {

    private static final Log   log         = LogFactory.getLog(ServerShutDown.class);

	 public static void shutdown()
	    {

	        ItextosSmppServer.getInstance().shutdownInitiated();
	        ItextosSmppServer.getInstance().getSmppServer().destroy();

	        processUnbindInfo();

	        ItextosSmppServer.getInstance().stop();

	        // TODO: Already called server stop in above statement. Not require to call
	        // again.
	        // stopOtherThreads();

	        // printRunningThreadsInfo();

	        while (!ItextosSmppServer.getInstance().isServerStopped())
	        {
	            log.error("shutdown() - Waiting for the Server to Stop.....");
	            CommonUtility.sleepForAWhile();
	        }

	        log.fatal("Shutdown completed.");
	    }
	 
	 private static void processUnbindInfo()
	    {

	        try
	        {
	            final List<ISmppInfo> unbindInfoList = InfoCollection.getInstance().getObjects(SmppObjectType.UNBIND_INFO_REDIS, 1000);

	            if (log.isDebugEnabled())
	                log.debug("UnbindInfoRedis Q Size - " + unbindInfoList.size());

	            while (!unbindInfoList.isEmpty())
	            {
	                log.error("shutdown() - Updating the unbind redis counts" + unbindInfoList);
	                CommonUtility.sleepForAWhile();
	            }
	        }
	        catch (final Exception e)
	        {
	            log.fatal("Exception while processing the unbind info ", e);
	        }
	    }

}
