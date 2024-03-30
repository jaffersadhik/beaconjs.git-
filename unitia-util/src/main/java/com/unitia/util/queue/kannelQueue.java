package com.unitia.util.queue;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.Map;

import com.itextos.beacon.commonlib.commondbpool.DBDataSourceFactory;
import com.itextos.beacon.commonlib.commondbpool.DatabaseSchema;
import com.itextos.beacon.commonlib.commondbpool.JndiInfoHolder;
import com.unitia.util.db.Close;
import com.unitia.util.db.CoreDBConnection;
import com.unitia.util.db.TableExsists;

public class kannelQueue {

	static kannelQueue obj=new kannelQueue();
	Map<String,Map<String,String>> smscqueue=new HashMap<String,Map<String,String>>();
	
	Map<String,String> queuecountmap=new HashMap<String,String>();
	
	private void init(){
		

			Connection connection=null;
			
			try{
				connection=CoreDBConnection.getInstance().getConnection();
				TableExsists table=new TableExsists();
				
				if(!table.isExsists(connection, "queue_count_smscid")){
					
					table.create(connection, "create table queue_count_smscid(smscid varchar(50),queuecount numeric(10,0),status varchar(20),updatetime numeric(13,0))", false);
				}
				
				
				
			}catch(Exception e){
				 e.printStackTrace();
			}finally{
			
				Close.close(connection);
			}
			
		
	}
	
	private kannelQueue(){
		init();
		reload();
	}
	
	public static kannelQueue getInstance(){
		
		if(obj==null){
			
			obj=new kannelQueue();
		}
		
		return obj;
	}
	public void reload() {
		
		smscqueue=getQueueFromDB();
	}
	
	
	public  Map<String,Map<String,String>> getQueueFromDB() {
		
		Connection connection=null;
		PreparedStatement select=null;
        ResultSet resultset=null;
    	Map<String,Map<String,String>> result=new HashMap<String,Map<String,String>>();

		try
		
		{
			
			queuecountmap=new HashMap<String,String>();
			
			connection=CoreDBConnection.getInstance().getConnection();
			select=connection.prepareStatement("select smscid,queuecount,status from queue_count_smscid");
            resultset=select.executeQuery();
			

			while(resultset.next()){
				
				String smscid=resultset.getString("smscid");
				String queuecount=resultset.getString("queuecount");
				String status=resultset.getString("status");
				Map<String,String> data=new HashMap<String,String>();
				data.put("queued", queuecount);
				data.put("status", status);

				result.put(smscid, data);
				
				try{
					if(Integer.parseInt(queuecount)>0){
						queuecountmap.put(smscid, queuecount);
						
					}
				}catch(Exception e){
					
				}
			}
			
			
		
		}catch(Exception e){
			e.printStackTrace();
		}finally{
			
			Close.close(resultset);
			Close.close(select);
			Close.close(connection);
		}
		
		return result;
	}
	 
	 
	 public  boolean isQueuedForRouter(String smscid,Map<String,Object> msgmap){
		 
		 Map<String,String> data=smscqueue.get(smscid);
		 
		 if(data==null){
			 
			 msgmap.put("isQueuedForRouter data", "null");
			 return false;
		 }
		 
		 String status=data.get("status");
		 
		 if(status==null){
			 
			 msgmap.put("isQueuedForRouter status", "null");

			 return false;
		 }
		 
		 
		 if(status.equals("down")){
			 
			 msgmap.put("isQueuedForRouter status", "down");

			 return true;
		 }
		 
		 
		 String queued=data.get("queued");
		 
		 if(queued==null){
			 
			 msgmap.put("isQueuedForRouter queued", "null");

			 return false;
		 }
		 
		 
		 String maxqueue=SMSCMaxQueue.getInstance().getQueue(smscid);
		 
		 msgmap.put("isQueuedForRouter maxqueue", maxqueue);

		 long lMaxQueue=Long.parseLong(maxqueue);
		 
		 long lQueued=Long.parseLong(queued);
		 
		 msgmap.put("isQueuedForRouter queued", queued);

		 return lQueued>lMaxQueue;
	 }
	 
	 public  boolean isQueued(String smscid,boolean isPoller){
		 
		 Map<String,String> data=smscqueue.get(smscid);
		 
		 if(data==null){
			 
			 return false;
		 }
		 
		 String status=data.get("status");
		 
		 if(status==null){
			 
			 return false;
		 }
		 
		 
		 if(status.equals("down")){
			 
			 return true;
		 }
		 
		 
		 String queued=data.get("queued");
		 
		 if(queued==null){
			 
			 return false;
		 }
		 
		 
		 String maxqueue=SMSCMaxQueue.getInstance().getQueue(smscid);
		 
		 long lMaxQueue=Long.parseLong(maxqueue);
		 
		 if(isPoller){
			 lMaxQueue+=40;
		 }
		 
		 long lQueued=Long.parseLong(queued);
		 
		 return lQueued>lMaxQueue;
	 }
	 
	 
	
}

