package com.itextos.beacon.platform.dnr.servlet;

import com.itextos.beacon.commonlib.prometheusmetricsutil.PrometheusMetrics;
import com.itextos.beacon.platform.dnrfallback.inmem.DlrFallbackQReaper;

public class InitializationSingleton {

	private static InitializationSingleton obj=new InitializationSingleton();
	
	private InitializationSingleton() {
		
		init();
	}
	
	private void init() {

        PrometheusMetrics.registerServer();
        PrometheusMetrics.registerApiMetrics();
        DlrFallbackQReaper.getInstance();
    }

	public static InitializationSingleton getInstance() {
		
		if(obj==null) {
			
			obj=new InitializationSingleton();
		}
		return obj;
	}
}
