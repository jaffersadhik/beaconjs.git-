package com.itextos.beacon.mysqlimport;

public class T {

	public static void main(String[] args) {
		
		String createscriptsql="cast(current_timestamp() as date)";
		createscriptsql=createscriptsql.replaceAll("cast\\(current_timestamp\\(\\) as date\\)", "\\(CURRENT_DATE\\)");

		System.out.print(createscriptsql);
	}

}
