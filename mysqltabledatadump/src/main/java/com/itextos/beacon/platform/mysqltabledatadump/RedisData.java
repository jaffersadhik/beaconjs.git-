package com.itextos.beacon.platform.mysqltabledatadump;

import java.util.Iterator;
import java.util.Map;

import com.itextos.beacon.commonlib.constants.ClusterType;
import com.itextos.beacon.commonlib.constants.Component;
import com.itextos.beacon.commonlib.redisconnectionprovider.RedisConnectionProvider;

import redis.clients.jedis.Jedis;

public class RedisData {
	
    private static final String PREPAID_KEY      = "wallet:amount";

	  public  static Map<String, String> getPrepaidData()
	    {

		  
		  Jedis jedis =null;
	        try 
	        {
	        	
	        	
	        	 jedis = RedisConnectionProvider.getInstance().getConnection(ClusterType.COMMON, Component.WALLET_CHK, 1);
	            return jedis.hgetAll(PREPAID_KEY);
	        }
	        catch (final Exception e)
	        {
	        	e.printStackTrace();
	        }finally {
	        	
	        	if(jedis!=null) {
	        	jedis.close();
	        	}
	        }
	        return null;
	    }
	  
	  
	  public  static void putPrepaidData( Map<String, String> data)
	    {

		  
		  Jedis jedis =null;
	        try 
	        {
	        	
	        	
	        	 jedis = RedisConnectionProvider.getInstance().getConnection(ClusterType.COMMON, Component.WALLET_CHK, 1);

	        	 Iterator<String> itr =data.keySet().iterator();
	        	 
	        	 while(itr.hasNext()) {
	        		 
	        		 String key=itr.next();
	        		 String value=data.get(key);
		        	 jedis.hset(PREPAID_KEY, key, value);

	        	 }
	        }
	        catch (final Exception e)
	        {
	        	e.printStackTrace();
	        }finally {
	        	
	        	if(jedis!=null) {
	        	jedis.close();
	        	}
	        }
	    }

}
