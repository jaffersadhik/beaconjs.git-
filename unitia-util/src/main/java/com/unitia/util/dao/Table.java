package com.unitia.util.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.unitia.util.db.Close;
import com.unitia.util.db.QueueDBConnection;
import com.unitia.util.db.TableExsists;


public class Table {

	private static final String BATCHIDLIST_TABLE_CREATE_SQL = "create table {0}(batchid varchar(100) ,ackid varchar(100) ,username varchar(30),itime timestamp default CURRENT_TIMESTAMP,index(batchid,username))";

	private static final String CONCATE_TABLE_CREATE_SQL = "create table {0}(msgid varchar(100) ,ackid varchar(100) ,username varchar(30),itime timestamp default CURRENT_TIMESTAMP,scheduletime numeric(13,0),data LONGBLOB,pstatus numeric(1,0),cc decimal(3,0),index(msgid,username,pstatus),index(ackid))";

	private static final String TABLE_CREATE_SQL = "create table {0}(msgid varchar(50) ,username varchar(30),itime timestamp default CURRENT_TIMESTAMP,scheduletime numeric(13,0),data LONGBLOB,pstatus numeric(1,0),index(msgid,username,scheduletime,pstatus))";

	private static final String TABLE_CREATE_SQL_WITHOUT_KEY = "create table {0}(msgid varchar(50) ,username varchar(30),itime timestamp default CURRENT_TIMESTAMP,scheduletime numeric(13,0),data LONGBLOB,pstatus numeric(1,0),index(username,scheduletime,pstatus))";

	private static final String TABLE_ALTER_SQL_1 = "alter table {0} add column utime timestamp  default CURRENT_TIMESTAMP ";

	private static final String TABLE_ALTER_SQL_2 = "alter table {0} add column tablename varchar(25)";

	private static Table obj=null;
	
	private Set<String> availabletable=new HashSet();
	
	private Map<String,Set<String>> availabletablemap=new HashMap<String,Set<String>>();

	private Table(){
		
	}
	
	public static Table getInstance(){
		
		if(obj==null){
			
			obj=new Table();
		}
		
		return obj;
	}
	
	public boolean isAvailableTable(String tablename){
	
		return availabletable.contains(tablename);
	}
	
	
	public boolean isAvailableTable(String dbrefid,String tablename){
		if(availabletablemap.get(dbrefid)!=null){
			
			return availabletablemap.get(dbrefid).contains(tablename);

		}
		
		return false;
	}
	
	public void addTable(String tablename){
		
		
		Connection connection=null;
		String SQL="";
		try{
			
		
				
				connection=QueueDBConnection.getInstance().getConnection();

				SQL=TABLE_CREATE_SQL ;
				
				if(tablename.equals("concatedata")){
					SQL=CONCATE_TABLE_CREATE_SQL ;

				}else if(tablename.equals("batchidlist")){
					
					SQL=BATCHIDLIST_TABLE_CREATE_SQL;
				}

			TableExsists table=new TableExsists();
		
		if(!table.isExsists(connection, tablename)){
			
			table.create(connection,getQueuey( SQL,tablename), true);
		}
		
		availabletable.add(tablename);
		}catch(Exception e){
			
		}finally{
			
			Close.close(connection);
		}
	}

	
	public void addColumn(String dbrefid,String tablename){
		
		
		Connection connection=null;
		try{
			
		
				
				connection=QueueDBConnection.getInstance().getConnection();

				
				addColumn(connection,TABLE_ALTER_SQL_1,tablename);
				
				addColumn(connection,TABLE_ALTER_SQL_2,tablename);

				
				
		}catch(Exception e){
			
		}finally{

			Close.close(connection);
		}

		
	}
	
public void addColumn(Connection connection,String SQL,String tablename){
		
		PreparedStatement statement=null;
		try{

			SQL=getQueuey(SQL,tablename);

			statement=connection.prepareStatement(SQL);
			
			statement.execute();
		}catch(Exception e){
			
		}finally{

			Close.close(statement);
		}

		
	}

public void addTable(String dbrefid,String tablename){
		
		
		Connection connection=null;
		String SQL="";
		try{
			
		
				
				connection=QueueDBConnection.getInstance().getConnection();

				SQL=TABLE_CREATE_SQL ;
				
				if(tablename.equals("concatedata")){
					SQL=CONCATE_TABLE_CREATE_SQL ;

				}else if(tablename.equals("batchidlist")){
					
					SQL=BATCHIDLIST_TABLE_CREATE_SQL;
				}

			TableExsists table=new TableExsists();
		
		if(!table.isExsists(connection, tablename)){
			
			table.create(connection,getQueuey( SQL,tablename), true);
		}
		
		Set<String> availabletable=availabletablemap.get(dbrefid);

		if(availabletable==null){
			availabletable=new HashSet<String>();
			availabletablemap.put(dbrefid, availabletable);
		}
		availabletable.add(tablename);
		}catch(Exception e){
			
		}finally{
			
			Close.close(connection);
		}
	}

	private String getQueuey(String tableCreateSql, String tablename) {
		String  [] params={tablename};
		
		
		return MessageFormat.format(tableCreateSql, params);
	}
}
