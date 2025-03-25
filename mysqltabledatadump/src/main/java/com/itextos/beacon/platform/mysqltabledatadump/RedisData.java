package com.itextos.beacon.platform.mysqltabledatadump;

import java.util.HashMap;
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
	  
	  
	  public static Map<String,Map<String, String> > getGeneralData(){
		  
		  Map<String,Map<String, String> > result=new HashMap<String,Map<String, String>> ();
		  
		  result.put("acc:current:sa", getGeneralData("acc:current:sa"));
		  result.put("acc:current:user", getGeneralData("acc:current:user"));
		  result.put("acc:current:admin", getGeneralData("acc:current:admin"));
		  result.put("cli:api:pass", getGeneralData("cli:api:pass"));
		  result.put("cli:smpp:pass", getGeneralData("cli:smpp:pass"));
		  result.put("cli:gui:pass", getGeneralData("cli:gui:pass"));

		

		  return result;
		  
		  
	  }
	  private  static Map<String, String> getGeneralData(String key)
	    {

		  
		  Jedis jedis =null;
	        try 
	        {
	        	
	        	
	        	 jedis = RedisConnectionProvider.getInstance().getConnection(ClusterType.COMMON, Component.GENE€RAL, 1);
	            return jedis.hgetAll(key);
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
