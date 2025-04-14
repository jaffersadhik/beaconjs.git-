package com.itextos.beacon.platform.mysqltabledatadump;

import java.io.IOException;

public class DumpStartup extends Thread{
	
	
	public static void main(String args[]) {
		
		Dump a=new Dump();
		
	       Thread.ofVirtual().start(new DumpStartup());
	       
	       Thread.ofVirtual().start(new RedisDump());


	}
	
	public void run() {
		
		while(true) {
			
			Dump a=new Dump();

			mysqlDump();
			
			mysqlCreateTableScript();
			
			String foldername=a.getFoldername("createtable");

    		String filename=foldername+"/createtable.ser";

            CollectionToFile.saveCollectionForCreate(CreateTableScript.TABLESCRIPT, filename);

			
			try {
				FolderCompressor.compressFolder(Dump.getZipFoldername(),"/mysqldump/technowizardsmysqldump/technowizardsmysqldump.zip");
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			GitAutomation.pushMysql();
			gotosleep();
		}
}

	private void mysqlCreateTableScript() {
		
		CreateTableScript.takeCreateScript("accounts");
		
		CreateTableScript.takeCreateScript("configuration");

		CreateTableScript.takeCreateScript("imp");

		CreateTableScript.takeCreateScript("listing");

		CreateTableScript.takeCreateScript("messaging");

		CreateTableScript.takeCreateScript("billing");

		CreateTableScript.takeCreateScript("carrier_handover");

		CreateTableScript.takeCreateScript("client_handover");

		CreateTableScript.takeCreateScript("sysconfig");

		CreateTableScript.takeCreateScript("r3c");

		CreateTableScript.takeCreateScript("logging");

		CreateTableScript.takeCreateScript("cm");

		CreateTableScript.takeCreateScript("payload");

		
	}
	private void mysqlDump() {
		
		Dump a=new Dump();
		
		MysqlDumpLog.log("accounts taken start");

		a.takedump("accounts",Table.ACCOUNTS);
		
		MysqlDumpLog.log("carrier_handover taken start");

		a.takedump("carrier_handover",Table.CARRIER_HANDOVER);
		
		MysqlDumpLog.log("client_handover taken start");

		a.takedump("client_handover",Table.CLIENT_HANDOVER);
		
		MysqlDumpLog.log("cm taken start");

		a.takedump("cm",Table.CM);
		
		MysqlDumpLog.log("configuration taken start");

		a.takedump("configuration",Table.CONFIGURATION);
		
		MysqlDumpLog.log("imp taken start");

		a.takedump("imp",Table.IMP);
		
		MysqlDumpLog.log("listing taken start");

		a.takedump("listing",Table.LISTING);
		
		MysqlDumpLog.log("logging taken start");

		a.takedump("logging",Table.LOGGING);
		
		MysqlDumpLog.log("messaging taken start");

		a.takedump("messaging",Table.MESSAGING);
		
		MysqlDumpLog.log("payload taken start");

		a.takedump("payload",Table.PAYLOAD);
		
		MysqlDumpLog.log("r3c taken start");

		a.takedump("r3c",Table.R3C);
		
		MysqlDumpLog.log("sysconfig taken start");

		a.takedump("sysconfig",Table.SYSCONFIG);

	}

	private void gotosleep() {

		try {
			Thread.sleep(60*60*1000);
		}catch(Exception e) {

		}
		
	}
	
}
