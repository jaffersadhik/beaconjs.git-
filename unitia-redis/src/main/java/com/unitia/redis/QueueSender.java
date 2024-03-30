package com.unitia.redis;

import java.io.IOException;
import java.util.Map;

import com.itextos.beacon.commonlib.message.MessageRequest;
import com.unitia.util.dao.Insert;

public class QueueSender {


	
	public boolean sendL(String queuename,MessageRequest requestObject,boolean isRetry,Map<String,Object > logmap) throws IOException {
		
		
		
		QueueName.getInstance().add(queuename);
		String searchredisidqueuename=queuename;
		boolean result=false;
		
		String redisid=null;
		
		
		
		String username=requestObject.getUser();
		String msgid=requestObject.getFileId();
		
		String smscid=requestObject.getSmscId();
		String scheduletime=requestObject.getScheduleDateTime()==null?"0":requestObject.getScheduleDateTime().getTime()+"";
		long start=System.currentTimeMillis();
		
		String mobile=requestObject.getMobileNumber();
		if(mobile.indexOf("@")>0) {
			
			if(searchredisidqueuename.equals("commonpool")) {
				searchredisidqueuename="emailqueue";
				queuename="emailqueue";
				QueueName.getInstance().add(queuename);

			}
		}
		
		
		redisid=RedisQueueConnectionPool.getInstance().getRedisId(queuename,isRetry,logmap);

		
		
		
		if(redisid!=null){
			
		
				result=new RedisWrite().lpushtoQueue(RedisQueueConnectionPool.getInstance().getPool(redisid,queuename),queuename ,requestObject ) ;
		}
		
		if(!result) {

			if(!isRetry){
			result=new Insert().insert(queuename,requestObject,username,msgid,scheduletime,smscid );
			logmap.put("queue type","mysql");

			}
		}else{
			
			logmap.put("queue type","redis");

		}
		

		long end=System.currentTimeMillis();
		


		return result;
				
				
		
		}





	

	

}
