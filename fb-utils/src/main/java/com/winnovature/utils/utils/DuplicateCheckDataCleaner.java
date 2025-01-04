package com.winnovature.utils.utils;

import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.itextos.beacon.commonlib.constants.ClusterType;
import com.itextos.beacon.commonlib.constants.Component;
import com.itextos.beacon.commonlib.redisconnectionprovider.RedisConnectionProvider;

import redis.clients.jedis.Jedis;

public class DuplicateCheckDataCleaner {
	static Log log = LogFactory.getLog(Constants.UtilsLogger);
	private static final String className = "[DuplicateCheckDataCleaner]";

	public void clearDuplicateCheckData(List<Map<String, String>> campaigns, String dupcheckRedisKeyPrefix) {
		String logname = className + " [clearDuplicateCheckData()] ";
		Jedis con = null;
		try {
			if(RedisConnectionProvider.getInstance().getRedisPoolCount(ClusterType.COMMON, Component.FP_DUPLICATE_CHECK) > 0) {
				int counter=0;
				
				if (counter<RedisConnectionProvider.getInstance().getRedisPoolCount(ClusterType.COMMON, Component.FP_DUPLICATE_CHECK)) {
					counter++;
					con = RedisConnectionProvider.getInstance().getConnection(ClusterType.COMMON, Component.FP_DUPLICATE_CHECK, counter);
					if (con != null) {
						for (Map<String, String> campaign : campaigns) {
							String hashKey = dupcheckRedisKeyPrefix + "~" + campaign.get("id");
							String setKey = dupcheckRedisKeyPrefix + "~" + campaign.get("id") + "~" + campaign.get("cli_id");
							con.del(hashKey);
							con.del(setKey);
						}
					}
				}
			}			
		} catch (Exception e) {
			log.error(logname + " Exception ::: ", e);
		} finally {
			if (con != null) {
				con.close();
			}
		}
	}

}
