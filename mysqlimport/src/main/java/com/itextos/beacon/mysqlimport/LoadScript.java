package com.itextos.beacon.mysqlimport;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class LoadScript {

	public static Map<String,Map<String,String>> scriptmap=new HashMap<String,Map<String,String>>();
	
	
	static {
		

        // Set the directory path
        String folderPath = "/script/mysql";  // Change this to your folder path

        File directory = new File(folderPath);

        if (directory.exists() && directory.isDirectory()) {
        	
            File[] items = directory.listFiles();

            if (items != null) {
                for (File item : items) {
                    if (item.isDirectory()) {

                        String schema=item.getName();
                        
                        Map<String,String> scriptfilemap=scriptmap.get(schema);
                        
                        if(scriptfilemap==null) {
                        
                        	scriptfilemap =new HashMap<String,String>();
                        	
                        	scriptmap.put(schema, scriptfilemap);
                        }
                        
                        // List files inside this subfolder
                        File[] subFiles = item.listFiles();
                        if (subFiles != null) {
                            for (File file : subFiles) {
                                if (file.isFile()) {
                                    
                                    String fileName=file.getAbsolutePath();
                                    String tablename=file.getName().substring(0, file.getName().indexOf("."));
                                    scriptfilemap.put(tablename, fileName);
                                }
                            }
                        }
                    } else if (item.isFile()) {
                        System.out.println("📄 File in root: " + item.getName());
                    }
                }
            }
        } else {
            System.out.println("The path is not a valid directory.");
        }
    }
			
	
}
