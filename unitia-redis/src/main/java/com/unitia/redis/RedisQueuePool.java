package com.unitia.redis;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.apache.kafka.common.config.ConfigDef.ConfigKey;

import com.itextos.beacon.commonlib.commondbpool.DBDataSourceFactory;
import com.itextos.beacon.commonlib.commondbpool.DatabaseSchema;
import com.itextos.beacon.commonlib.commondbpool.JndiInfoHolder;
import com.unitia.util.db.Close;
import com.unitia.util.db.CoreDBConnection;
import com.unitia.util.db.TableExsists;
import com.unitia.util.misc.ConfigParams;
import com.unitia.util.misc.Prop;
import com.unitia.util.queue.RedisQueue;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;


public class RedisQueuePool {


	private JedisPool pool=null;
	
	private Set<String> unavailablequeue=new HashSet<String>();
	
	private Set<String> unavailableretryqueue=new HashSet<String>();
	


	
	private Map<String, String> queueCount=new HashMap<String, String>();
	
	private Map<String, String> queuemaxCount=new HashMap<String, String>();

	
	private String redisid=null;
		
	public RedisQueuePool(String redisid) {


		this.redisid=redisid;
		createPool();
		checkQueueTableAvailable();
		reload();

	}
	
	
	public RedisQueuePool(String redisid,Properties redisproperties) {


		this.redisid=redisid;
		createPool(redisproperties);
		checkQueueTableAvailable();
		reload();

	}
	

	public Map<String, String> getQueueCount() {
		return queueCount;
	}



	private void checkQueueTableAvailable() {

		Connection connection=null;
		
		try{
			connection=CoreDBConnection.getInstance().getConnection();
			TableExsists table=new TableExsists();
			
			if(!table.isExsists(connection, "queue_count_redis")){
				
				table.create(connection, "create table queue_count_redis(queuename varchar(50),count numeric(10,0),updatetime numeric(13,0),redisid varchar(25),unique(redisid,queuename)  )", false);
			}
			
			
			if(!table.isExsists(connection, "queue_max_redis")){
				
				table.create(connection, "create table queue_max_redis(queuename varchar(50),count numeric(10,0),updatetime numeric(13,0),redisid varchar(25),unique(redisid,queuename)  )", false);
			}
		}catch(Exception e){
			 e.printStackTrace();
		}finally{
		
			Close.close(connection);
		}
		
	}

	public void reload() {
		

		setQueueCountMap();
		
		insertQueueintoDB();
		
		setQueueMaxCountMap();
		
		insertQueueMaxintoDB();

	}
	
		
	private void insertQueueMaxintoDB() {
		

		
		Connection connection=null;
		
		try
		
		{
			

			
			connection=CoreDBConnection.getInstance().getConnection();
			Iterator itr=queuemaxCount.keySet().iterator();
			
			while(itr.hasNext()){
				
				String queuename=itr.next().toString();
				String count=queuemaxCount.get(queuename);
				if(updateMaxDB(connection, queuename, count)<1){
					
					insertQueueMaxintoDB(connection, queuename, count);
				}

				
			}
			
		
		}catch(Exception e){
			e.printStackTrace();
		}finally{
			
			Close.close(connection);
		}
	
		
	}



	private void insertQueueMaxintoDB(Connection connection, String queuename, String count) {
		

		

			PreparedStatement insert=null;
			
			try
			
			{
							long updatetime=System.currentTimeMillis();
				
				insert=connection.prepareStatement("insert into queue_max_redis(queuename,count ,updatetime,redisid ) values(?,?,?,?)");
				insert.setString(1, queuename);
				insert.setString(2, count);
				insert.setString(3, ""+updatetime);
				insert.setString(4, redisid);

				insert.execute();
				
			}catch(Exception e){
				e.printStackTrace();
			}finally{
				Close.close(insert);
			}
		
		
	}



	private int updateMaxDB(Connection connection, String queuename, String count) {
		

		PreparedStatement update=null;
		
		try
		
		{
			
			long updatetime=System.currentTimeMillis();
			
			update=connection.prepareStatement("update queue_max_redis set count=?,updatetime=? where queuename=? and redisid=?");

			update.setString(1, count);
			update.setString(2, ""+updatetime);
			update.setString(3, ""+queuename);
			update.setString(4, redisid);
			return update.executeUpdate();
		}catch(Exception e){
			e.printStackTrace();
		}finally{
			Close.close(update);
		}
		
		return 0;
	}



	private void setQueueMaxCountMap() {
		
		Iterator itr=queueCount.keySet().iterator();
		
		while(itr.hasNext()){
			
			String queuename=itr.next().toString();
			
			String count=queueCount.get(queuename);
			
			String maxcount=queuemaxCount.get(queuename);
			
			if(maxcount==null){
				
				queuemaxCount.put(queuename, count);
				
				continue;
			}
			
			try{
				int countInt=Integer.parseInt(count);
				
				int maxcountInt=Integer.parseInt(maxcount);
				
				if(countInt>maxcountInt){
					
					queuemaxCount.put(queuename, count);
					
				}
			}catch(Exception e){
				
			}
		}
		
	}

	
	private void createPool(Properties prop) {
			pool= new RedisPool().createJedisPool(prop);
	
	}
	


	private void createPool() {
		
		if(redisid.equals("redisqueue1")){
			pool= new RedisPool().createJedisPool(Prop.getInstance().getRedisQueue1Prop());
	
		}
		
	}
	
	public JedisPool getPool() {
		
		return pool;
	}
	public long getQueueCount(String queuename) {
		if(queueCount.containsKey(queuename)) {
		return Long.parseLong(queueCount.get(queuename));
		}else {
			
		return 0;	
		}
		
	}

	

	private boolean isAvailable() {
		Jedis jedis=null;
	try {
	     jedis = pool.getResource();
	    return true;
	} catch (Exception e) {
	    e.printStackTrace();
	}finally {
		try {
			
			jedis.close();
			
		}catch(Exception ignore) {
			
		}
	}
	
	return false;
	}
	
	
	private Set<String> getKey(JedisPool pool) {
		Jedis jedis=null;
	
		try{
	    
			jedis = pool.getResource();

			return jedis.keys("*");
	     
			}catch (Exception e) {
			}finally {
	

		try {
			
			jedis.close();
			
		}catch(Exception ignore) {
			
		}
	}
	
	
	return null;
	}
	

	private void setQueueCountMap() {

		if(isAvailable()) {
		Set<String> keys=getKey(pool);
		
		Map<String,String> r=getQueueFromDB();
		add(r,keys);
		this.queueCount=r;
		
		}else {
			
			this.queueCount=new HashMap();
		}
		

	}


	
	private void add(Map<String, String> r,Set<String> keys) {
		Map<String,String> map=getQueueMap(pool,keys);
		Iterator itr=map.keySet().iterator();
		
		while(itr.hasNext()){
			String key=itr.next().toString();
			String value=map.get(key);
			r.put(key, value);
			
		}

		
	}



	private Map<String, String> getQueueFromDB() {

		 Map<String, String> result=new HashMap<String,String>();
		 
		 Connection connection=null;
		 PreparedStatement statement=null;
		 ResultSet resultset=null;
		 try{
             connection=CoreDBConnection.getInstance().getConnection();
			 statement=connection.prepareStatement("select queuename from queue_count_redis");
			 resultset=statement.executeQuery();
			 
			 while(resultset.next()){
				 
				 result.put(resultset.getString("queuename"), "0");
			 }
		 }catch(Exception e){
			 e.printStackTrace();
		 }finally{
			 
			 Close.close(resultset);
			 Close.close(statement);
			 Close.close(connection);
		 }

		 return result;
	}



	private long getCount(JedisPool pool, String queuename) {
		Jedis jedis=null;
	try {
	     jedis = pool.getResource();
	     
	     return jedis.llen(queuename);

	} catch (Exception e) {
	    // Not connected
	}finally {
		try {
			
			jedis.close();
			
		}catch(Exception ignore) {
			
		}
	}
	
	return 0;
	
	}

	private Map<String, String> getQueueMap(JedisPool pool, Set<String> keys) {

		Map<String, String> queuemap=new HashMap<String, String>();
	
		Set<String> unavailablequeue=new HashSet<String>();
		
		Set<String> unavailableretryqueue=new HashSet<String>();

		long totalqueue=0L;
		
		if(keys!=null) {
			
			Iterator itr=keys.iterator();
			
			while(itr.hasNext()) {
				
				String queuename=itr.next().toString();
				long count= getCount(pool,queuename);
				
				
						
				if(isUnavilable(queuename,count)){
					unavailablequeue.add(queuename);
				}
				if(isUnavilableRetry(queuename,count)){
					unavailableretryqueue.add(queuename);
				}
				totalqueue+=count;
				queuemap.put(queuename, Long.toString(count));
			}
		}
		
		this.unavailablequeue=unavailablequeue;
		this.unavailableretryqueue=unavailableretryqueue;
		queuemap.put("allqueuecount", Long.toString(totalqueue));
		this.queueCount=queuemap;
		return queuemap;
	}
	
	private boolean isUnavilableRetry(String queuename, long count) {

		long maxcount=getMaxCount(queuename);
		
		if(queuename.startsWith("smppdn_")){
			
			maxcount+=100;
		}else{
			
			maxcount+=1000;

		}
		
		if(maxcount>count){
			
			return false;
		}
		return true;
	}



	private boolean isUnavilable(String queuename, long count) {
	
		long maxcount=getMaxCount(queuename);
		
		if(maxcount>count){
			
			return false;
		}
		return true;
	}



	private long getMaxCount(String queuename) {
		
		try{
			
			if(("commonpool").equals(queuename)){
				
		//	 return Long.parseLong(ConfigParams.getInstance().getProperty(ConfigKey.MAX_COMMONPOOL));
			
			}
			
			
		}catch(Exception e){
			
		}
		return 5000;
	}



	public boolean isAvailableQueue(String redisid,String queuename,boolean isRetry,Map<String,Object> logmap) {
		

		return !RedisQueue.getInstance().isQueued(redisid, queuename, isRetry, logmap);

		
	}

	
	public void insertQueueintoDB() {
		
		Connection connection=null;
		
		try
		
		{
			

			
			connection =CoreDBConnection.getInstance().getConnection();
			Iterator itr=queueCount.keySet().iterator();
			
			while(itr.hasNext()){
				
				String queuename=itr.next().toString();
				String count=queueCount.get(queuename);
				if(updateDB(connection, queuename, count)<1){
					
					insertQueueintoDB(connection, queuename, count);
				}

				
			}
			
			
			
		}catch(Exception e){
			e.printStackTrace();
		}finally{
			
			Close.close(connection);
		}
	}

public void insertQueueintoDB(Connection connection,String queuename,String count) {
		

		PreparedStatement insert=null;
		
		try
		
		{
						long updatetime=System.currentTimeMillis();
			
			insert=connection.prepareStatement("insert into queue_count_redis(queuename,count ,updatetime,redisid ) values(?,?,?,?)");
			insert.setString(1, queuename);
			insert.setString(2, count);
			insert.setString(3, ""+updatetime);
			insert.setString(4, redisid);

			insert.execute();
			
		}catch(Exception e){
			e.printStackTrace();
		}finally{
			Close.close(insert);
		}
	}

	
	public int updateDB(Connection connection,String queuename,String count) {
		

		PreparedStatement update=null;
		
		try
		
		{
			
			long updatetime=System.currentTimeMillis();
			
			update=connection.prepareStatement("update queue_count_redis set count=?,updatetime=? where queuename=? and redisid=?");

			update.setString(1, count);
			update.setString(2, ""+updatetime);
			update.setString(3, ""+queuename);
			update.setString(4, redisid);
			return update.executeUpdate();
		}catch(Exception e){
			e.printStackTrace();
		}finally{
			Close.close(update);
		}
		
		return 0;
	}



	public String getRedisId() {
		
		return redisid;
		
	}



	public boolean isAvailableForReadingQueue(String queuename) {
		
		return queueCount.containsKey(queuename);
	}
	
	
	public void print(){
		
		
		Map<String,Object> logmap=new HashMap<String,Object>();
		logmap.put("username", "sys");
		logmap.put("logname", "redisqueue");
		logmap.put("unavailablequeue", unavailablequeue);
		logmap.put("unavailableretryqueue", unavailableretryqueue);

		logmap.putAll(queueCount);

		
	}
}
