package com.itextos.beacon.platform.mysqltabledatadump;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

public class RedisDump extends Thread{

	
	public void run() {
		
while(true) {
			
			redisDump();
			
			
			gotosleep();
		}
	}
	
	private void redisDump() {
		
		
		Map<String, String> data=RedisData.getPrepaidData();
		
        CollectionToFile.saveCollection(data, getFoldername("wallet")+"/wallet.ser");

        try {
			FolderCompressor.compressFolder(getFoldername("wallet"),"/redisdump/technowizardsredisdump.zip");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
        GitAutomation.pushRedis();
	}


	private void gotosleep() {

	try {
		Thread.sleep(60*1000);
	}catch(Exception e) {

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
