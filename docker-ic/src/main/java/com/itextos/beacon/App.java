package com.itextos.beacon;

public class App {

	public static void main(String[] args) {
		
		String module=System.getenv("module");


		if(module.equals("ic")){
			
			com.itextos.beacon.platform.iccore.StartApplication.main(args);
			
		}else if(module.equals("sbcv")){
			
			com.itextos.beacon.platform.sbcvcore.StartApplication.main(args);
			
		}else if(module.equals("vc")){
			
			com.itextos.beacon.platform.vccore.StartApplication.main(args);
			
		}else if(module.equals("ch")){
			
			com.itextos.beacon.platform.chcore.StartApplication.main(args);
			
		}else if(module.equals("wc")){
			
			com.itextos.beacon.platform.walletconsumer.StartApplication.main(args);
			
		}

	}

}
