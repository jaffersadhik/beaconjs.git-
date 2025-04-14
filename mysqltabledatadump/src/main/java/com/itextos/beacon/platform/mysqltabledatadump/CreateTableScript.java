package com.itextos.beacon.platform.mysqltabledatadump;

import java.io.File;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.itextos.beacon.commonlib.commondbpool.DBDataSourceFactory;
import com.itextos.beacon.commonlib.commondbpool.DatabaseSchema;
import com.itextos.beacon.commonlib.commondbpool.JndiInfoHolder;
import com.itextos.beacon.commonlib.constants.ErrorMessage;

public class CreateTableScript {

	private static String FILE_PATH="/mysqldump/";
	
	
	public static Map<String,Map<String,String>> TABLESCRIPT=new HashMap<String,Map<String,String>>();
	
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
	 
	public static void takeCreateScript(String schemaname) {
		
		


        
        Connection connection=null;
        try {
        	
    		connection=getConnection(schemaname);
    		
    		List<String> tablenamelist=getTableNameList(connection,schemaname);
    		
    		createScriptPopulate(connection,schemaname,tablenamelist);

        
        }catch(Exception e) {
        	
       	 MysqlDumpLog.log(ErrorMessage.getStackTraceAsString(e));

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

	

	private static void createScriptPopulate(final Connection connection,String schemaname, List<String> tablenamelist) {
		
		tablenamelist.forEach((tablename)->{
			
			Map<String,String> createtablemap=TABLESCRIPT.get(schemaname);
			
			if(createtablemap==null) {
				
				createtablemap=new HashMap<String,String>();
				
				TABLESCRIPT.put(schemaname, createtablemap);
			}
			
			createtablemap.put(tablename, getCreateTableScript(connection,schemaname,tablename));
		});
		
	}

	private static String getCreateTableScript(Connection connection, String schemaname, String tablename) {

	
		
		String sql="show create table "+schemaname+"."+tablename;
		
		
		 Statement stmt = null;
         ResultSet rs = null;

         try {
        	  stmt = connection.createStatement();
              rs = stmt.executeQuery(sql);
              
            if(rs.next()) {
            	 
            	
            		 return rs.getString(2);
            	
             }
              
         }catch(Exception e){
        	 
        	 MysqlDumpLog.log(ErrorMessage.getStackTraceAsString(e));
         }finally {
        	 
        	 try {
        		 if(stmt!=null) {
				stmt.close();
        		 }
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
        	 try {
        		 if(rs!=null) {
				rs.close();
        		 }
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
         }
	
         return null;
	
	}

	private static List<String> getTableNameList(Connection connection,String schemaname) {
	
		
		String sql="SELECT table_schema, table_name FROM information_schema.tables WHERE table_schema NOT IN ('information_schema', 'mysql', 'performance_schema', 'sys')";
		
		List<String> tablenamelist=new ArrayList<String>();
		
		 Statement stmt = null;
         ResultSet rs = null;

         try {
        	  stmt = connection.createStatement();
              rs = stmt.executeQuery(sql);
              
             while(rs.next()) {
            	 
            	 String schema=rs.getString("table_schema");
            	 
            	 if(schema.equals(schemaname)) {
            		 
            		 tablenamelist.add(rs.getString("table_name"));
            	 }
             }
              
         }catch(Exception e){
        	 
        	 MysqlDumpLog.log(ErrorMessage.getStackTraceAsString(e));
         }finally {
        	 
        	 try {
        		 if(stmt!=null) {
				stmt.close();
        		 }
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
        	 try {
        		 if(rs!=null) {
				rs.close();
        		 }
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
         }
	
         return tablenamelist;
	}

	private static Connection getConnection(String schemaname) {
		
		try {
			if(schemaname.equals("configuration")) {
				return DBDataSourceFactory.getConnection(JndiInfoHolder.getJndiInfoUsingName(DatabaseSchema.CONFIGURATION.getKey()));
			}else if(schemaname.equals("accounts")) {
				return DBDataSourceFactory.getConnection(JndiInfoHolder.getJndiInfoUsingName(DatabaseSchema.ACCOUNTS.getKey()));

			}else if(schemaname.equals("carrier_handover")) {
				return DBDataSourceFactory.getConnection(JndiInfoHolder.getJndiInfoUsingName(DatabaseSchema.CARRIER_HANDOVER.getKey()));

			}else if(schemaname.equals("client_handover")) {
				return DBDataSourceFactory.getConnection(JndiInfoHolder.getJndiInfoUsingName(DatabaseSchema.CLIENT_HANDOVER.getKey()));

			}else if(schemaname.equals("cm")) {
				return DBDataSourceFactory.getConnection(JndiInfoHolder.getJndiInfoUsingName(DatabaseSchema.CM.getKey()));

			}else if(schemaname.equals("imp")) {
				return DBDataSourceFactory.getConnection(JndiInfoHolder.getJndiInfoUsingName(DatabaseSchema.CONFIGURATION.getKey()));

			}else if(schemaname.equals("listing")) {
				return DBDataSourceFactory.getConnection(JndiInfoHolder.getJndiInfoUsingName(DatabaseSchema.LISTING.getKey()));

			}else if(schemaname.equals("messaging")) {
				return DBDataSourceFactory.getConnection(JndiInfoHolder.getJndiInfoUsingName(DatabaseSchema.MESSAGING.getKey()));

			}else if(schemaname.equals("logging")) {
				return DBDataSourceFactory.getConnection(JndiInfoHolder.getJndiInfoUsingName(DatabaseSchema.LOGGING.getKey()));

			}else if(schemaname.equals("payload")) {
				return DBDataSourceFactory.getConnection(JndiInfoHolder.getJndiInfoUsingName(DatabaseSchema.PAYLOAD.getKey()));

			}else if(schemaname.equals("r3c")) {
				return DBDataSourceFactory.getConnection(JndiInfoHolder.getJndiInfoUsingName(DatabaseSchema.R3C.getKey()));

			}else if(schemaname.equals("sysconfig")) {
				return DBDataSourceFactory.getConnection(JndiInfoHolder.getJndiInfoUsingName(DatabaseSchema.CONFIGURATION.getKey()));

			}else if(schemaname.equals("billing")) {
				return DBDataSourceFactory.getConnection(JndiInfoHolder.getJndiInfoUsingName(DatabaseSchema.BILLING.getKey()));

			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			
		}
		return null;

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
        	 
        	 MysqlDumpLog.log(ErrorMessage.getStackTraceAsString(e));
         }finally {
        	 
        	 try {
        		 if(stmt!=null) {
				stmt.close();
        		 }
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
        	 try {
        		 if(rs!=null) {
				rs.close();
        		 }
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
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
        
        String foldername=FILE_PATH+"/"+yearStr+"/"+monthStr+"/"+dayStr+"/"+schemaname;
        foldercreaton(foldername);

         return foldername;
		
	}
	
      public static String getZipFoldername() {
		
		SimpleDateFormat year = new SimpleDateFormat("yyyy"); // Customize format as needed
		SimpleDateFormat month = new SimpleDateFormat("MM"); // Customize format as needed
		SimpleDateFormat day = new SimpleDateFormat("dd"); // Customize format as needed

        Date date = new Date();
        String yearStr = year.format(date);
        String monthStr = month.format(date);
        String dayStr = day.format(date);
        
        String foldername=FILE_PATH+"/"+yearStr+"/"+monthStr+"/"+dayStr;
        foldercreaton(foldername);

         return foldername;
		
	}
	
	
	
}
