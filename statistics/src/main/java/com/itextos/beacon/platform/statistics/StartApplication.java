package com.itextos.beacon.platform.statistics;

import java.util.Map;

public class StartApplication {

	public static void main(String arsg[]) {
	
		//HourlyAt15Scheduler.start();
		Map<String,Map<String,String>> cli_id_infomap=MasterData.getCli_idInfoMap();
		Map<String,String> carrier_infomap=MasterData.getCarrierInfoMap();
        HourlyInsert.doProcess(cli_id_infomap,carrier_infomap);
        LatencySubmission.doProcess(cli_id_infomap, carrier_infomap);
        LatencyTelco.doProcess(cli_id_infomap, carrier_infomap);

	}
}
