package com.unitia.redis;

import java.util.ArrayList;
import java.util.List;


public class RedisInstance {

	private static RedisInstance obj=null;
	
	private RedisInstance(){
	
	}
	
	public static RedisInstance getInstance(){
		
		if(obj==null){
			
			obj=new RedisInstance();
		}
		
		return obj;
	}
	
	


	public List<String> getRedisInstanceList(){
		
		List<String> result=new ArrayList();
		result.add("redisqueue1");
		result.add("redisqueue2");
		result.add("redisqueue3");
		result.add("redisqueue4");

		return result;
	}
	
}
