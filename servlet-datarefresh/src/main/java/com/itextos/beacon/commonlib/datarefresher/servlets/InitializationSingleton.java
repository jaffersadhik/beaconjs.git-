package com.itextos.beacon.commonlib.datarefresher.servlets;

import com.itextos.beacon.commonlib.datarefresher.DataRefresher;

public class InitializationSingleton {

	private static InitializationSingleton obj=new InitializationSingleton();
	
	private InitializationSingleton() {
		
		init();
	}
	
	private void init() {
        DataRefresher.getInstance();

    }

	public static InitializationSingleton getInstance() {
		
		if(obj==null) {
			
			obj=new InitializationSingleton();
		}
		return obj;
	}
}
