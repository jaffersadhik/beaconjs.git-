package com.itextos.beacon.platform.kannelstatusupdater;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.itextos.beacon.commonlib.commondbpool.DBDataSourceFactory;
import com.itextos.beacon.commonlib.constants.Table2DBInserterId;
import com.itextos.beacon.commonlib.utility.CommonUtility;
import com.itextos.beacon.commonlib.utility.tp.ExecutorSheduler;
import com.itextos.beacon.inmemory.loader.InmemoryLoaderCollection;
import com.itextos.beacon.inmemory.loader.process.InmemoryId;
import com.itextos.beacon.platform.kannelstatusupdater.process.KannelStatusRefresher;
import com.itextos.beacon.platform.topic2table.dbinfo.TableInserterInfo;
import com.itextos.beacon.platform.topic2table.dbinfo.TableInserterInfoCollection;
import com.itextos.beacon.smslog.DebugLog;
import com.itextos.beacon.smslog.TableCreationLog;

public class StartApplication
{

    private static final Log log = LogFactory.getLog(StartApplication.class);

    public static void main(
            String[] args)
    {
        if (log.isDebugEnabled())
            log.debug("Starting the application Kannel Status Updater");

        DebugLog.log("Starting the application Kannel Status Updater");
        try
        {
            KannelStatusRefresher.getInstance();
            


        }
        catch (final Exception e)
        {
            log.error("Exception while staring the Kannel status Refresher.", e);
            System.exit(-1);
        }
    }
    
    
   static  class TableCreation implements Runnable {
    	
    	public void run() {
    		
    		while(true) {
    			
    			doTableCreation();
    			
                CommonUtility.sleepForAWhile(15*60*1000);

    		}
    	}

		private void doTableCreation() {


			Connection con=null;
			try {
				
				 final TableInserterInfoCollection tiic = (TableInserterInfoCollection) InmemoryLoaderCollection.getInstance().getInmemoryCollection(InmemoryId.TABLE_INSERTER_INFO);

				 TableInserterInfo  mTableInserterInfo = tiic.getTableInserterInfo(Table2DBInserterId.DELIVERIES);


			 con = DBDataSourceFactory.getConnection(mTableInserterInfo.getJndiInfo());
			 
			 Map<String,List<String>> schemamap=getSchemaList();
			 
			 Iterator itr=schemamap.keySet().iterator();
			 
			 while(itr.hasNext()) {
				 
				 String schema=itr.next().toString();
				 
				 if (!isSchemaExists(con, schema)) {
		                createSchema(con, schema);
		                
		                TableCreationLog.log(" schema : "+schema+" : Schema Created On : "+new Date());
		            }
				 
				 List<String> tablelist=schemamap.get(schema);
				 
				 for(int i=0;i<tablelist.size();i++) {
					 
					 createTable(con,schema+".submission_"+tablelist.get(i));
					 
					 createTable(con,schema+".interim_failures_"+tablelist.get(i));

					 createTable(con,schema+".full_message_"+tablelist.get(i));

					 createTable(con,schema+".dn_post_log_"+tablelist.get(i));
					 
					 createTable(con,schema+".deliveries_"+tablelist.get(i));


					 

				 }
			 }
			 
			
			 
		
				
			}catch(Exception e) {
				
				
			}finally {
				
				if(con!=null) {
					try {
					con.close();
					}catch(Exception e) {}
				}
				
			}
			
		}

		private void createTable(Connection con, String tablename) {
			
			
			if(isTableExists(con,tablename)) {
				
			}else {
				
				createTableInSchema(con,tablename);
				
                TableCreationLog.log(" table : "+tablename+" : Table Created On : "+new Date());

			}
			
		}

		private Map<String,List<String>> getSchemaList() {
			
			List<String> schemalist=new ArrayList<String>();
			
			SimpleDateFormat sdf1 =new SimpleDateFormat("yyyymmdd");

			String day=sdf1.format(new Date());
			int intDay=Integer.parseInt(day);
			
			schemalist.add(""+intDay);
			intDay++;
						
			schemalist.add(""+intDay);
			
			Map<String,List<String>> result=new HashMap<String,List<String>>();
			
			SimpleDateFormat sdf =new SimpleDateFormat("yyyymm");
			
			String month=sdf.format(new Date());
			
			
			
			if(month.endsWith("12")) {
				
				int intMonth=Integer.parseInt(month);
				result.put("billing_"+intMonth, schemalist);
				
				SimpleDateFormat sdf2 =new SimpleDateFormat("yyyy");
				
				String year=sdf2.format(new Date());
				
				int intYear=Integer.parseInt(year);
				
				intYear++;
				
				List<String> l=new ArrayList<String>();
				
				l.add(""+intYear+"0101");
				result.put("billing_"+intYear+"01", l);


			}else {
			

				int intMonth=Integer.parseInt(month);
				result.put("billing_"+intMonth, schemalist);
				
				intMonth++;
				
				List<String> l=new ArrayList<String>();
				
				l.add(""+intMonth+"01");
				
				result.put("billing_"+intMonth, l);

			}
			
			
			return result;
		}
    }
   
   
   private static void createSchema(Connection connection, String schemaName)  {
       String query = "CREATE DATABASE " + schemaName;
       Statement statement=null;

       try {
    	   statement = connection.createStatement();
           statement.executeUpdate(query);
       }catch(Exception e) {
    	   
       }finally {
    	   
    	
    		
    		if(statement!=null) {
				try {
					statement.close();
				}catch(Exception e) {}
			}
    	   
       }
   }
   
   private static void createTableInSchema(Connection connection, String tablename)  {
	   
       String query = "create table "+tablename+" as select * from "+tablename.substring(tablename.indexOf("."));
       Statement statement=null;

       try {
    	   statement = connection.createStatement();
           statement.executeUpdate(query);
       }catch(Exception e) {
    	   
       }finally {
    	
    	   if(statement!=null) {
				try {
					statement.close();
				}catch(Exception e1) {}
			}
       }
   }
 
   private static boolean isTableExists(Connection connection, String tablename)  {

       String query = "SELECT count(*) from "+tablename;
       Statement statement=null;
       ResultSet resultSet=null;
       try  {
    	   statement= connection.createStatement();
		   resultSet= statement.executeQuery(query);
           return resultSet.next(); // Returns true if schema exists
       }catch(Exception e) {
    	   
       }finally {
    	   
    		if(resultSet!=null) {
				try {
					resultSet.close();
				}catch(Exception e) {}
			}
    		
    		if(statement!=null) {
				try {
					statement.close();
				}catch(Exception e) {}
			}
    	   
       }
       
       return false;
   }

   private static boolean isSchemaExists(Connection connection, String schemaName)  {
	   
       String query = "SELECT SCHEMA_NAME FROM INFORMATION_SCHEMA.SCHEMATA WHERE SCHEMA_NAME = '" + schemaName + "'";
       Statement statement=null;
       ResultSet resultSet=null;
       try  {
    	   statement= connection.createStatement();
		   resultSet= statement.executeQuery(query);
           return resultSet.next(); // Returns true if schema exists
       }catch(Exception e) {
    	   
       }finally {
    	   
    		if(resultSet!=null) {
				try {
					resultSet.close();
				}catch(Exception e) {}
			}
    		
    		if(statement!=null) {
				try {
					statement.close();
				}catch(Exception e) {}
			}
    	   
       }
       
       return false;
   }
}
