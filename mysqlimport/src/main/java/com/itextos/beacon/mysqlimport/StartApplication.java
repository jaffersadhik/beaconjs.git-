package com.itextos.beacon.mysqlimport;

public class StartApplication {

	public static void main(String[] args) {
		
		
		GitAutomation.pullMysql();
		
		try {
			FolderCompressor.uncompressFile("/mysqldump/technowizardsmysqldump/technowizardsmysqldump.zip","/mysqldump/uncompress");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		new CreateScript().createschema();
		new CreateScript().create();

	}

}
