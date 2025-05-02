package com.itextos.beacon.mysqlimport;

import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.itextos.beacon.commonlib.constants.ErrorMessage;

public class CollectionToTableInsert {

	
	public static void execute() {
		doProcess();
		doProcess();
		doProcess();
	}
	private static void doProcess() {

		List<String> schemalist=getListOfSchema();
		
		schemalist.forEach((schema)->{
			
			if(!schema.equals("createtable")) {
			
				List<String> tablelist=getList(schema);
				
				tablelist.forEach((table)->{
					
										
					String tablefilename=table.substring(0,table.lastIndexOf(".")-1);
					MysqlImportLog.log(tablefilename+" import taken");

					List<Map<String, Object>> rowlist=CollectionToFile.loadCollection("/mysqldump/uncompress/"+schema+"/"+table);
					MysqlImportLog.log(rowlist.size()-1+" rows going to  import to "+tablefilename);

					insert(schema,tablefilename,rowlist);
				});
				

			}
		});

	}
	
	 private static void insert(String schema, String tablefilename, List<Map<String, Object>> rowlist) {
		Connection connection=null;
		
		try {
			
			connection=DBConnection.getConnection(schema);
			
			MysqlImportLog.log(schema+" db connection getting");
			insertIntoTable(connection, rowlist, tablefilename);
			
		}catch(Exception e) {
        	MysqlImportLog.log(ErrorMessage.getStackTraceAsString(e));

		}finally {
			
			try {
				if(connection!=null) {
					
					connection.close();
				}
			}catch(Exception e) {
				
			}
		}
		
	}

	private static  List<String> getListOfSchema() {
			
   	  File folder = new File("/mysqldump/uncompress");
   	  
   	List<String> result=new ArrayList<String>();
   	
         // Check if it's a directory
         if (folder.isDirectory()) {
             File[] files = folder.listFiles();

             if (files != null && files.length > 0) {
                 for (File file : files) {
                     if (file.isDirectory()) {
                         result.add(file.getName());
                     }
                 }
             }
         } 
         
         return result;
	}
	 
	 
    private static List<String> getList(String schema) {
		
     	  File folder = new File("/mysqldump/uncompress/"+schema);
     	  
     	List<String> result=new ArrayList<String>();
     	
           // Check if it's a directory
           if (folder.isDirectory()) {
               File[] files = folder.listFiles();

               if (files != null && files.length > 0) {
                   for (File file : files) {
                       if (file.isFile()) {
                           result.add(file.getAbsolutePath());
                       }
                   }
               }
           } 
           
           return result;
  	}
	private static void insertIntoTable(Connection conn, List<Map<String, Object>> rows, String tableName) throws SQLException {
        if (rows == null || rows.isEmpty()) return;

        Map<String, Object> firstRow = rows.get(0);
        String[] columns = firstRow.keySet().toArray(new String[0]);

        StringBuilder sql = new StringBuilder("INSERT INTO " + tableName + " (");
        sql.append(String.join(", ", columns));
        sql.append(") VALUES (");
        sql.append("?,".repeat(columns.length));
        sql.setLength(sql.length() - 1); // Remove trailing comma
        sql.append(")");

        MysqlImportLog.log(sql.toString());
        PreparedStatement ps=null;
        try  {
        	ps= conn.prepareStatement(sql.toString());
            for (Map<String, Object> row : rows) {
                for (int i = 0; i < columns.length; i++) {
                    ps.setObject(i + 1, row.get(columns[i]));
                }
                ps.addBatch();
            }
            ps.executeBatch();
        }catch(Exception e) {
        	
        	MysqlImportLog.log(ErrorMessage.getStackTraceAsString(e));
        }finally {
        	try {
        		if(ps!=null) {
        			ps.close();
        		}
        	}catch(Exception e) {
        		
        	}finally {
        		
        	}
        }
    }
}
