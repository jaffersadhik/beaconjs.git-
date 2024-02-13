package com.itextos.beacon.platform.kannelstatusupdater.process;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.Future;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.itextos.beacon.inmemory.carrierhandover.bean.KannelInfo;
import com.itextos.beacon.inmemory.carrierhandover.util.ICHUtil;
import com.itextos.beacon.platform.kannelstatusupdater.beans.KannelStatusInfo;
import com.itextos.beacon.platform.kannelstatusupdater.taskpool.KannelConnectTask;
import com.itextos.beacon.platform.kannelstatusupdater.taskpool.ThreadPoolTon;
import com.itextos.beacon.platform.kannelstatusupdater.utility.RedisProcess;
import com.itextos.beacon.platform.kannelstatusupdater.utility.Utility;

public class DataCollector
{

    private static Log log = LogFactory.getLog(DataCollector.class);

    private DataCollector()
    {}

    public static void getKannelStatusData()
    {
        final Map<String, KannelInfo> lAllRouteConfigs = ICHUtil.getAllRouteConfig();

            log.debug("Kannel info to be validated " + lAllRouteConfigs);

        final HashMap<String, Future<KannelStatusInfo>> resultMap = getHttpConnect(lAllRouteConfigs);


        final Map<String, KannelStatusInfo> outputMap = getResults(resultMap);

            log.debug("resultMap=" + resultMap);

        RedisProcess.populateDataIntoRedis(lAllRouteConfigs, outputMap);
    }

    private static Map<String, KannelStatusInfo> getResults(
            HashMap<String, Future<KannelStatusInfo>> aResultMap)
    {
        final Map<String, KannelStatusInfo> outputMap   = new HashMap<>();
        boolean                             isCompleted = false;

        try
        {

            while (!isCompleted)
            {
                String kannelId = null;

                for (final Entry<String, Future<KannelStatusInfo>> entry : aResultMap.entrySet())
                {
                    kannelId = entry.getKey();
                    final Future<KannelStatusInfo> futureList = entry.getValue();

                    if (futureList.get() != null)
                    {
                        isCompleted = true;
                        outputMap.put(kannelId, futureList.get());
                    }
                    else
                    {
                        // Wait to finish the HTTP call.
                        isCompleted = false;
                        break;
                    }
                }

                if (!isCompleted)
                {
                    log.error("Waiting for the Kannel Status to complete. for Kannel '" + kannelId + "'");
                    Thread.sleep(1000);
                }
            }
        }
        catch (final Exception e)
        {
            log.error("Exception while getting the response ", e);
        }

        return outputMap;
    }

    private static HashMap<String, Future<KannelStatusInfo>> getHttpConnect(
            Map<String, KannelInfo> aAllRouteConfigs)
    {
        final HashMap<String, Future<KannelStatusInfo>> resultMap = new HashMap<>();

        for (final Entry<String, KannelInfo> entry : aAllRouteConfigs.entrySet())
        {
            final String     kannelId   = entry.getKey();
            final KannelInfo kannelInfo = entry.getValue();

            if (log.isInfoEnabled())
                log.info("Kannel Id : '" + kannelId + "' ===> '" + kannelInfo + "'");

            try
            {
                final String kannelURL = Utility.formatURL(kannelInfo);

                if (log.isInfoEnabled())
                    log.info("Checking status for : '" + kannelId + "' with URL : '" + kannelURL + "'");

                final KannelConnectTask        task   = new KannelConnectTask(kannelId, kannelURL);
                final Future<KannelStatusInfo> future = ThreadPoolTon.getInstance().getExecutor().submit(task);
                resultMap.put(kannelId, future);
            }
            catch (final Exception e)
            {
                resultMap.put(kannelId, null);
                log.error("Exception while getting future object for Kannel '" + kannelId + "'", e);
            }
        }
        return resultMap;
    }

}