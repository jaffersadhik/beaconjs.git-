package com.itextos.beacon.mysqlimport;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class PropertyReader {

    private static Properties getProperty(String filename) {
        Properties prop = new Properties();

        try (FileInputStream input = new FileInputStream(filename)) {
            // Load the property file
            prop.load(input);

            return prop;

        } catch (IOException ex) {
            ex.printStackTrace();
        }
        
        return null;
    }
    
    public static Properties getDefaultProperty() {
    	
    	return getProperty("/import_default.properties");
    }
    
    
    public static Properties getAccountsProperty() {
    	
    	return getProperty("/import_accounts.properties");
    }
    
   public static Properties getBillingProperty() {
    	
    	return getProperty("/import_billing.properties");
    }
   
   public static Properties getR3CProperty() {
   	
   	return getProperty("/import_r3c.properties");
   }
   
   public static Properties getListingProperty() {
   	
   	return getProperty("/import_listing.properties");
   }
   
   public static Properties getIMPProperty() {
   	
   	return getProperty("/import_imp.properties");
   }
   
   public static Properties getPayloadProperty() {
   	
   	return getProperty("/import_payload.properties");
   }
   
   public static Properties getMessagingProperty() {
   	
   	return getProperty("/import_messaging.properties");
   }
   
   
   public static Properties getLoggingProperty() {
   	
   	return getProperty("/import_logging.properties");
   }
   
   
   public static Properties getConfigurationProperty() {
   	
   	return getProperty("/import_configuration.properties");
   }
   
   
   public static Properties getPostgresStatisticsPropertyPrimary() {
	   	
	   	return getProperty("/"+System.getenv("")+"_postgresstatistics_primary.properties");
   }
   
   
   public static Properties getPostgresStatisticsPropertySecondary() {
	   	
	   	return getProperty("/"+System.getenv("")+"_postgresstatistics_secondary.properties");
	   }
  
   
   public static Properties getClientHandoverProperty() {
   	
   	return getProperty("/import_client_handover.properties");
   }
   
   
   public static Properties getCarrierHandoverProperty() {
   	
   	return getProperty("/import_carrier_handover.properties");
   }
   
   
   public static Properties getCMProperty() {
   	
   	return getProperty("/import_cm.properties");
   }
   
   
   public static Properties getSysconfigProperty() {
   	
   	return getProperty("/import_sysconfig.properties");
   }
   
   
}
