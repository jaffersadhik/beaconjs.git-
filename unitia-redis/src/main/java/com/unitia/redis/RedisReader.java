package com.unitia.redis;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.itextos.beacon.commonlib.constants.ErrorMessage;
import com.itextos.beacon.commonlib.message.BaseMessage;
import com.itextos.beacon.smslog.ErrorLog;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

public class RedisReader {


	
public BaseMessage getData(String queuename,String redisid) throws ClassNotFoundException, IOException{
	

    long start=System.currentTimeMillis();
    
        JedisPool pool = null;
        Jedis jedis = null;
        byte[] bytes=null;
        Object result=null;
        try
        {
            pool = RedisQueueConnectionPool.getInstance().getPool(redisid,queuename);
            jedis = pool.getResource();
           
            bytes= jedis.rpop((queuename).getBytes("utf-8"));
            
            if(bytes==null || bytes.length == 0)
            {
            	result=null;
            }
            else
            {
            	result=consume(bytes);
            }
        }
        catch (Exception e)
        {

        	ErrorLog.log(new Date()+" \t "+ErrorMessage.getStackTraceAsString(e));
        	System.err.println();
        	
        	try {
				Thread.sleep(1000L);
			} catch (InterruptedException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
        	
        }finally{
        	if (jedis != null){
				try {
					jedis.close();
					
				}
				catch (Exception e) {

					e.printStackTrace();
				}
		}
        }
        
        long end=System.currentTimeMillis();
        

        	
            return (BaseMessage)result;

	}

	


	private Object consume(byte[] bytes) throws Exception
	    {
	        ByteArrayInputStream bis = null;
	        ObjectInput in = null;

	        bis = new ByteArrayInputStream(bytes);

	        in = new ObjectInputStream(bis);
	        Object dtoobj =  in.readObject();
	        in.close();
	        bis.close();
	        return dtoobj;
	    }


	public BaseMessage getData(String queuename) {

        long start=System.currentTimeMillis();
        String redisid="NULL";
        JedisPool pool = null;
        Jedis jedis = null;
        byte[] bytes=null;
        Object result=null;
        try
        {
        	redisid=RedisQueueConnectionPool.getInstance().getRedisIdForReader(queuename);
        	
        	
        	if(redisid!=null){
            pool = RedisQueueConnectionPool.getInstance().getPool(redisid,queuename);
            jedis = pool.getResource();
            List<byte[]> list=jedis.brpop(0, (queuename).getBytes("utf-8"));
           if(list!=null){
            bytes = list.get(1);
            }
            if(bytes==null || bytes.length == 0)
            {
            	result=null;
            }
            else
            {
            	result=consume(bytes);
            }
        	}
        }
        catch (Exception e)
        {

        	e.printStackTrace();
        	
        }finally{
        	if (jedis != null){
				try {
					jedis.close();
					
				}
				catch (Exception e) {

					e.printStackTrace();
				}
		}
        }
        
        long end=System.currentTimeMillis();
        

        return (BaseMessage)result;
    
	
	}

}
