package com.winnovature.campaignfinisher.consumers;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.winnovature.campaignfinisher.daos.GenericDAO;
import com.winnovature.campaignfinisher.singletons.CampaignFinisherPropertiesTon;

import com.winnovature.campaignfinisher.utils.Constants;
import com.winnovature.utils.singletons.ConfigParamsTon;
import com.winnovature.utils.singletons.RedisConnectionTonRoundRobinForCampaign;
import com.winnovature.utils.utils.HeartBeatMonitoring;
import com.winnovature.utils.utils.Utility;

import redis.clients.jedis.Jedis;

public class QueryExecutor extends Thread {

	static Log log = LogFactory.getLog(Constants.CampaignFinisherLogger);
	private static final String className = "[QueryExecutor]";
	private String threadName = null;
	private long sleepTime = 1000;

	public QueryExecutor() throws Exception {
		this.sleepTime = com.winnovature.utils.utils.Utility.getIdleThreadSleepTime();
	}

	@Override
	public void run() {
		String methodName = " [run] ";
		while (true) {
			try {
				String instanceId = CampaignFinisherPropertiesTon.getInstance().getPropertiesConfiguration()
						.getString(Constants.MONITORING_INSTANCE_ID);
				String timeStampAsString = Utility.getTimestampAsString();
				new HeartBeatMonitoring().pushConsumersHeartBeat("FP-CampaignFinisher", "QueryExecutor", instanceId,
						this.getName(), timeStampAsString);
				executeQueries();
			} catch (Exception e) {
				log.error(className + methodName + " RUNTIME EXCEPTION executing queries  \n", e);
				int threadSleepTime = Utility.getThreadSleepTime();
				log.error(className + methodName + " : " + threadName + "  will sleep for " + threadSleepTime
						+ " milli seconds then restarts.");
				consumerSleep(threadSleepTime);
			}
		} // end of while
	}

	private void executeQueries() throws Exception {
		
		String methodName = " [executeQueries] ";
		Map<String, String> configMap = null;
		String updateQueriesQueue = null;
		try {
			configMap = (HashMap<String, String>) ConfigParamsTon.getInstance().getConfigurationFromconfigParams();
			updateQueriesQueue = configMap
					.get(com.winnovature.utils.utils.Constants.STATS_UPDATE_STATUS_QUERY_QUEUE_NAME);
			if (StringUtils.isBlank(updateQueriesQueue)) {
				throw new Exception(" QueryUpdateQ name not found ");
			}
		} catch (Exception e) {
			throw e;
		}
		Jedis resource = null;
		try {
			resource = RedisConnectionTonRoundRobinForCampaign.getInstance().getJedisConnectionAsRoundRobin();
			if (resource.exists(updateQueriesQueue)) {
				String query = resource.rpop(updateQueriesQueue);
				int result = 0;
				if (StringUtils.isNotBlank(query)) {
					try {
						result = new GenericDAO().executeQuery(query);
						if (result != 1) {
							resource.lpush(updateQueriesQueue, query);
						}
						
					} catch (Exception e) {
						log.error(className + methodName, e);
						try {
							
							repushToUpdateQueryQueue(updateQueriesQueue, query,resource);
						} catch (Exception ex) {
							log.error(className + methodName, ex);
							log.error(className + methodName + " : Failed while HO to queue : " + result + ". query : ["
									+ query + "]");
						}
						throw e;
					}
				} else {
					if (resource != null) {
						resource.close();
					}
					// No data found let consumer rest for some time
					consumerSleep(sleepTime);
				}
			} else {
				if (resource != null) {
					resource.close();
				}
				// No data found let consumer rest for some time
				consumerSleep(sleepTime);
			}
		} catch (Exception e) {
			throw e;
		}finally {
			if (resource != null) {
				resource.close();
			}
		}
	}

	public void repushToUpdateQueryQueue(String queue, String query,Jedis resource) throws Exception {
		String methodName = "[repushToUpdateQueryQueue]";
		try {
			resource.lpush(queue, query);
		} catch (Exception e) {
			log.error(className + methodName, e);
			throw e;
		} 
	}

	private void consumerSleep(long sleepTime) {
		try {
			TimeUnit.MILLISECONDS.sleep(sleepTime);
		} catch (Exception e) {
			log.error(className, e);
		}
	}

}
