package com.itextos.beacon.platform.kannelstatusupdater.process;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.concurrent.Future;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.itextos.beacon.commonlib.utility.CommonUtility;
import com.itextos.beacon.inmemory.carrierhandover.bean.KannelInfo;
import com.itextos.beacon.inmemory.carrierhandover.util.ICHUtil;
import com.itextos.beacon.platform.kannelstatusupdater.beans.KannelStatusInfo;
import com.itextos.beacon.platform.kannelstatusupdater.taskpool.KannelConnectTask;
import com.itextos.beacon.platform.kannelstatusupdater.taskpool.ThreadPoolTon;
import com.itextos.beacon.platform.kannelstatusupdater.utility.RedisProcess;
import com.itextos.beacon.platform.kannelstatusupdater.utility.Utility;
import com.itextos.beacon.smslog.KannelStatusLog;

public class DataCollector
{

    private static Log log = LogFactory.getLog(DataCollector.class);

    private DataCollector()
    {}

    public static void getKannelStatusData()
    {
        final Map<String, KannelInfo> lAllRouteConfigs = ICHUtil.getAllRouteConfig();
        
        

        Set<String> kannelSet=getKannelSet(lAllRouteConfigs);
        
            log.debug("Kannel info to be validated " + lAllRouteConfigs);

            KannelStatusLog.log("kannelSet : "+kannelSet);

        final HashMap<String, Future<KannelStatusInfo>> resultMap = getHttpConnect(kannelSet);

        KannelStatusLog.log("resultMap : "+resultMap);


        final Map<String, KannelStatusInfo> outputMap = getResults(resultMap);

            log.debug("resultMap=" + resultMap);

        KannelStatusLog.log("outputMap : "+outputMap);
        RedisProcess.populateDataIntoRedis( outputMap);
    }

    private static Set<String> getKannelSet(Map<String, KannelInfo> lAllRouteConfigs) {
    	Set<String> result=new HashSet<String>();
    	lAllRouteConfigs.forEach((k,v)->{
    		
    		final String           kannelIpPort = CommonUtility.combine(':', v.getKannelIp(), v.getKannelPort(), "StatusPort", v.getStatusPort());
    		result.add(kannelIpPort);
    	});
		return result;
	}

	private static Map<String, KannelStatusInfo> getResults(
            HashMap<String, Future<KannelStatusInfo>> aResultMap)
    {
        final Map<String, KannelStatusInfo> outputMap   = new HashMap<>();

        try
        {

         
                for (final Entry<String, Future<KannelStatusInfo>> entry : aResultMap.entrySet())
                {
                  String  kannelId = entry.getKey();
                  
                    while(true) {

                    final Future<KannelStatusInfo> futureList = entry.getValue();

                    if (futureList.get() != null)
                    {
                        outputMap.put(kannelId, futureList.get());
                        break;
                    }
                    else
                    {
                    	 Thread.sleep(500);
                    }
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
            Set<String> aAllRouteConfigs)
    {
        final HashMap<String, Future<KannelStatusInfo>> resultMap = new HashMap<>();

        Iterator itr=aAllRouteConfigs.iterator();
        
        while(itr.hasNext()) {
            final String     kannelId   =itr.next().toString();
            StringTokenizer st=new StringTokenizer(kannelId,":");
            
            String ip=st.nextToken();
            st.nextToken();
            st.nextToken();
            String port=st.nextToken();
            

            if (log.isInfoEnabled())
                log.info("Kannel Id : '" + kannelId );

            try
            {
                final String kannelURL = Utility.formatURL(ip,port);

                if (log.isInfoEnabled())
                    log.info("Checking status for : '" + kannelId + "' with URL : '" + kannelURL + "'");
                
                KannelStatusLog.log("Checking status for : '" + kannelId + "' with URL : '" + kannelURL + "'");

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