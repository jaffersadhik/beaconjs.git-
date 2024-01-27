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

	
	public static Connection getConnection(DataSourceConfig dataSourceConfig){
		Connection con=null;
		try{
			
			Properties prop=dataSourceConfig.getConfigAsProperties();
			log.debug("prop : "+prop);
	//		prop.setProperty("password", Encryptor.getDecryptedDbPassword(prop.getProperty("password")));
		Class.forName(prop.getProperty("driverClassName")); 
		con=DriverManager.getConnection(  
				prop.getProperty("url"),prop.getProperty("username"),prop.getProperty("password"));
	    return con;
		
		}catch(Exception e){
			
			return null;

		}
	}
	public static Connection getConnection(Properties prop) throws Exception {
		Connection con=null;
		try{
			
			
		Class.forName(prop.getProperty("driverClassName")); 
		con=DriverManager.getConnection(  
				prop.getProperty("url"),prop.getProperty("username"),prop.getProperty("password"));
	    return con;
		
		}catch(Exception e){
			
			throw e;

		}
	}

}
