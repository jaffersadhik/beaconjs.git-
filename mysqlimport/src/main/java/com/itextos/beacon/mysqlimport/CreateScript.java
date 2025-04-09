package com.itextos.beacon.mysqlimport;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class CreateScript {

	Map<String,String> tablestatus=new HashMap<String,String>();
	
	public void create() {
		
		createAccountsSchematable();
		createBillingSchematable();
		createCarrierHandoverSchematable();
		createClientHandoverSchematable();
		createCMSchematable();
		createConfigurationSchematable();
		createIMPSchematable();
		createListingSchematable();
		createLoggingSchematable();
		createMessagingSchematable();
		createPayloadSchematable();
		createR3CSchematable();
		createSysconfigSchematable();

	}

	private void createAccountsSchematable() {
		
		Connection connection=DBConnection.getAccountsConnection();
		
		String schema="accounts";
		
		Map<String,String> scriptfilemap=LoadScript.scriptmap.get(schema);
		
		createtable(connection,schema,scriptfilemap);
		
		createtable(connection,schema,scriptfilemap);

		try {
			connection.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	
	
private void createSysconfigSchematable() {
		
		Connection connection=DBConnection.getSysconfigConnection();
		
		String schema="sysconfig";
		
		Map<String,String> scriptfilemap=LoadScript.scriptmap.get(schema);
		
		createtable(connection,schema,scriptfilemap);
		
		createtable(connection,schema,scriptfilemap);

		try {
			connection.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

private void createR3CSchematable() {
	
	Connection connection=DBConnection.getR3CConnection();
	
	String schema="r3c";
	
	Map<String,String> scriptfilemap=LoadScript.scriptmap.get(schema);
	
	createtable(connection,schema,scriptfilemap);
	
	createtable(connection,schema,scriptfilemap);

	try {
		connection.close();
	} catch (SQLException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
	
}

private void createPayloadSchematable() {
	
	Connection connection=DBConnection.getPayloadConnection();
	
	String schema="payload";
	
	Map<String,String> scriptfilemap=LoadScript.scriptmap.get(schema);
	
	createtable(connection,schema,scriptfilemap);
	
	createtable(connection,schema,scriptfilemap);

	try {
		connection.close();
	} catch (SQLException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
	
}

private void createMessagingSchematable() {
	
	Connection connection=DBConnection.getMessagingConnection();
	
	String schema="messaging";
	
	Map<String,String> scriptfilemap=LoadScript.scriptmap.get(schema);
	
	createtable(connection,schema,scriptfilemap);
	
	createtable(connection,schema,scriptfilemap);

	try {
		connection.close();
	} catch (SQLException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
	
}

private void createLoggingSchematable() {
	
	Connection connection=DBConnection.getLoggingConnection();
	
	String schema="logging";
	
	Map<String,String> scriptfilemap=LoadScript.scriptmap.get(schema);
	
	createtable(connection,schema,scriptfilemap);
	
	createtable(connection,schema,scriptfilemap);

	try {
		connection.close();
	} catch (SQLException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
	
}

private void createListingSchematable() {
	
	Connection connection=DBConnection.getListingConnection();
	
	String schema="listing";
	
	Map<String,String> scriptfilemap=LoadScript.scriptmap.get(schema);
	
	createtable(connection,schema,scriptfilemap);
	
	createtable(connection,schema,scriptfilemap);

	try {
		connection.close();
	} catch (SQLException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
	
}


private void createIMPSchematable() {
	
	Connection connection=DBConnection.getIMPConnection();
	
	String schema="imp";
	
	Map<String,String> scriptfilemap=LoadScript.scriptmap.get(schema);
	
	createtable(connection,schema,scriptfilemap);
	
	createtable(connection,schema,scriptfilemap);

	try {
		connection.close();
	} catch (SQLException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
	
}


private void createConfigurationSchematable() {
	
	Connection connection=DBConnection.getConfigurationConnection();
	
	String schema="configuration";
	
	Map<String,String> scriptfilemap=LoadScript.scriptmap.get(schema);
	
	createtable(connection,schema,scriptfilemap);
	
	createtable(connection,schema,scriptfilemap);

	try {
		connection.close();
	} catch (SQLException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
	
}


private void createClientHandoverSchematable() {
	
	Connection connection=DBConnection.getClientHanoverConnection();
	
	String schema="client_handover";
	
	Map<String,String> scriptfilemap=LoadScript.scriptmap.get(schema);
	
	createtable(connection,schema,scriptfilemap);
	
	createtable(connection,schema,scriptfilemap);

	try {
		connection.close();
	} catch (SQLException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
	
}


private void createCarrierHandoverSchematable() {
	
	Connection connection=DBConnection.getCarrierHandoverConnection();
	
	String schema="carrier_handover";
	
	Map<String,String> scriptfilemap=LoadScript.scriptmap.get(schema);
	
	createtable(connection,schema,scriptfilemap);
	
	createtable(connection,schema,scriptfilemap);

	try {
		connection.close();
	} catch (SQLException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
	
}


private void createCMSchematable() {
	
	Connection connection=DBConnection.getCMConnection();
	
	String schema="cm";
	
	Map<String,String> scriptfilemap=LoadScript.scriptmap.get(schema);
	
	createtable(connection,schema,scriptfilemap);
	
	createtable(connection,schema,scriptfilemap);

	try {
		connection.close();
	} catch (SQLException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
	
}


private void createBillingSchematable() {
	
	Connection connection=DBConnection.getBillingConnection();
	
	String schema="billing";
	
	Map<String,String> scriptfilemap=LoadScript.scriptmap.get(schema);
	
	createtable(connection,schema,scriptfilemap);
	
	createtable(connection,schema,scriptfilemap);

	try {
		connection.close();
	} catch (SQLException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
	
}



	
	
	

	private void createtable(Connection connection, String schema, Map<String, String> scriptfilemap) {
		
		
		Iterator<String> itr=scriptfilemap.keySet().iterator();
		
		while(itr.hasNext()) {
			
			String tablename=itr.next();
			
			String scriptfilename=scriptfilemap.get(tablename);
			
			String tablestatusstring=tablestatus.get(schema+"~"+tablename);
			
			if(tablestatusstring==null||!tablestatusstring.equals("created")) {
				
				createtable(connection,schema,tablename,scriptfilename);
			}
		}
		
	}

	private void createtable(Connection connection, String schema, String tablename, String scriptfilename) {

		String createscriptstring=readFileAsString(scriptfilename);
		
		Statement statement=null;
		
		try {
			
			statement=connection.createStatement();
			
			if(statement.execute(createscriptstring)) {
			
			tablestatus.put(schema+"~"+tablename, "created");
			
			}
			
			
		}catch(Exception e) {
			
		}finally {
		
			if(statement!=null) {
				
				try {
					statement.close();
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		
	}

	private static String readFileAsString(String filePath)  {
        try {
			return new String(Files.readAllBytes(Paths.get(filePath)));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        
        return null;
    }
}
