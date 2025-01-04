package com.winnovature.utils.singletons;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.itextos.beacon.commonlib.constants.ClusterType;
import com.itextos.beacon.commonlib.constants.Component;
import com.itextos.beacon.commonlib.redisconnectionprovider.RedisConnectionProvider;
import com.winnovature.utils.daos.GenericDao;
import com.winnovature.utils.utils.Constants;

import redis.clients.jedis.Jedis;


public class RedisConnectionTonRoundRobinForDuplicateCheck {

	private static String className = "[RedisConnectionTonRoundRobinForUnProcess]";
	static Log log = LogFactory.getLog(Constants.UtilsLogger);

	private static RedisConnectionTonRoundRobinForDuplicateCheck singleton;
	private static int roundRobinPointer = 0;
	private int connectionErrorCounter;
	private String methodName;

	private RedisConnectionTonRoundRobinForDuplicateCheck() {
	}

	public static RedisConnectionTonRoundRobinForDuplicateCheck getInstance() {

		if (singleton == null) {
			singleton = new RedisConnectionTonRoundRobinForDuplicateCheck();
		}

		return singleton;
	}

	
	
	
	
	public Jedis getJedisConnectionAsRoundRobin() throws Exception {
		methodName = "[getJedisConnectionAsRoundRobin]";

	

		// If not null
		if (RedisConnectionProvider.getInstance().getRedisPoolCount(ClusterType.COMMON, Component.FP_DUPLICATE_CHECK)  > 0) {

			// Try to connect to at-least one redis server
			while (connectionErrorCounter <= RedisConnectionProvider.getInstance().getRedisPoolCount(ClusterType.COMMON, Component.FP_DUPLICATE_CHECK)) {
				// If increment is at last value, reset it to 0
				if (roundRobinPointer > RedisConnectionProvider.getInstance().getRedisPoolCount(ClusterType.COMMON, Component.FP_DUPLICATE_CHECK) - 1) {
					roundRobinPointer = 0;
				}

				Jedis resource = null;

				try {
			/*		resource = RedisConnectionFactoryForUnProcess.getInstance()
							.getConnection(bean.getRid());
				*/	
					resource=RedisConnectionProvider.getInstance().getConnection(ClusterType.COMMON, Component.FP_DUPLICATE_CHECK, roundRobinPointer+1);
					roundRobinPointer++;

					connectionErrorCounter = 0;
					return resource;

				} catch (Exception e) {
					log.error(className + methodName
							+ " Exception while connecting to redis for bean:"
							, e);

					connectionErrorCounter++;
				} 
			}

			connectionErrorCounter = 0;
		} else {
			throw new Exception("Redis not configured/reachable in Task Engine");
		}
		return null;
	}
}
