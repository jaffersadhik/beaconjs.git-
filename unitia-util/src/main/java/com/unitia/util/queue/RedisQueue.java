package com.unitia.util.queue;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.unitia.util.db.Close;
import com.unitia.util.db.CoreDBConnection;

public class RedisQueue {

	private static long PANIC_1=System.currentTimeMillis();
	 private static String MODE="";
		
		static {
			
			String mode=System.getenv("mode");
			
			if(mode==null||mode.trim().length()<1){
				
				MODE="production";
			}else{
				
				MODE=mode;
			}
			
			

		}
		
	private static String SQL="select queuename,count,redisid from queue_count_redis";
	
	private static RedisQueue obj=new RedisQueue();
	
	private  Map<String,Map<String,String>> redisqueuemap=new HashMap<String,Map<String,String>>();

	
	private RedisQueue(){
		
	}
	
	public static RedisQueue getInstance(){
		
		if(obj==null){
			
			obj=new RedisQueue();
		}
		
		return obj;
	}
	
	public void reload(){
		
		Map<String,Map<String,String>> temp=getMap();
		
		if(temp!=null){
			
			redisqueuemap=temp;
		}
		
	}

	private Map<String, Map<String, String>> getMap() {
		
		Map<String,Map<String,String>> kannelmap=new HashMap<String,Map<String,String>>();
		Connection connection=null;
		PreparedStatement statement=null;
		ResultSet resultset=null;
		
		try{
			connection=CoreDBConnection.getInstance().getConnection();
			statement=connection.prepareStatement(SQL);
			resultset=statement.executeQuery();
			while(resultset.next()){
				
				String smscid=resultset.getString("queuename");
				String count=resultset.getString("count");
				String redisid=resultset.getString("redisid");

				Map<String,String> smscidmap=kannelmap.get(redisid);
				
				if(smscidmap==null){
					smscidmap=new HashMap();
					kannelmap.put(redisid, smscidmap);
				}
				
				smscidmap.put(smscid, count);
			}
		}catch(Exception e){
			e.printStackTrace();
			return null;
		}finally{
			Close.close(resultset);
			Close.close(statement);
			Close.close(connection);
		}
		return kannelmap;
	}

	public boolean isQueued(String redisid,String queuename,boolean isRetry,Map<String,Object> logmap){
	
		logmap.put("nextlevelqueuename", queuename);
		logmap.put("redisid", redisid);
		Map<String,String> map=redisqueuemap.get(redisid);
		
		if(map==null){
			
			logmap.put("redisid failed","queue not captured for the redisid");

			if((System.currentTimeMillis()-PANIC_1)>30000){
			Map<String,Object> panicmap=new HashMap<String,Object>();
			panicmap.put("username", "sys");
			panicmap.put("logname", "panicmessage");
			panicmap.put("redisid", redisid);
			panicmap.put("queuename", queuename);
			panicmap.put("queuename with mode",MODE+"_"+queuename);

			panicmap.put("isRetry", isRetry);
			panicmap.put("module", System.getenv("module"));
			panicmap.put("message", "redis not found for this redisid");
	        PANIC_1=System.currentTimeMillis();
			}
			return false;
		}
		
		
		String count=map.get(MODE+"_"+queuename);
		
		logmap.put(queuename + " queue size", count);

		int size=0;
		
		try{
			
			size=Integer.parseInt(count);
			
		}catch(Exception e){
			
			if((System.currentTimeMillis()-PANIC_1)>30000){
				Map<String,Object> panicmap=new HashMap<String,Object>();
				panicmap.put("username", "sys");
				panicmap.put("logname", "panicmessage");
				panicmap.put("redisid", redisid);
				panicmap.put("queuename", queuename);
				panicmap.put("queuename with mode",MODE+"_"+queuename);

				panicmap.put("isRetry", isRetry);
				panicmap.put("module", System.getenv("module"));
				panicmap.put("message", "queue not found, may be qsize is zero or invalid queue name");
		        PANIC_1=System.currentTimeMillis();
				}
		}
		
		if(queuename.startsWith("smpp")){
			
			
			if(isRetry){
				
				if(size>20000){
							
					return true;
				}else{
					
					return false;
					
				}
			}else{
				
				
				if(size>19000){
					
					return true;
					
				}else{
					
					return false;
				}
			}
		}else{
			
			if(isRetry){
				
				if(size>21000){
							
					return true;
				}else{
					
					return false;
					
				}
			}else{
				
				
				if(size>20000){
					
					return true;
					
				}else{
					
					return false;
				}
			}
		}
	}
	
	
	
	public Map<String,List<String>> getSmppQueue(Map<String,Object> logmap){
		
		Map<String,List<String>> result=new HashMap<String,List<String>>();
		
		Iterator<String> itr=redisqueuemap.keySet().iterator();
		
		logmap.put("redisqueuemap", redisqueuemap);
		
		while(itr.hasNext()){
			
			String redisid=itr.next();
			
			Map<String,String> queuemap=redisqueuemap.get(redisid);
			
			Iterator<String> itr2=queuemap.keySet().iterator();
			
			while(itr2.hasNext()){
				
				String queuename=itr2.next();

				if(queuename.indexOf("_smppdn_")>-1){
					
					
					if(isQueued(queuemap,queuename)){
						
						List<String> list=result.get(redisid);
						
						if(list==null){
							list=new ArrayList<String>();
							result.put(redisid, list);
						}
						
						list.add(queuename.replaceAll(MODE+"_", ""));
					}
				}
			}
			
		}
	
		return result;
	}

	private boolean isQueued(Map<String, String> queuemap, String queuename) {
		String count=queuemap.get(queuename);

		try{
			long c=Long.parseLong(count);
			
			if(c>0){
				
				return true;
			}
		}catch(Exception e){
			
		}
		
		return false;
	}
}
