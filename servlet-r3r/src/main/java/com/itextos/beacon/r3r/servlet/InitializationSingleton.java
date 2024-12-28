package com.itextos.beacon.r3r.servlet;

import com.itextos.beacon.commonlib.datarefresher.DataRefresher;
import com.itextos.beacon.commonlib.prometheusmetricsutil.PrometheusMetrics;
import com.itextos.beacon.r3r.process.DataProcessor;

public class InitializationSingleton {

	private static InitializationSingleton obj=new InitializationSingleton();
	
	private InitializationSingleton() {
		
		init();
	}
	
	private void init() {
	    PrometheusMetrics.registerServer();
        PrometheusMetrics.registerApiMetrics();
  
        DataProcessor.getInstance();
    }

	public static InitializationSingleton getInstance() {
		
		if(obj==null) {
			
			obj=new InitializationSingleton();
		}
		return obj;
	}
}
