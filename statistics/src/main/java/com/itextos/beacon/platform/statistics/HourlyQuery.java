package com.itextos.beacon.platform.statistics;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.itextos.beacon.commonlib.commondbpool.DBDataSourceFactory;
import com.itextos.beacon.commonlib.constants.ErrorMessage;
import com.itextos.beacon.commonlib.constants.Table2DBInserterId;
import com.itextos.beacon.inmemory.loader.InmemoryLoaderCollection;
import com.itextos.beacon.inmemory.loader.process.InmemoryId;
import com.itextos.beacon.platform.topic2table.dbinfo.TableInserterInfo;
import com.itextos.beacon.platform.topic2table.dbinfo.TableInserterInfoCollection;

public class HourlyQuery {

	
	
	static String SQL="select a.cli_id cli_id,a.smsc_id smsc_id ,DATE(a.recv_date) recv_date ,HOUR(a.recv_time) recv_hour,a.sub_cli_sts_code sub_cli_sts_code ,b.dn_ori_sts_code dn_ori_sts_code ,a.msg_type msg_type,count(*) cnt from billing_{0}.submission_{1} a  LEFT OUTER JOIN "
			+ "  billing_{2}.deliveries_{3} b "
			+ " ON a.msg_id=b.msg_id group by a.cli_id,a.smsc_id,DATE(a.recv_date),HOUR(a.recv_time),a.sub_cli_sts_code,b.dn_ori_sts_code,a.msg_type";
	
	
	
	private static String getYesterdayQuery(int days) {
		
		
		String formattedSQL2 = MessageFormat.format(SQL, getYesderdayMonthString(days), getYesderdayString(days), getYesderdayMonthString(days), getYesderdayString(days));

	    return formattedSQL2;
	}
	
	
	
	
	
	private static String getYesderdayString(int days) {
		
		SimpleDateFormat sdf=new SimpleDateFormat("yyyyMMdd");
		
		return sdf.format(new Date(System.currentTimeMillis()-(days*24*60*60*1000)));
		
	}
	
	private static String getYesderdayMonthString(int days) {
		
		SimpleDateFormat sdf=new SimpleDateFormat("yyyyMM");
		
		return sdf.format(new Date(System.currentTimeMillis()-(days*24*60*60*1000)));
		
	}
	

	public static Set<String> getAvailableDays(Map<String,Map<String,Map<String,Map<String,String>>>> data){
		
		int days=Integer.parseInt(System.getenv("statisticdays"));

		Set<String> result=new HashSet<String>();
		
		data.forEach((cli_id,datedata)->{
			
			datedata.forEach((datestring,hourdata)->{
				
				result.add(datestring);
				
			});
			
			if(result.size()==days) {
				
				return ;
				
			}
		});
		
		return result;
	}
	
	
	private static  List<Map<String,String>> getResult(String sql) {

		List<Map<String,String>> result=new ArrayList<Map<String,String>>();
		
		Connection con=null;
		PreparedStatement pstmt=null;
		ResultSet rs=null;
		
		try {
			
			 final TableInserterInfoCollection tiic = (TableInserterInfoCollection) InmemoryLoaderCollection.getInstance().getInmemoryCollection(InmemoryId.TABLE_INSERTER_INFO);

			 TableInserterInfo  mTableInserterInfo = tiic.getTableInserterInfo(Table2DBInserterId.DELIVERIES);


			 con = DBDataSourceFactory.getConnection(mTableInserterInfo.getJndiInfo());
		 
			 
			 pstmt=con.prepareStatement(sql);
			 
			 rs=pstmt.executeQuery();
			 
			 while(rs.next()) {
				 
				 
				 String cli_id=rs.getString("cli_id");
				 String smsc_id=rs.getString("smsc_id");
				 String msg_type=rs.getString("msg_type");

				 String recv_date=rs.getString("recv_date");
				 String recv_hour=rs.getString("recv_hour");
				 String sub_cli_sts_code=rs.getString("sub_cli_sts_code");
				 String dn_ori_sts_code=rs.getString("dn_ori_sts_code");
				 String cnt=rs.getString("cnt");

				 Map<String,String> data=new HashMap<String,String>();
				 
				 data.put("cli_id", cli_id);
				 data.put("smsc_id", smsc_id);
				 data.put("recv_date", recv_date);
				 data.put("recv_hour", recv_hour);
				 data.put("sub_cli_sts_code", sub_cli_sts_code);
				 data.put("dn_ori_sts_code", dn_ori_sts_code);
				 data.put("cnt", cnt);
				 data.put("msg_type", msg_type);

				 result.add(data);

			 }
		
		 
		
		 
	
			
		}catch(Exception e) {
			
			StatisticsLog.log("error : "+ErrorMessage.getStackTraceAsString(e));

			
		}finally {
			
			if(rs!=null) {
				try {
				rs.close();
				}catch(Exception e) {}
			}
			
			if(pstmt!=null) {
				try {
					pstmt.close();
				}catch(Exception e) {}
			}
			
			
			
			if(con!=null) {
				try {
				con.close();
				}catch(Exception e) {}
			}
			
		}
		
		
		return result;
	}

	
	
	public static  Map<String,Map<String,Map<String,Map<String,String>>>> getHourlyData(Map<String,String> carrier_infomap){
		
		Map<String,Map<String,Map<String,Map<String,String>>>> result=new HashMap<String,Map<String,Map<String,Map<String,String>>>>();
	
	
		int statisticsdays=Integer.parseInt(System.getenv("statisticdays"));
		
		StatisticsLog.log("statisticsdays : "+statisticsdays);

		for(int i=0;i<statisticsdays;i++) {
			
			StatisticsLog.log(getYesterdayQuery(i));
			setHourlyData(result,carrier_infomap,getYesterdayQuery(i));

		}
		
		StatisticsLog.log("result : "+result.size());

		return result;
	}
	
	private static void setHourlyData(Map<String, Map<String, Map<String, Map<String, String>>>> result,Map<String,String> carrier_infomap, String sql) {

		List<Map<String,String>> datalist=getResult( sql);
		
		StatisticsLog.log("datalist : "+datalist.size());
		
		for(int i=0;i<datalist.size();i++) {
			
			Map<String,String> data=datalist.get(i);
			
			 String cli_id=data.get("cli_id");
			 
			 String smsc_id=data.get("smsc_id");
			 if(smsc_id==null) {
				 
				 smsc_id="dontdid";
			 }

				
				
				final String carriername=carrier_infomap.get(smsc_id)==null?" ":carrier_infomap.get(smsc_id);

			 String msg_type=data.get("msg_type");

			 
			 Map<String, Map<String, Map<String, String>>> datewisereport= result.get(cli_id+"~"+carriername);
			 
			 if(datewisereport==null) {
				 
				 datewisereport=new HashMap<String, Map<String, Map<String, String>>>();
				 
				 result.put(cli_id+"~"+carriername, datewisereport);
			 }

			 
			 String recv_date=data.get("recv_date");

			 Map<String, Map<String, String>> hourwisereport= datewisereport.get(recv_date);
			 
			 if(hourwisereport==null) {
				 
				 hourwisereport = new HashMap<String, Map<String, String>>();
				 
				 datewisereport.put(recv_date, hourwisereport);
			 }
			 
			 String recv_hour=data.get("recv_hour");

			 
			  Map<String, String> tablerecord= hourwisereport.get(recv_hour);
			  
			  if(tablerecord==null) {
				  
				  tablerecord=new HashMap<String,String>();
				  
				  hourwisereport.put(recv_hour, tablerecord);
			  }


				 String sub_cli_sts_code=data.get("sub_cli_sts_code");
				 String dn_ori_sts_code=data.get("dn_ori_sts_code");
				 long cnt=Long.parseLong(data.get("cnt"));

			incrementReceived(tablerecord,cnt);
			incrementSubmit(tablerecord,sub_cli_sts_code,cnt);
			incrementPlatformReject(tablerecord,sub_cli_sts_code,cnt);
			incrementnonPromoSubCount(tablerecord,sub_cli_sts_code,msg_type,cnt);
			incrementdelivery(tablerecord,dn_ori_sts_code,cnt);
			incrementfailed(tablerecord,dn_ori_sts_code,cnt);
			incrementnulldn(tablerecord,dn_ori_sts_code,cnt);
			resetdnpercentage(tablerecord);
			
	 
		}
	}

	public static void resetdnpercentage(Map<String, String> tablerecord) {
		
		String delivery=tablerecord.get("delivery")==null?"0":tablerecord.get("delivery");
		String failed=tablerecord.get("failed")==null?"0":tablerecord.get("failed");
		String submit=tablerecord.get("submit")==null?"0":tablerecord.get("submit");

		double lDelivery=Long.parseLong(delivery);
		double lFailed=Long.parseLong(failed);
		double lSubmit=Long.parseLong(submit);

		double totalDN=lDelivery+lFailed;
		
		double dnPercentage=0D;
		if(totalDN==0||lSubmit==0) {
			
		}else {
			dnPercentage=(totalDN/lSubmit)*100;

		}
		
		tablerecord.put("dnpercentage",""+dnPercentage);
		
	}

	private static  void incrementnulldn(Map<String, String> tablerecord, String dn_ori_sts_code,long cnt) {




		
		 String received= tablerecord.get("nulldn");
		 
		 if(received==null) {
			 
			 tablerecord.put("nulldn", "0");
			 received= tablerecord.get("nulldn");
		 }
		 
		 long lReceived=Long.parseLong(received);
		 
		 if(dn_ori_sts_code==null ){
		 tablerecord.put("nulldn", ""+(cnt+lReceived));
		 }
			
			
	}

	private static  void incrementfailed(Map<String, String> tablerecord, String dn_ori_sts_code,long cnt) {




		
		 String received= tablerecord.get("failed");
		 
		 if(received==null) {
			 
			 tablerecord.put("failed", "0");
			 received= tablerecord.get("failed");
		 }
		 
		 long lReceived=Long.parseLong(received);
		 
		 if(dn_ori_sts_code!=null) {
			 
			 int d=Integer.parseInt(dn_ori_sts_code);
			 
			 if(d>600) {
				 	tablerecord.put("failed", ""+(cnt+lReceived));
			 }
		 }
			
			
	}

	private static  void incrementdelivery(Map<String, String> tablerecord, String dn_ori_sts_code,long cnt) {



		
		 String received= tablerecord.get("delivery");
		 
		 if(received==null) {
			 
			 tablerecord.put("delivery", "0");
			 received= tablerecord.get("delivery");
		 }
		 
		 long lReceived=Long.parseLong(received);
		 
		 if(dn_ori_sts_code!=null&&dn_ori_sts_code.equals("600")) {
		 tablerecord.put("delivery", ""+(cnt+lReceived));
		 }
			
	}

	private  static void incrementnonPromoSubCount(Map<String, String> tablerecord, String sub_cli_sts_code, String msg_type,long cnt) {




		
		 String received= tablerecord.get("nonpromosubmit");
		 
		 if(received==null) {
			 
			 tablerecord.put("nonpromosubmit", "0");
			 received= tablerecord.get("nonpromosubmit");
		 }
		 
		 long lReceived=Long.parseLong(received);
		 
		 if(sub_cli_sts_code.equals("400")&& (msg_type!=null&&!msg_type.equals("0"))) {
		 tablerecord.put("nonpromosubmit			", ""+(cnt+lReceived));
		 }
	
	}

	private  static void incrementPlatformReject(Map<String, String> tablerecord, String sub_cli_sts_code,long cnt) {


		
		 String received= tablerecord.get("platformreject");
		 
		 if(received==null) {
			 
			 tablerecord.put("platformreject", "0");
			 received= tablerecord.get("platformreject");
		 }
		 
		 long lReceived=Long.parseLong(received);
		 
		 if(!sub_cli_sts_code.equals("400")) {
		 tablerecord.put("platformreject", ""+(cnt+lReceived));
		 }
	}

	private static  void incrementSubmit(Map<String, String> tablerecord, String sub_cli_sts_code,long cnt) {


		
		 String received= tablerecord.get("submit");
		 
		 if(received==null) {
			 
			 tablerecord.put("submit", "0");
			 received= tablerecord.get("submit");
		 }
		 
		 long lReceived=Long.parseLong(received);
		 
		 if(sub_cli_sts_code.equals("400")) {
		 tablerecord.put("submit", ""+(cnt+lReceived));
		 }
	}

	private static  void incrementReceived(Map<String, String> tablerecord,long cnt) {
		
		
		 String received= tablerecord.get("received");
		 
		 if(received==null) {
			 
			 tablerecord.put("received", "0");
			 received= tablerecord.get("received");
		 }
		 
		 long lReceived=Long.parseLong(received);
		 tablerecord.put("received", ""+(cnt+lReceived));

		
	}

	public static void main(String args[]) {
		
		
		
       
       

	}

}
