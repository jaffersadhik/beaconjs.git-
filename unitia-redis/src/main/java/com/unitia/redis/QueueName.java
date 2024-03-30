package com.unitia.redis;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashSet;
import java.util.Set;

import com.unitia.util.db.Close;
import com.unitia.util.db.CoreDBConnection;
import com.unitia.util.db.TableExsists;

public class QueueName {

	private static Set<String> queuenameset=new HashSet<String>();

	private static QueueName obj =new QueueName();
	
	
	private QueueName() {
	
		init();
		
		reload();
	}
	
	private void init() {
		
		TableExsists table=new TableExsists();
		
		Connection connection=null;
		
		try {
			
			connection=CoreDBConnection.getInstance().getConnection();
			
			if(!table.isExsists(connection, "queuenames")) {
				
				table.create(connection, "create table queuenames(queuename varchar(100) primary key not null)",false);
			}
		}catch(Exception e) {
			
		}finally {
			
			Close.close(connection);
		}
		
	}
	
	public static QueueName getInstance() {
		
		if(obj==null) {
			
			obj=new QueueName();
		}

		return obj;
	}
	
	public void add(String queuename) {
		
		if(!queuenameset.contains(queuename)) {
			
			insert(queuename);
			reload();
		}
	}

	private void reload() {
		Connection connection=null;
		PreparedStatement statement=null;
		ResultSet resultset=null;
		try {
			connection=CoreDBConnection.getInstance().getConnection();
			statement=connection.prepareStatement("select queuename from queuenames");
			resultset=statement.executeQuery();
			while(resultset.next()) {
			
				if(queuenameset==null) {
					
					queuenameset=new HashSet<String>();
				}
				queuenameset.add(resultset.getString("queuename"));
			}
			
		}catch(Exception e) {
			e.printStackTrace();;
		}finally {
			
			Close.close(resultset);
			Close.close(statement);
			Close.close(connection);
		}
		
	}

	private void insert(String queuename) {
		Connection connection=null;
		PreparedStatement statement=null;
		
		try {
			connection=CoreDBConnection.getInstance().getConnection();
			statement=connection.prepareStatement("insert into queuenames(queuename) values(?)");
			statement.setString(1, queuename);
			statement.execute();
		}catch(Exception e) {
			e.printStackTrace();;
		}finally {
			
			Close.close(statement);
			Close.close(connection);
		}
		
	}
}
