package com.itextos.beacon.dnk2e;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.itextos.beacon.commonlib.constants.Component;

public class StartApplication
{

    private static final Log log = LogFactory.getLog(StartApplication.class);

    public static void main(
            String[] args)
    {
       
    	System.setProperty("kafka.2.elasticsearch.config.file", "/kafka2es_dn.properties_"+System.getenv("profile"));
    	
    	List<String> topiclist=getTopicList();
    	
    	if(topiclist!=null) {
    		
			com.itextos.beacon.kafkabackend.kafka2elasticsearch.start.StartApplication.main("deliveries",topiclist);

    	}
    }

	

	private static List<String> getTopicList() {
		
		String priority=System.getenv("priority");
		
		List<String> result=new ArrayList<String>();
	
		if(priority!=null&&priority.trim().length()>0) {
			
			StringTokenizer st=new StringTokenizer(priority,",");
			while(st.hasMoreTokens()) {
				
				String token=st.nextToken();
				
				if(token.equals("default")) {
					result.add(Component.T2DB_DELIVERIES.getKey());
				}else {
					result.add(Component.T2DB_DELIVERIES.getKey()+"-"+token);

				}
				
			}
			
			return result;
			
		}else {
			System.err.print("no priority find");
			System.exit(0);
		}
		
		return null;
	}

}
