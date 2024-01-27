package com.itextos.beacon.commonlib.commondbpool;

import java.sql.Connection;
import java.sql.DriverManager;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.itextos.beacon.commonlib.pwdencryption.Encryptor;
import com.itextos.beacon.commonlib.utility.CommonUtility;

public class MysqlThinConnection {
	
    private static final Log log = LogFactory.getLog(MysqlThinConnection.class);

	private static Properties masterdbproperties=null;
	
	static {
		
		try {
			Class.forName("org.mariadb.jdbc.Driver");
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 

	}
	public static Connection getConnection(JndiInfo aDBConID){
		Connection con=null;
		try{
	        waitForPropertiesLoad();

	    String url=masterdbproperties.getProperty("url"); 
	    
	    if(aDBConID.getId()==1) {
	    	url+="configuration";
	    }else if(aDBConID.getId()==2) {
	    	url+="accounts";

	    }else if(aDBConID.getId()==3) {
	    	url+="listing";

	    }else if(aDBConID.getId()==4) {
	    	url+="carrier_handover";

	    }else if(aDBConID.getId()==5)  {
	    	
	    	return DBDataSourceFactory.getConnection(JndiInfoHolder.getInstance().getJndiInfoUsingName(DatabaseSchema.PAYLOAD.getKey()));
	    }else if(aDBConID.getId()==7)  {
	    	
	    	return DBDataSourceFactory.getConnection(JndiInfoHolder.getInstance().getJndiInfoUsingName(DatabaseSchema.MESSAGING.getKey()));
	    }else if(aDBConID.getId()==8)  {
	    	
	    	return DBDataSourceFactory.getConnection(JndiInfoHolder.getInstance().getJndiInfoUsingName(DatabaseSchema.LOGGING.getKey()));
	    }else if(aDBConID.getId()==9) {
	    	return DBDataSourceFactory.getConnection(JndiInfoHolder.getInstance().getJndiInfoUsingName(DatabaseSchema.CLIENT_HANDOVER.getKey()));

	    }else if(aDBConID.getId()==10)  {
	    	
	    	return DBDataSourceFactory.getConnection(JndiInfoHolder.getInstance().getJndiInfoUsingName(DatabaseSchema.R3C.getKey()));
	    }
		con=DriverManager.getConnection(  
				url,masterdbproperties.getProperty("username"),masterdbproperties.getProperty("password"));
	    return con;
		
		}catch(Exception e){
			
			return null;

		}
	}
	private static void waitForPropertiesLoad() {
		
		  while (masterdbproperties==null)
	        {
	            if (log.isDebugEnabled())
	                log.debug("Waiting for Properties load to complete.");

	            CommonUtility.sleepForAWhile(1);
	        }
		
	}
	public static Connection getConnection(Properties prop) throws Exception {
		Connection con=null;
		try{
			
			 String url=prop.getProperty("url")+"sysconfig";
		masterdbproperties=prop;
		con=DriverManager.getConnection(  
				url,prop.getProperty("username"),prop.getProperty("password"));
	    return con;
		
		}catch(Exception e){
			
			throw e;

		}
	}

}
