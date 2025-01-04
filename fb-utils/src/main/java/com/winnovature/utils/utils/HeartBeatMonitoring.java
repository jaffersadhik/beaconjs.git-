package com.winnovature.utils.utils;

import java.util.Calendar;
import java.util.Date;

import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.itextos.beacon.commonlib.constants.ClusterType;
import com.itextos.beacon.commonlib.constants.Component;
import com.itextos.beacon.commonlib.redisconnectionprovider.RedisConnectionProvider;
import com.winnovature.utils.singletons.UtilsPropertiesTon;

import redis.clients.jedis.Jedis;

public class HeartBeatMonitoring {
	private static final String className = "[HeartBeatMonitoring] ";
	PropertiesConfiguration prop = null;
	static Log logger = LogFactory.getLog(Constants.UtilsLogger);

	public void pushConsumersHeartBeat(String module, String consumerName, String instanceid,
			String threadName, String timeStamp) {
		String logName = className + " [pushConsumersHeartBeat] ";

		if (logger.isDebugEnabled()) {
			logger.debug(logName + " begin ... ");
			logger.debug(logName + " Inputs:  queueName:" + consumerName
					+ ", instanceId:" + instanceid + ", threadName:"
					+ threadName + ", timeStamp:" + timeStamp);
		}

		//JedisPool con = null;
		Jedis resource = null;
		String parentkeyName = null;

		try {
			prop = UtilsPropertiesTon.getInstance()
					.getPropertiesConfiguration();
			parentkeyName = prop
					.getString(Constants.REDIS_HEART_BEAT_PARENT_KEY);
		/*	RedisServerDetailsBean redisServerDetailsBean = null;
			redisServerDetailsBean = RedisConnectionFactoryForHeartBeat
					.getInstance()
					.getConfigurationFromconfigParamsForHeartBeat();
*/
	//		if (redisServerDetailsBean != null) {
			//	con = getConnection(redisServerDetailsBean);
				
				resource = RedisConnectionProvider.getInstance().getConnection(ClusterType.COMMON, Component.FP_HEART_BEAT, 1);
						

				String delimeter = ":";
				String subKey = module + delimeter + consumerName + delimeter
						+ instanceid + delimeter + threadName;
				if (parentkeyName != null) {
					String timeStampAsString = Utility.getCustomDateAsString(Constants.DATE_FORMAT_DDMMYYYY);
					parentkeyName = parentkeyName + delimeter
							+ timeStampAsString;
				}

				if (logger.isDebugEnabled()) {
					logger.debug(logName + " parentkeyName:" + parentkeyName
							+ ", subKey:" + subKey);
				}
				long result = resource.hset(parentkeyName, subKey, timeStamp);
				if (resource.exists(parentkeyName) && result == 1) {
					// calculate expiry time
					Calendar calendarToday = Calendar.getInstance();
					calendarToday.add(Calendar.DAY_OF_YEAR, 0);

					Calendar calendarTomorrow = Calendar.getInstance();
					calendarTomorrow.add(Calendar.DAY_OF_YEAR, 1);

					Date startDate = calendarToday.getTime(); // set your start
																// time
					Date endDate = calendarTomorrow.getTime(); // set your end

					long duration = endDate.getTime() - startDate.getTime();
					int diffsec = (int) (duration / (1000));
					resource.expire(parentkeyName, diffsec);
				}

		//	}
		} catch (Exception e) {
			logger.error(logName + " Input:  queueName:" + consumerName
					+ ", instanceId:" + instanceid + ", threadName:"
					+ threadName + ", timeStamp:" + timeStamp);
			logger.error(logName + " Exception occured... \n", e);
		} finally {
			if (resource != null)
				resource.close();
/*
			if (con != null)
				con.close();
	*/	}
		if (logger.isDebugEnabled()) {
			logger.debug(logName + " end.");
		}
	}

	

}
