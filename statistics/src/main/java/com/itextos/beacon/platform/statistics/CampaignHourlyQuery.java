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

public class CampaignHourlyQuery {

	
	static String NULL="##NULL##";

	static String SQL="select a.cli_id cli_id,a.cli_hdr,DATE(a.recv_date) recv_date ,a.sub_cli_sts_code sub_cli_sts_code ,b.dn_ori_sts_code dn_ori_sts_code ,a.intf_type intf_type,a.country country,a.sms_rate,a.dlt_rate,a.billing_currency,a.billing_sms_rate,a.billing_add_fixed_rate,campaign_id,campaign_name,a.file_id,a.file_name,msg_tag,msg_tag1,msg_tag2,msg_tag3,msg_tag4,msg_tag5,count(*) cnt from billing_{0}.submission_{1} a  LEFT OUTER JOIN   billing_{2}.deliveries_{3} b  ON a.msg_id=b.msg_id where campaign_name is not null group by a.cli_id,a.cli_hdr,DATE(a.recv_date) ,a.sub_cli_sts_code  ,b.dn_ori_sts_code  ,a.intf_type ,a.country ,a.sms_rate,a.dlt_rate,a.billing_currency,a.billing_sms_rate,a.billing_add_fixed_rate,campaign_id,campaign_name,a.file_id,a.file_name,msg_tag,msg_tag1,msg_tag2,msg_tag3,msg_tag4,msg_tag5";
	
	
	
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
	

	public static Set<String> getAvailableDays(Map<String,Map<String,Map<String,String>>> data){
		
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
				 String cli_hdr=rs.getString("cli_hdr");
				 String recv_date=rs.getString("recv_date");
				 String sub_cli_sts_code=rs.getString("sub_cli_sts_code");
				 String dn_ori_sts_code=rs.getString("dn_ori_sts_code");
				 String cnt=rs.getString("cnt");
				 String intf_type=rs.getString("intf_type");
				 String country=rs.getString("country");
				 String sms_rate=rs.getString("sms_rate");
				 String dlt_rate=rs.getString("dlt_rate");
				 String billing_currency=rs.getString("billing_currency");
				 String billing_sms_rate=rs.getString("billing_sms_rate");
				 String billing_add_fixed_rate=rs.getString("billing_add_fixed_rate");
				 
				 String campaign_id=rs.getString("campaign_id");
				 String campaign_name=rs.getString("campaign_name");

				 String msg_tag=rs.getString("msg_tag");
				 String msg_tag1=rs.getString("msg_tag1");
				 String msg_tag2=rs.getString("msg_tag2");
				 String msg_tag3=rs.getString("msg_tag3");
				 String msg_tag4=rs.getString("msg_tag4");
				 String msg_tag5=rs.getString("msg_tag5");


				 
				 String file_id=rs.getString("file_id");
				 String file_name=rs.getString("file_name");

				 Map<String,String> data=new HashMap<String,String>();
				 
				 data.put("cli_id", cli_id);
				 data.put("cli_hdr", cli_hdr);
				 data.put("recv_date", recv_date);
				 data.put("sub_cli_sts_code", sub_cli_sts_code);
				 data.put("dn_ori_sts_code", dn_ori_sts_code);
				 data.put("cnt", cnt);

				 data.put("intf_type", intf_type);
				 data.put("country", country);
				 data.put("sms_rate", sms_rate);
				 data.put("dlt_rate", dlt_rate);
				 data.put("billing_currency", billing_currency);
				 data.put("billing_sms_rate", billing_sms_rate);
				 data.put("billing_add_fixed_rate", billing_add_fixed_rate);
				 data.put("campaign_id", campaign_id);
				 data.put("campaign_name", campaign_name);
				 
				 data.put("msg_tag", msg_tag);
				 data.put("msg_tag1", msg_tag1);
				 data.put("msg_tag2", msg_tag2);
				 data.put("msg_tag3", msg_tag3);
				 data.put("msg_tag4", msg_tag4);
				 data.put("msg_tag5", msg_tag5);

				 data.put("file_id", file_id);
				 data.put("file_name", file_name);
				 
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

	
	
	public static  Map<String,Map<String,Map<String,String>>> getHourlyData(Map<String,String> carrier_infomap){
		
		Map<String,Map<String,Map<String,String>>> result=new HashMap<String,Map<String,Map<String,String>>>();
	
	
		int statisticsdays=Integer.parseInt(System.getenv("statisticdays"));
		
		StatisticsLog.log("statisticsdays : "+statisticsdays);

		for(int i=0;i<statisticsdays;i++) {
			
			StatisticsLog.log(getYesterdayQuery(i));
			setHourlyData(result,carrier_infomap,getYesterdayQuery(i));

		}
		
		StatisticsLog.log("result : "+result.size());

		return result;
	}
	
	private static void setHourlyData(Map<String, Map<String, Map<String, String>>> result,Map<String,String> carrier_infomap, String sql) {

		List<Map<String,String>> datalist=getResult( sql);
		
		StatisticsLog.log("datalist : "+datalist.size());
		
		for(int i=0;i<datalist.size();i++) {
			
			

			Map<String,String> data=datalist.get(i);
			
			 String cli_id=data.get("cli_id");	
			 String cli_hdr=data.get("cli_hdr")==null?NULL:data.get("cli_hdr");			
			 String recv_date=data.get("recv_date");		 
			 String sub_cli_sts_code=data.get("sub_cli_sts_code");			 
			 String dn_ori_sts_code=data.get("dn_ori_sts_code")==null?NULL:data.get("dn_ori_sts_code");			 
			 String intf_type=data.get("intf_type")==null?NULL:data.get("intf_type");			 
			 String country=data.get("country")==null?NULL:data.get("country");			 
			 String sms_rate=data.get("sms_rate")==null?NULL:data.get("sms_rate");			 
			 String dlt_rate=data.get("dlt_rate")==null?NULL:data.get("dlt_rate");			 
			 String billing_currency=data.get("billing_currency")==null?NULL:data.get("billing_currency");			 
			 String billing_sms_rate=data.get("billing_sms_rate")==null?NULL:data.get("billing_sms_rate");			 
			 String billing_add_fixed_rate=data.get("billing_add_fixed_rate")==null?NULL:data.get("billing_add_fixed_rate");			 
			 String campaign_id=data.get("campaign_id")==null?NULL:data.get("campaign_id");			 
			 String campaign_name=data.get("campaign_name")==null?NULL:data.get("campaign_name");			 
		
			 String msg_tag=data.get("msg_tag")==null?NULL:data.get("msg_tag");			 
			 String msg_tag1=data.get("msg_tag1")==null?NULL:data.get("msg_tag1");			 
			 String msg_tag2=data.get("msg_tag2")==null?NULL:data.get("msg_tag2");			 
			 String msg_tag3=data.get("msg_tag3")==null?NULL:data.get("msg_tag3");			 
			 String msg_tag4=data.get("msg_tag4")==null?NULL:data.get("msg_tag4");			 
			 String msg_tag5=data.get("msg_tag5")==null?NULL:data.get("msg_tag5");			 

			 String file_id=data.get("file_id")==null?NULL:data.get("file_id");			 
			 String file_name=data.get("file_name")==null?NULL:data.get("file_name");			 
		
			 
			 String key=cli_id+"~"+cli_hdr+"~"+intf_type+"~"+country+"~"+sms_rate+"~"+dlt_rate+"~"+billing_currency+"~"+billing_sms_rate+"~"+billing_add_fixed_rate+"~"+campaign_id+"~"+campaign_name+"~"+msg_tag+"~"+msg_tag1+"~"+msg_tag2+"~"+msg_tag3+"~"+msg_tag4+"~"+msg_tag5+"~"+file_id+"~"+file_name;

			 
			 Map<String, Map<String,  String>> datewisereport= result.get(key);
			 
			 if(datewisereport==null) {
				 
				 datewisereport=new HashMap<String, Map<String, String>>();
				 
				 result.put(key, datewisereport);
			 }

			 

			 Map<String,  String> tablerecord= datewisereport.get(recv_date);
			 
			 if(tablerecord==null) {
				 
				 tablerecord = new HashMap<String,  String>();
				 
				 datewisereport.put(recv_date, tablerecord);
			 }
			 
			


			
				 long cnt=Long.parseLong(data.get("cnt"));

			incrementReceived(tablerecord,cnt);
			incrementSubmit(tablerecord,sub_cli_sts_code,cnt);
			incrementPlatformReject(tablerecord,sub_cli_sts_code,cnt);
			incrementdelivery(tablerecord,dn_ori_sts_code,cnt);
			incrementfailed(tablerecord,dn_ori_sts_code,cnt);
			incrementnulldn(tablerecord,dn_ori_sts_code,cnt);
			incrementOutofCredit(tablerecord,sub_cli_sts_code,cnt);
			incrementdnexpired(tablerecord,dn_ori_sts_code,cnt);
			resetdnpercentage(tablerecord);


	 
		}
	}

	private static void incrementdnexpired(Map<String, String> tablerecord, String dn_ori_sts_code, long cnt) {
		





		
		 String received= tablerecord.get("dnexpired");
		 
		 if(received==null) {
			 
			 tablerecord.put("dnexpired", "0");
			 received= tablerecord.get("dnexpired");
		 }
		 
		 long lReceived=Long.parseLong(received);
		 
		 if(dn_ori_sts_code!=null &&dn_ori_sts_code.equals("699")){
		 tablerecord.put("dnexpired", ""+(cnt+lReceived));
		 }
			
			
	
		
	}





	private static void incrementOutofCredit(Map<String, String> tablerecord, String sub_cli_sts_code, long cnt) {



		
		 String received= tablerecord.get("outofcredit");
		 
		 if(received==null) {
			 
			 tablerecord.put("outofcredit", "0");
			 received= tablerecord.get("outofcredit");
		 }
		 
		 long lReceived=Long.parseLong(received);
		 
		 if(sub_cli_sts_code!=null&&sub_cli_sts_code.equals("445")) {
			 tablerecord.put("outofcredit", ""+(cnt+lReceived));
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
		 
		 if(dn_ori_sts_code==null ||dn_ori_sts_code.trim().length()<1||"##NULL##".equals(dn_ori_sts_code)){
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
		 
 if(dn_ori_sts_code!=null&&dn_ori_sts_code.trim().length()>0) {
			 
			 if("##NULL##".equals(dn_ori_sts_code)) {
				 
			 }else {
			 int d=Integer.parseInt(dn_ori_sts_code);
			 
			 if(d>600) {
				 	tablerecord.put("failed", ""+(cnt+lReceived));
			 }
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
		 
		 if(sub_cli_sts_code!=null&&!sub_cli_sts_code.equals("400")&&!sub_cli_sts_code.equals("600")) {
			 
			 
			 int d=Integer.parseInt(sub_cli_sts_code);
			 
			 if(d<600) {
				 	tablerecord.put("platformreject", ""+(cnt+lReceived));
			 }
			 }
	}

	private static  void incrementSubmit(Map<String, String> tablerecord, String sub_cli_sts_code,long cnt) {


		
		 String received= tablerecord.get("submit");
		 
		 if(received==null) {
			 
			 tablerecord.put("submit", "0");
			 received= tablerecord.get("submit");
		 }
		 
		 long lReceived=Long.parseLong(received);
		 
		 if(sub_cli_sts_code!=null&&sub_cli_sts_code.equals("400")) {
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
