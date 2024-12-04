package com.itextos.beacon.subk2e;

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
       
    	System.setProperty("kafka.2.elasticsearch.config.file", "/kafka2es_sub.properties_"+System.getenv("profile"));
    	
    	List<String> topiclist=getTopicList();
    	
    	if(topiclist!=null) {
    		
    		for(int i=0;i<topiclist.size();i++) {
    			
    			String arsg[]=getArgs(topiclist.get(i));
    			
    			com.itextos.beacon.kafkabackend.kafka2elasticsearch.start.StartApplication.main(args);
    			
    		}
    	}
    }

	private static String[] getArgs(String topicname) {
		
		
		String args[]=new String[3];
		args[0]="submission";
		args[1]=topicname;
		args[2]="2";
		args[3]="2";
		return args;
	}

	private static List<String> getTopicList() {
		
		String priority=System.getenv("priority");
		
		List<String> result=new ArrayList<String>();
	
		if(priority!=null&&priority.trim().length()>0) {
			
			StringTokenizer st=new StringTokenizer(priority,",");
			while(st.hasMoreTokens()) {
				
				String token=st.nextToken();
				
				if(token.equals("default")) {
					result.add(Component.T2DB_SUBMISSION.getKey());
				}else {
					result.add(Component.T2DB_SUBMISSION.getKey()+"-"+token);

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
