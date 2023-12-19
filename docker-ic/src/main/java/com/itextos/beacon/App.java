package com.itextos.beacon;

public class App {

	public static void main(String[] args) {
		
		String module=System.getenv("module");

		if(!isMW(module,args)) {
			
			
			if(!isBiller(module,args)) {
				
				if(!isAux(module,args)) {
					
				}
			}
		}
	}

	private static boolean isAux(String module, String[] args) {

		
		if(module.equals("wc")) {
			
			com.itextos.beacon.platform.wc.StartApplication.main(args);
			
			return true;
			
		}else if(module.equals("dnp")) {
			
			com.itextos.beacon.platform.dnpcore.StartApplication.main(args);
			
			return true;			
		}else if(module.equals("r3c")) {
			
			com.itextos.beacon.platform.r3c.StartApplication.main(args);
			
			return true;			
		}else if(module.equals("prc")) {
			
			com.itextos.beacon.platform.prc.StartApplication.main(args);
			
			return true;			
		}else if(module.equals("dltc")) {
			
			com.itextos.beacon.platform.dltvc.StartApplication.main(args);
			
			return true;			
		}else if(module.equals("smppdlrhandover")) {
			
			com.itextos.beacon.platform.smppdlr.StartApplication.main(args);
			
			return true;			
		}else if(module.equals("httpclienthandover")) {
			
			com.itextos.beacon.httpclienthandover.StartApplication.main(args);
			
			return true;			
		}
		
		
		return false;
	
	}

	private static boolean isBiller(String module, String[] args) {
		
		if(module.equals("subbiller")) {
			
			com.itextos.beacon.platform.subbiller.StartApplication.main(args);
			
			return true;
			
		}else if(module.equals("subt2tb")) {
			
			com.itextos.beacon.platform.subt2tb.StartApplication.main(args);
			
			return true;			
		}else if(module.equals("dnt2tb")) {
			
			com.itextos.beacon.platform.dnt2tb.StartApplication.main(args);
			
			return true;			
		}else if(module.equals("fullmsgt2tb")) {
			
			com.itextos.beacon.platform.fullmsgt2tb.StartApplication.main(args);
			
			return true;			
		}else if(module.equals("errorlogt2tb")) {
			
			com.itextos.beacon.platform.errorlogt2tb.StartApplication.main(args);
			
			return true;			
		}else if(module.equals("dnpostlogt2tb")) {
			
			com.itextos.beacon.platform.dnpostlogt2tb.StartApplication.main(args);
			
			return true;			
		}else if(module.equals("dnnopayloadt2tb")) {
			
			com.itextos.beacon.platform.dnnopayloadt2tb.StartApplication.main(args);
			
			return true;			
		}else if(module.equals("sbc")) {
			
			com.itextos.beacon.platform.sbc.StartApplication.main(args);
			
			return true;			
		}else if(module.equals("t2e")) {
			
			com.itextos.beacon.platform.t2e.StartApplication.main(args);
			
			return true;			
		}else if(module.equals("clienthandovert2tb")) {
			
			com.itextos.beacon.platform.clienthandovert2tb.StartApplication.main(args);
			
			return true;			
		}
		
		return false;
	}

	private static boolean isMW(String module,String[] args) {
		

		if(module.equals("ic")){
			
			com.itextos.beacon.platform.ic.StartApplication.main(args);
			
			return true;
			
		}else if(module.equals("sbcv")){
			
			com.itextos.beacon.platform.sbcv.StartApplication.main(args);
			
			return true;
			
		}else if(module.equals("vc")){
			
			com.itextos.beacon.platform.vc.StartApplication.main(args);
			
			return true;
			
		}else if(module.equals("rc")){
			
			com.itextos.beacon.platform.rc.StartApplication.main(args);
			
			return true;
			
		}else if(module.equals("ch")){
			
			com.itextos.beacon.platform.ch.StartApplication.main(args);
			
			return true;
		}else if(module.equals("rch")){
			
			com.itextos.beacon.platform.rch.StartApplication.main(args);
			
			return true;
		}else if(module.equals("dch")){
			
			com.itextos.beacon.platform.dch.StartApplication.main(args);
			
			return true;
		}
		
		return false;
	}

}
