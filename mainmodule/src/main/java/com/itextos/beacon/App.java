package com.itextos.beacon;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.itextos.beacon.commonlib.prometheusmetricsutil.PrometheusMetrics;

public class App {

    private static final Log                log                               = LogFactory.getLog(App.class);

    private static boolean IS_START_PROMETHEUS=false;
    
	public static void main(String[] args) {
		
		
		String module=System.getenv("module");

		if(!isMW(module,args)) {
			
			
			if(!isBiller(module,args)) {
				
				if(!isAux(module,args)) {
					
					if(!isSMPP(module,args)) {

						boolean result=isHTTP(module,args);
						
						if(!result) {
							
						}else {
							
							IS_START_PROMETHEUS=true;
						}

					}
				}
			}
		}
		
		if(IS_START_PROMETHEUS) {
			
			startPrometheusServer(true);

		}
	}
	
	 private static boolean isHTTP(String module, String[] args) {

		return com.itextos.beacon.jetty.server.MultiWarDeployment.deploy(module);

	}

	private static void startPrometheusServer(
	            boolean aStartJettyServer)
	    {

	        try
	        {

	            if (aStartJettyServer)
	            {
	                PrometheusMetrics.registerServer();
	                PrometheusMetrics.registerPlatformMetrics();
	            }
	        }
	        catch (final Exception e)
	        {
	            // Add this exception in INFO mode.
	            if (log.isInfoEnabled())
	                log.info("IGNORE: Exception while working on prometheus counter", e);
	        }
	    }

	private static boolean isSMPP(String module, String[] args) {
		
		if(module.equals("smpp")) {
			
			com.itextos.beacon.smpp.interfaces.StartApplication.main(args);

			IS_START_PROMETHEUS=true;

			return true;
			
		}
		return false;
	}

	private static boolean isAux(String module, String[] args) {

		
		if(module.equals("wc")) {
			
			com.itextos.beacon.platform.wc.StartApplication.main(args);

			IS_START_PROMETHEUS=true;

			return true;
			
		}else if(module.equals("dnp")) {
			
			com.itextos.beacon.platform.dnpcore.StartApplication.main(args);

			IS_START_PROMETHEUS=true;

			return true;			
		}else if(module.equals("r3c")) {
			
			com.itextos.beacon.platform.r3c.StartApplication.main(args);

			IS_START_PROMETHEUS=true;

			return true;			
		}else if(module.equals("prc")) {
			
			com.itextos.beacon.platform.prc.StartApplication.main(args);

			IS_START_PROMETHEUS=true;

			return true;			
		}else if(module.equals("dltc")) {
			
			com.itextos.beacon.platform.dltvc.StartApplication.main(args);

			IS_START_PROMETHEUS=true;

			return true;			
		}else if(module.equals("smppdlrhandover")) {
			
			com.itextos.beacon.platform.smppdlr.StartApplication.main(args);

			IS_START_PROMETHEUS=true;

			return true;			
		}else if(module.equals("httpclienthandover")) {
			
			com.itextos.beacon.httpclienthandover.StartApplication.main(args);

			IS_START_PROMETHEUS=true;

			return true;			
		}
		
		
		return false;
	
	}

	private static boolean isBiller(String module, String[] args) {
		
		if(module.equals("subbiller")) {
			
			com.itextos.beacon.platform.subbiller.StartApplication.main(args);

			IS_START_PROMETHEUS=true;

			return true;
			
		}else if(module.equals("subt2tb")) {
			
			com.itextos.beacon.platform.subt2tb.StartApplication.main(args);

			IS_START_PROMETHEUS=true;

			return true;			
		}else if(module.equals("dnt2tb")) {
			
			com.itextos.beacon.platform.dnt2tb.StartApplication.main(args);

			IS_START_PROMETHEUS=true;

			return true;			
		}else if(module.equals("fullmsgt2tb")) {
			
			com.itextos.beacon.platform.fullmsgt2tb.StartApplication.main(args);

			IS_START_PROMETHEUS=true;

			return true;			
		}else if(module.equals("errorlogt2tb")) {
			
			com.itextos.beacon.platform.errorlogt2tb.StartApplication.main(args);

			IS_START_PROMETHEUS=true;

			return true;			
		}else if(module.equals("dnpostlogt2tb")) {
			
			com.itextos.beacon.platform.dnpostlogt2tb.StartApplication.main(args);

			IS_START_PROMETHEUS=true;

			return true;			
		}else if(module.equals("dnnopayloadt2tb")) {
			
			com.itextos.beacon.platform.dnnopayloadt2tb.StartApplication.main(args);

			IS_START_PROMETHEUS=true;

			return true;			
		}else if(module.equals("sbc")) {
			
			com.itextos.beacon.platform.sbc.StartApplication.main(args);

			IS_START_PROMETHEUS=true;

			return true;			
		}else if(module.equals("t2e")) {
			
			com.itextos.beacon.platform.t2e.StartApplication.main(args);
			
			IS_START_PROMETHEUS=true;

			return true;			
		}else if(module.equals("clienthandovert2tb")) {
			
			com.itextos.beacon.platform.clienthandovert2tb.StartApplication.main(args);
			
			IS_START_PROMETHEUS=true;

			return true;			
		}
		
		return false;
	}

	private static boolean isMW(String module,String[] args) {
		

		if(module.equals("ic")){
			
			com.itextos.beacon.platform.ic.StartApplication.main(args);
			
			IS_START_PROMETHEUS=true;
			return true;
			
		}else if(module.equals("sbcv")){
			
			com.itextos.beacon.platform.sbcv.StartApplication.main(args);
			
			IS_START_PROMETHEUS=true;

			return true;
			
		}else if(module.equals("vc")){
			
			com.itextos.beacon.platform.vc.StartApplication.main(args);
			
			IS_START_PROMETHEUS=true;

			return true;
			
		}else if(module.equals("rc")){
			
			com.itextos.beacon.platform.rc.StartApplication.main(args);
			
			IS_START_PROMETHEUS=true;

			return true;
			
		}else if(module.equals("ch")){
			
			com.itextos.beacon.platform.ch.StartApplication.main(args);
			
			IS_START_PROMETHEUS=true;

			return true;
		}else if(module.equals("rch")){
			
			com.itextos.beacon.platform.rch.StartApplication.main(args);
			
			IS_START_PROMETHEUS=true;

			return true;
		}else if(module.equals("dch")){
			
			com.itextos.beacon.platform.dch.StartApplication.main(args);
			
			IS_START_PROMETHEUS=true;

			return true;
		}
		
		return false;
	}

}
