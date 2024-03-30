package com.unitia.util.dao;

import java.io.ByteArrayOutputStream;
import java.io.ObjectOutputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

import com.unitia.util.db.Close;
import com.unitia.util.db.QueueDBConnection;
import com.unitia.util.misc.RoundRobinTon;


public class Insert {

	static String INSERT_SQL_PATTERN="insert into {0}(msgid,username,scheduletime,pstatus,data,tablename) values(?,?,?,?,?,?)";
	
	List<String> tlist=new ArrayList();

	public Insert(){
		

		tlist.add("t1");
		tlist.add("t2");
		tlist.add("t3");
		tlist.add("t4");
		
	}
	public boolean insert(String tablename, Object requestObject,String username,String msgid,String scheduletime,String smscid) {
		
	
		
		String querytablename=tablename;
		
		if(!(tablename.equals("concatedata")||tablename.equals("httpdn")||tablename.equals("reroute_kannel"))){
			querytablename="queuetable";
		}
	
		Connection connection=null;
		PreparedStatement statement=null;

		try {
		
			String param2=getTname(tablename,username);
			
			if(tablename.startsWith("reroute_kannel")){
				
				param2=smscid;
			}
					
				
				
				connection=QueueDBConnection.getInstance().getConnection();
					
				
			statement=connection.prepareStatement(getQuery(querytablename));
			statement.setString(1, msgid);
			statement.setString(2, param2);
			if(scheduletime==null||scheduletime.trim().length()<1||scheduletime.trim().length()>13){
				scheduletime="0";
			}
			statement.setString(3, scheduletime);
			statement.setString(4, "0");
			
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
            
            ObjectOutputStream oos = new ObjectOutputStream(bos);
            
            oos.writeObject(requestObject);
            
            byte[] Bytes = bos.toByteArray();

			statement.setBytes(5, Bytes);
			statement.setString(6, tablename);

			statement.execute();
			
			return true;
			
		}catch(Exception e) {
			e.printStackTrace();
		}finally {
			
			Close.close(statement);
			Close.close(connection);

			
		}
		
		return false;
	}


	
	

	
	




	
	
	
	private String getQuery(String tablename) {

		String params[]= {tablename};
		
		return MessageFormat.format(INSERT_SQL_PATTERN, params);
	}

	
	
	private String getTname(String tablename,String username) {
		
		int pointer=RoundRobinTon.getInstance().getCurrentIndex(tablename+":"+username, tlist.size());
		
		return username+"_"+tlist.get(pointer);
	}
	
}
