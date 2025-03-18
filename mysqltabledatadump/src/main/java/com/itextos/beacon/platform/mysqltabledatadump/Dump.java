package com.itextos.beacon.platform.mysqltabledatadump;

import java.io.File;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.itextos.beacon.commonlib.commondbpool.DBDataSourceFactory;
import com.itextos.beacon.commonlib.commondbpool.DatabaseSchema;
import com.itextos.beacon.commonlib.commondbpool.JndiInfoHolder;

public class Dump {

	private static String FILE_PATH="/mysqldump/";
	
	 public static void foldercreaton(String folderPath) {
	        

	        // Create a File object representing the directory
	        File folder = new File(folderPath);

	        // Check if the directory exists
	        if (!folder.exists()) {
	            // Attempt to create the directory
	            if (folder.mkdirs()) {
	                System.out.println("Directory created successfully: " + folderPath);
	            } else {
	                System.out.println("Failed to create directory: " + folderPath);
	            }
	        } else {
	            System.out.println("Directory already exists: " + folderPath);
	        }
	    }
	 
	public static void takedump(String schemaname,Set<String> tableset) {
		
		

		String foldername=getFoldername(schemaname);

        foldercreaton(foldername);
        
        Connection connection=null;
        try {
        	
        	Iterator<String> itr=tableset.iterator();
        	
        	while(itr.hasNext()) {
        		
        		String tablename=itr.next().toString();
        		
        		String filename=foldername+"/"+tablename.trim()+".ser";
        		
        		connection=getConnection();
        		
        		takedump(connection,filename,schemaname+"."+tablename);
        	}
        
        }catch(Exception e) {
        	
        }finally {
        	
        	if(connection!=null) {
        		
        		try {
					connection.close();
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
        		
        	}
        }

	}

	

	private static Connection getConnection() {
		
		try {
			return DBDataSourceFactory.getConnection(JndiInfoHolder.getJndiInfoUsingName(DatabaseSchema.CONFIGURATION.getKey()));
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			
			return null;
		}
	}

	private static void takedump(Connection connection, String filename, String tablename) {
		
 		 Statement stmt = null;
         ResultSet rs = null;

         try {
        	  stmt = connection.createStatement();
              rs = stmt.executeQuery("SELECT * FROM "+tablename);
              List<Map<String, Object>> data = ResultSetToCollection.resultSetToList(rs);
              CollectionToFile.saveCollection(data, filename);
         }catch(Exception e){
        	 
         }finally {
        	 
        	 try {
				stmt.close();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
        	 try {
				rs.close();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
         }
	
		
	}

	private static String getFoldername(String schemaname) {
		
		SimpleDateFormat year = new SimpleDateFormat("yyyy"); // Customize format as needed
		SimpleDateFormat month = new SimpleDateFormat("MM"); // Customize format as needed
		SimpleDateFormat day = new SimpleDateFormat("dd"); // Customize format as needed

        Date date = new Date();
        String yearStr = year.format(date);
        String monthStr = month.format(date);
        String dayStr = day.format(date);
        
        String foldername=FILE_PATH+"/"+yearStr+"/"+monthStr+"/"+dayStr+"/"+schemaname;
         return foldername;
		
	}
	
	
	
	
	
}
