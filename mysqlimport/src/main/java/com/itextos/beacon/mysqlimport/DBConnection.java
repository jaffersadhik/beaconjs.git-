package com.itextos.beacon.mysqlimport;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public class DBConnection {

    // Database credentials
   

    private static Connection getConnection(Properties prop) {
        Connection conn = null;
        try {
            // Load the JDBC driver (optional for newer versions of Java)
            Class.forName(prop.getProperty("driverClassName"));

            // Get a connection
            conn = DriverManager.getConnection(prop.getProperty("url"), prop.getProperty("username"), prop.getProperty("password"));
            System.out.println("Connection successful!");
        } catch (ClassNotFoundException e) {
            System.out.println("JDBC Driver not found.");
            e.printStackTrace();
        } catch (SQLException e) {
            System.out.println("Database connection failed.");
            e.printStackTrace();
        }
        return conn;
    }

   
    public static Connection getAccountsConnection() {
    	
    	return getConnection(PropertyReader.getAccountsProperty());
    }
    
   public static Connection getClientHanoverConnection() {
    	
    	return getConnection(PropertyReader.getClientHandoverProperty());
    }
   
   public static Connection getCarrierHandoverConnection() {
   	
   	return getConnection(PropertyReader.getCarrierHandoverProperty());
   }
   
   public static Connection getCMConnection() {
   	
   	return getConnection(PropertyReader.getCMProperty());
   }
   
   public static Connection getLoggingConnection() {
   	
   	return getConnection(PropertyReader.getLoggingProperty());
   }
   
   public static Connection getMessagingConnection() {
   	
   	return getConnection(PropertyReader.getMessagingProperty());
   }
   
   public static Connection getPayloadConnection() {
   	
   	return getConnection(PropertyReader.getPayloadProperty());
   }
   
   public static Connection getConfigurationConnection() {
   	
   	return getConnection(PropertyReader.getConfigurationProperty());
   }
   
   public static Connection getIMPConnection() {
   	
   	return getConnection(PropertyReader.getIMPProperty());
   }
   
   public static Connection getListingConnection() {
   	
   	return getConnection(PropertyReader.getListingProperty());
   }
   
   public static Connection getR3CConnection() {
   	
   	return getConnection(PropertyReader.getR3CProperty());
   }
   
   public static Connection getBillingConnection() {
   	
   	return getConnection(PropertyReader.getBillingProperty());
   }
   
   public static Connection getSysconfigConnection() {
   	
   	return getConnection(PropertyReader.getSysconfigProperty());
   }
}

