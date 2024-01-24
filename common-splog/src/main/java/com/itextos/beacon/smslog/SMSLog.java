package com.itextos.beacon.smslog;

import java.io.IOException;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;


public class SMSLog {


    private static final  Logger logger = Logger.getLogger(SMSLog.class.getName());
    
    static {
    	

        String logFileNamePattern = "/logs/smslog.%g.log";

        // Create a FileHandler with the specified log file name
        FileHandler fileHandler=null;
		try {
			fileHandler = new FileHandler(logFileNamePattern, 1000000, 10, true);
			
			   // Set the logging level for the handler
	        fileHandler.setLevel(Level.INFO);

	        // Set a formatter for the handler (optional)
	        fileHandler.setFormatter(new SimpleFormatter());

	        // Add the handler to the logger
	        logger.addHandler(fileHandler);
	        
	        logger.setLevel(Level.INFO);

		} catch (SecurityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

     

        // Set the logging level for the logger
    }

    public static void log(String string) {

    	logger.info(string);
    	
    }
}
