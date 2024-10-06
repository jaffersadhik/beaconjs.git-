package com.itextos.beacon.smslog;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;


public class KafkaSender {


    private  final  Logger logger = Logger.getLogger(KafkaSender.class.getName());

	private static Map<String,KafkaSender> objmap=new HashMap<String,KafkaSender>();
	
	
	
	public static KafkaSender getInstance(String nextcomponent) {
	
		
		KafkaSender obj=objmap.get(nextcomponent);
		
		if(obj==null) {
			
			obj=new KafkaSender(nextcomponent);
			
			objmap.put(nextcomponent, obj);
		}
		
		
		return obj;
	}
	
	
	private KafkaSender() {
		
	}
	
	private KafkaSender(String nextcomponent) {
		

    	
        int limit = 1024 * 1024*5; // 1 MB file size limit
        int count = 1; // N

        String logFileNamePattern = "/logs/kafkasender/kafkasender_"+nextcomponent+".%g.log";

        Level loglevel=Level.INFO;
        
        String loglevelFromEnr=System.getenv("loglevel");
        
        if(loglevelFromEnr!=null) {
        
        	if(loglevelFromEnr.equals("all")) {
        		
        		loglevel=Level.ALL;
        		
        	}else if(loglevelFromEnr.equals("off")) {
        		
        		loglevel=Level.OFF;
        	}
        }
        
        // Create a FileHandler with the specified log file name
        FileHandler fileHandler=null;
		try {
			fileHandler = new FileHandler(logFileNamePattern, limit, count, true);
			
			   // Set the logging level for the handler
	        fileHandler.setLevel(loglevel);

	        // Set a formatter for the handler (optional)
	        fileHandler.setFormatter(new SimpleFormatter());

	        // Add the handler to the logger
	        logger.addHandler(fileHandler);
	        
	        logger.setLevel(loglevel);

		} catch (SecurityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

     

        // Set the logging level for the logger
    
	}

    public void log(String string) {

    	logger.info(string);
    	
    }
}
