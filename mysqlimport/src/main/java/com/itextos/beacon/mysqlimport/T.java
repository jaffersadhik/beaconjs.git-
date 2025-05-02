package com.itextos.beacon.mysqlimport;

import java.nio.file.Paths;

public class T {

	public static void main(String[] args) {
		
		String table="/mysqldump/uncompress/configuration/fmsg_log_download_col_map.ser";
		String tablefilename=table.substring(0,table.lastIndexOf("."));
         tablefilename = Paths.get(tablefilename).getFileName().toString();
 		System.out.println(tablefilename);


		System.out.print(tablefilename);
	}

}
