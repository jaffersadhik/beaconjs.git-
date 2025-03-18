package com.itextos.beacon.platform.redisimport;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.Map;

import com.itextos.beacon.commonlib.constants.ErrorMessage;
import com.itextos.beacon.platform.mysqltabledatadump.CollectionToFile;
import com.itextos.beacon.platform.mysqltabledatadump.Dump;
import com.itextos.beacon.platform.mysqltabledatadump.MysqlDumpLog;

import redis.clients.jedis.Jedis;

public class RedisImport {

    private static final String PREPAID_KEY      = "wallet:amount";

	public static void main(String args[]) {
		
		Map<String, String> data=CollectionToFile.getWalletDetail(getFoldername("wallet")+"/wallet_read.ser");

		putPrepaidData(data);
	}
	
	
	 public  static void putPrepaidData( Map<String, String> data)
	    {

		  
		  Jedis jedis =null;
	        try 
	        {
	        	String jedishost=System.getenv("jedishost");
	        	
	        	if(jedishost==null||jedishost.trim().length()<1) {
	        		
	        		MysqlDumpLog.log("Exit jedishost : "+jedishost);
	        	}
	        	
	        	String jedisPort=System.getenv("jedisport");
	        	
	        	if(jedisPort==null||jedisPort.trim().length()<1) {
	        		
	        		MysqlDumpLog.log("Exit jedisPort : "+jedisPort);
	        	}
	        	
	        	
	        	
	        	String jedisDB=System.getenv("jedisdb");
	        	
	        	if(jedisDB==null||jedisDB.trim().length()<1) {
	        		
	        		MysqlDumpLog.log("Exit jedisPort : "+jedisDB);
	        	}
	        	
	        	 jedis = new Jedis(jedishost,Integer.parseInt(jedisPort));
	        	 
	        	 jedis.select(Integer.parseInt(jedisDB));

	        	 Iterator<String> itr =data.keySet().iterator();
	        	 
	        	 while(itr.hasNext()) {
	        		 
	        		 String key=itr.next();
	        		 String value=data.get(key);
		        	 jedis.hset(PREPAID_KEY, key, value);

	        	 }
	        }
	        catch (final Exception e)
	        {
	        	MysqlDumpLog.log(ErrorMessage.getStackTraceAsString(e));
	        }finally {
	        	
	        	if(jedis!=null) {
	        	jedis.close();
	        	}
	        }
	    }

	 
	 public static String getFoldername(String schemaname) {
		
		SimpleDateFormat year = new SimpleDateFormat("yyyy"); // Customize format as needed
		SimpleDateFormat month = new SimpleDateFormat("MM"); // Customize format as needed
		SimpleDateFormat day = new SimpleDateFormat("dd"); // Customize format as needed

        Date date = new Date();
        String yearStr = year.format(date);
        String monthStr = month.format(date);
        String dayStr = day.format(date);
        
        String foldername="/redisdump/"+yearStr+"/"+monthStr+"/"+dayStr+"/"+schemaname;
        Dump.foldercreaton(foldername);

         return foldername;
		
	}
}
