package com.itextos.beacon.commonlib.commondbpool;

import java.sql.Connection;
import java.sql.DriverManager;
import java.util.Iterator;
import java.util.Properties;

import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.itextos.beacon.commonlib.commonpropertyloader.PropertiesPath;
import com.itextos.beacon.commonlib.commonpropertyloader.PropertyLoader;
import com.itextos.beacon.commonlib.constants.exception.ItextosRuntimeException;
import com.itextos.beacon.commonlib.pwdencryption.Encryptor;
import com.itextos.beacon.commonlib.utility.CommonUtility;

public class MysqlThinConnection {
	
    private static final Log log = LogFactory.getLog(MysqlThinConnection.class);

	private static Properties masterdbproperties=readProperties();
	
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

	    }else{
	    	
	    	return DBDataSourceFactory.getConnection(aDBConID);
	    }
	    
	    
		con=DriverManager.getConnection(  
				url,masterdbproperties.getProperty("username"),masterdbproperties.getProperty("password"));
	    return con;
		
		}catch(Exception e){
			log.error(e);
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

	   private static Properties readProperties()
	    {

	        try
	        {
	            final PropertiesConfiguration pc = PropertyLoader.getInstance().getPropertiesConfiguration(PropertiesPath.COMMON_DATABASE_PROPERTIES, true);

	            if (pc != null)
	            {
	                final Properties       props   = new Properties();
	                final Iterator<String> keys    = pc.getKeys();
	                String                 currKey = null;

	                while (keys.hasNext())
	                {
	                    currKey = keys.next();
	                    props.setProperty(currKey, pc.getString(currKey));
	                }

	    			props.setProperty("password", Encryptor.getDecryptedDbPassword(props.getProperty("password")));

	                return props;
	            }
	            throw new ItextosRuntimeException("Unable to load the common db properties");
	        }
	        catch (final Exception exp)
	        {
	            log.error("Problem loading property file...", exp);
	            throw new ItextosRuntimeException("Unable to load the common db properties");
	        }
	    }
}
