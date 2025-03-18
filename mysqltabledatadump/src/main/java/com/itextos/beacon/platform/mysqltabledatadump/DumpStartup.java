package com.itextos.beacon.platform.mysqltabledatadump;


public class DumpStartup extends Thread{
	
	
	public static void main(String args[]) {
		
		Dump a=new Dump();
		
	       Thread.ofVirtual().start(new DumpStartup());

	}
	
	public void run() {
		
		while(true) {
			
			mysqlDump();
			
			gotosleep();
		}
}

	private void mysqlDump() {
		
		Dump a=new Dump();
		a.takedump("accounts",Table.ACCOUNTS);
		a.takedump("carrier_handover",Table.CARRIER_HANDOVER);
		a.takedump("accounts",Table.ACCOUNTS);
		a.takedump("client_handover",Table.CLIENT_HANDOVER);
		a.takedump("cm",Table.CM);
		a.takedump("configuration",Table.CONFIGURATION);
		a.takedump("imp",Table.IMP);
		a.takedump("listing",Table.LISTING);
		a.takedump("logging",Table.LOGGING);
		a.takedump("messaging",Table.MESSAGING);
		a.takedump("payload",Table.PAYLOAD);
		a.takedump("r3c",Table.R3C);
		a.takedump("sysconfig",Table.R3C);

	}

	private void gotosleep() {

		try {
			Thread.sleep(60*60*1000);
		}catch(Exception e) {

		}
		
	}
	
}
