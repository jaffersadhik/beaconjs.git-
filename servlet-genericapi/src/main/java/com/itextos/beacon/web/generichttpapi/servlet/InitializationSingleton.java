package com.itextos.beacon.web.generichttpapi.servlet;

import com.itextos.beacon.commonlib.constants.InterfaceType;
import com.itextos.beacon.commonlib.messageidentifier.MessageIdentifier;
import com.itextos.beacon.commonlib.prometheusmetricsutil.PrometheusMetrics;
import com.itextos.beacon.http.generichttpapi.common.utils.APIConstants;
import com.itextos.beacon.http.interfacefallback.inmem.FallbackQReaper;

public class InitializationSingleton {

	private static InitializationSingleton obj=new InitializationSingleton();
	
	private InitializationSingleton() {
		
		init();
	}
	
	private void init() {


    	
	    

	     

        try
        {
            
            final MessageIdentifier lMsgIdentifier = MessageIdentifier.getInstance();
            lMsgIdentifier.init(InterfaceType.HTTP_JAPI);

            final String lAppInstanceId = lMsgIdentifier.getAppInstanceId();

         
            PrometheusMetrics.registerServer();
            PrometheusMetrics.registerApiMetrics();

            APIConstants.setAppInstanceId(lAppInstanceId);

            if (APIConstants.CLUSTER_INSTANCE == null)
            {
              //  System.exit(-1);
            }

            
       //     String module=System.getenv("module");
     //       if(module!=null&&(module.equals("japi")||module.equals("all"))) {
            	
            	FallbackQReaper.getInstance();

   //         	startConsumers();
      //      }
        }
        catch (final Exception e)
        {
        }
    
	}

	public static InitializationSingleton getInstance() {
		
		if(obj==null) {
			
			obj=new InitializationSingleton();
		}
		return obj;
	}
}
