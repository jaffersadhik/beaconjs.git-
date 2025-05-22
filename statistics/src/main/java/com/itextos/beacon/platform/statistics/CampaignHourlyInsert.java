package com.itextos.beacon.platform.statistics;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.BatchUpdateException;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.UUID;

import com.itextos.beacon.commonlib.constants.ErrorMessage;
import com.itextos.beacon.commonlib.utility.CommonUtility;
import com.itextos.beacon.mysqlimport.DBConnection;

public class CampaignHourlyInsert {

	public static void doProcess(Map<String,Map<String,String>> cli_id_infomap,Map<String,String> carrier_infomap) {
		
		long start=System.currentTimeMillis();
		
		Map<String,Map<String,Map<String,String>>> hourlyStatisticsdata=CampaignHourlyQuery.getHourlyData(carrier_infomap);
		
		
		long end=System.currentTimeMillis();
		
		StatisticsLog.log("Total Time Query : "+((end-start)/1000)+" seconds");

		Set<String> dateset=CampaignHourlyQuery.getAvailableDays(hourlyStatisticsdata);
		
		StatisticsLog.log("dateset : "+dateset);



		start=System.currentTimeMillis();
		
		doMixTrafficInsert(dateset,cli_id_infomap,carrier_infomap,hourlyStatisticsdata);
		
	
		
		end=System.currentTimeMillis();
		
		StatisticsLog.log("Total Time Insert : "+((end-start)/1000)+" seconds");

		
	}

	private static void doTrafficInsert(Map<String, Map<String, String>> cli_id_infomap, Set<String> dateset,
			Map<String, Map<String, Map<String, String>>> daywiseStatisticsdata) {
		
		

		
		
       	Connection con =null;
		
		try {
			con=DBConnection.getConnectionPostgresStatistics();
			con.setAutoCommit(false);
			delete(con,dateset,"summary.ui_traffic_report");
			insertTraffic(con,cli_id_infomap,daywiseStatisticsdata);
			con.commit();
			
		}catch(Exception e) {
			StatisticsLog.log("error : "+ErrorMessage.getStackTraceAsString(e));
		}finally {
            CommonUtility.closeConnection(con);
     
        }
		
	
		
	}

	private static void insertTraffic(Connection con, Map<String, Map<String, String>> cli_id_infomap, Map<String, Map<String, Map<String, String>>> daywiseStatisticsdata) {
		
	  	String sql="insert into summary.ui_traffic_report(id,recv_date,cli_id,cli_hdr,"
    			+ "intf_type,country,sms_rate,"
    			+ "dlt_rate,billing_currency,billing_sms_rate,billing_add_fixed_rate,"
    			+ "tot_cnt,submitted_cnt,delivered_cnt,dn_failed_cnt,"
    			+ "dn_expired_cnt,rejected_cnt,out_of_credits_cnt,dn_pending_cnt)values("
    			+ "?,?,?,?,"
    			+ "?,?,?,"
    			+ "?,?,?,?,"
    			+ "?,?,?,?,"
    			+ "?,?,?,?)";



    	PreparedStatement pstmt = null;
    	ResultSet rs=null;

    		try {
    		
    		pstmt=con.prepareStatement(sql);
    		addTraffic(cli_id_infomap,daywiseStatisticsdata,pstmt);
    		int [] result=pstmt.executeBatch();
    		int count =getCount(result);
    		StatisticsLog.log("ui_traffic_report inserted : "+count);

    	}catch(Exception e) {
			StatisticsLog.log("error : "+ErrorMessage.getStackTraceAsString(e));
			
			if(e instanceof BatchUpdateException ) {
				nextExceptionLog((BatchUpdateException)e);
			}

    	}finally {
            CommonUtility.closeResultSet(rs);	
            CommonUtility.closeStatement(pstmt);
     
        }
		
	
		
	}

	
	private static Map<String, Map<String, Map<String, String>>> getTraffic(
			Map<String, Map<String, Map<String,  String>>> hourlyStatisticsdata,Map<String,String> carrier_infomap) {
		
		Map<String, Map<String, Map<String, String>>> result=new HashMap<String, Map<String, Map<String, String>>>();
		
		hourlyStatisticsdata.forEach((key,receivedatedata)->{
			
			StringTokenizer st=new StringTokenizer(key,"~");

			String cli_id=st.nextToken();
			String cli_hdr=st.nextToken();
			String intf_type=st.nextToken();
			String country=st.nextToken();
			String sms_rate=st.nextToken();
			String dlt_rate=st.nextToken();
			String billing_currency=st.nextToken();
			String billing_sms_rate=st.nextToken();
			String billing_add_fixed_rate=st.nextToken();
	
			 String resultkey=cli_id+"~"+cli_hdr+"~"+intf_type+"~"+country+"~"+sms_rate+"~"+dlt_rate+"~"+billing_currency+"~"+billing_sms_rate+"~"+billing_add_fixed_rate;

			final Map<String, Map<String, String>> resultreceivedatedata=result.get(resultkey)==null?new HashMap<String, Map<String, String>>():result.get(resultkey);
			
			result.put(resultkey, resultreceivedatedata);
		
			
			receivedatedata.forEach((receivedate,hourdata)->{
				
				final Map<String, String> data=resultreceivedatedata.get(receivedate)==null?new HashMap<String,String>():resultreceivedatedata.get(receivedate);
			
				resultreceivedatedata.put(receivedate, data);
					
					add(hourdata,data);
	
		});
		});
		return result;
	}

	private static void add(Map<String, String> hourdata, Map<String, String> data) {
		
		
		
	
		long lReceived=data.get("received")==null?0L:Long.parseLong(data.get("received"));
		long lSubmit=data.get("submit")==null?0L:Long.parseLong(data.get("submit"));
		long lNonpromosubmit=data.get("nonpromosubmit")==null?0L:Long.parseLong(data.get("nonpromosubmit"));
		long lDelivery=data.get("delivery")==null?0L:Long.parseLong(data.get("delivery"));
		long lFailed=data.get("failed")==null?0L:Long.parseLong(data.get("failed"));
		long lPlatformreject=data.get("platformreject")==null?0L:Long.parseLong(data.get("platformreject"));
		long lNulldn=data.get("nulldn")==null?0L:Long.parseLong(data.get("nulldn"));
		long dnexpired=data.get("dnexpired")==null?0L:Long.parseLong(data.get("dnexpired"));
		long outofcredit=data.get("outofcredit")==null?0L:Long.parseLong(data.get("outofcredit"));

		data.put("received", ""+(lReceived+Long.parseLong(hourdata.get("received")==null?"0":hourdata.get("received"))));
		data.put("submit", ""+(lSubmit+Long.parseLong(hourdata.get("submit")==null?"0":hourdata.get("submit"))));
		data.put("delivery", ""+(lDelivery+Long.parseLong(hourdata.get("delivery")==null?"0":hourdata.get("delivery"))));
		data.put("nonpromosubmit", ""+(lNonpromosubmit+Long.parseLong(hourdata.get("nonpromosubmit")==null?"0":hourdata.get("nonpromosubmit"))));
		data.put("failed", ""+(lFailed+Long.parseLong(hourdata.get("failed")==null?"0":hourdata.get("failed"))));
		data.put("platformreject", ""+(lPlatformreject+Long.parseLong(hourdata.get("platformreject")==null?"0":hourdata.get("platformreject"))));
		data.put("nulldn", ""+(lNulldn+Long.parseLong(hourdata.get("nulldn")==null?"0":hourdata.get("nulldn"))));
		data.put("dnexpired", ""+(dnexpired+Long.parseLong(hourdata.get("dnexpired")==null?"0":hourdata.get("dnexpired"))));
		data.put("outofcredit", ""+(outofcredit+Long.parseLong(hourdata.get("outofcredit")==null?"0":hourdata.get("outofcredit"))));

		

		HourlyQuery.resetdnpercentage(data);
	}

	private static void doMixTrafficInsert(Set<String> dateset, Map<String, Map<String, String>> cli_id_infomap,
			Map<String, String> carrier_infomap,
			Map<String, Map<String, Map<String, String>>> hourlyStatisticsdata) {
		
		
       	Connection con =null;
		
		try {
			con=DBConnection.getConnectionPostgresStatistics();
			con.setAutoCommit(false);
			delete(con,dateset,"summary.ui_camp_report");
			insertMixTraffic(con,cli_id_infomap,carrier_infomap,hourlyStatisticsdata);
			con.commit();
			
		}catch(Exception e) {
			StatisticsLog.log("error : "+ErrorMessage.getStackTraceAsString(e));
		}finally {
            CommonUtility.closeConnection(con);
     
        }
		
	}

	private static void insertMixTraffic(Connection con, Map<String, Map<String, String>> cli_id_infomap,
			Map<String, String> carrier_infomap,
			Map<String, Map<String, Map<String,  String>>> hourlyStatisticsdata) {
    	PreparedStatement pstmt = null;

    	String sql="insert into summary.ui_camp_report(id,recv_date,cli_id,cli_hdr,"
    			+ "campaign_id,campaign_name,intf_type,country,sms_rate,"
    			+ "dlt_rate,billing_currency,billing_sms_rate,billing_add_fixed_rate,"
    			+ "tot_cnt,submitted_cnt,delivered_cnt,dn_failed_cnt,"
    			+ "dn_expired_cnt,rejected_cnt,out_of_credits_cnt,dn_pending_cnt,"
    			+ "msg_tag,msg_tag1,msg_tag2,msg_tag3,"
    			+ "msg_tag4,msg_tag5,file_id,file_name )values("
    			+ "?,?,?,?,"
    			+ "?,?,?,?,?,"
    			+ "?,?,?,?,"
    			+ "?,?,?,?,"
    			+ "?,?,?,?,"
    			+ "?,?,?,?,"
    			+ "?,?,?,?)";
    			
    	try {
    		
    		pstmt=con.prepareStatement(sql);
    		addTrafficMix(cli_id_infomap,carrier_infomap,hourlyStatisticsdata,pstmt);
    		int [] result=pstmt.executeBatch();
    		int count =getCount(result);
    		StatisticsLog.log("ui_camp_report inserted : "+count);

    	}catch(Exception e) {
			StatisticsLog.log("error : "+ErrorMessage.getStackTraceAsString(e));
			
			if(e instanceof BatchUpdateException ) {
				nextExceptionLog((BatchUpdateException)e);
			}

    	}finally {
            CommonUtility.closeStatement(pstmt);
     
        }
		
	}

	private static void nextExceptionLog(BatchUpdateException bue) {
		
		    SQLException nextEx = bue.getNextException();
		    if (nextEx != null) {
				StatisticsLog.log("error : "+ErrorMessage.getStackTraceAsString(nextEx));

		    }
		
	}

	private static int getCount(int[] result) {
		int count=0;
		for(int i=0;i<result.length;i++){
			
			count+=result[i];
		}
		return count;
	}

	private static void addTrafficMix(Map<String, Map<String, String>> cli_id_infomap, Map<String, String> carrier_infomap,
			Map<String, Map<String, Map<String, String>>> hourlyStatisticsdata, final PreparedStatement pstmt) {

    	SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd");
    

		hourlyStatisticsdata.forEach((key,datedata)->{
			
			StringTokenizer st=new StringTokenizer(key,"~");

			String cli_id=st.nextToken();
			String cli_hdr=st.nextToken();
			String intf_type=st.nextToken();
			String country=st.nextToken();
			String sms_rate=st.nextToken();
			String dlt_rate=st.nextToken();
			String billing_currency=st.nextToken();
			String billing_sms_rate=st.nextToken();
			String billing_add_fixed_rate=st.nextToken();
			String campaign_id=st.nextToken();
			String campaign_name=st.nextToken();

			String msg_tag=st.nextToken();
			String msg_tag1=st.nextToken();
			String msg_tag2=st.nextToken();
			String msg_tag3=st.nextToken();
			String msg_tag4=st.nextToken();
			String msg_tag5=st.nextToken();
			String file_id=st.nextToken();
			String file_name=st.nextToken();

				datedata.forEach((datestring,data)->{
					
					try {
						Date receiveDate=new Date(sdf.parse(datestring).getTime());
						
							
							try {
								pstmt.setString(1, UUID.randomUUID().toString());
								pstmt.setDate(2, receiveDate);
								pstmt.setLong(3, Long.parseLong(cli_id));								
								pstmt.setString(4, cli_hdr);
								pstmt.setString(5, campaign_id);
								pstmt.setString(6, campaign_name);
								pstmt.setString(7, intf_type);
								pstmt.setString(8, country);

								double smsrateD = sms_rate.trim().length()<1?0D:Double.parseDouble(sms_rate);
								if(smsrateD==0) {
									
									pstmt.setDouble(9,0D)	;

								}else {
									
									BigDecimal rounded = new BigDecimal(smsrateD).setScale(4, RoundingMode.HALF_UP);
									double result = rounded.doubleValue();
									pstmt.setDouble(9,result)	;
								}
								
								

								double dltrateD = dlt_rate.trim().length()<1?0D:Double.parseDouble(dlt_rate);
								if(dltrateD==0) {
									
									pstmt.setDouble(10,0D)	;

								}else {
									
									BigDecimal rounded = new BigDecimal(dltrateD).setScale(4, RoundingMode.HALF_UP);
									double result = rounded.doubleValue();
									pstmt.setDouble(10,result)	;
								}
					
								
								pstmt.setString(11, billing_currency);

								
								double billingsmsrateD = billing_sms_rate.trim().length()<1?0D:Double.parseDouble(billing_sms_rate);
								if(billingsmsrateD==0) {
									
									pstmt.setDouble(12,0D)	;

								}else {
									
									BigDecimal rounded = new BigDecimal(billingsmsrateD).setScale(12, RoundingMode.HALF_UP);
									double result = rounded.doubleValue();
									pstmt.setDouble(12,result)	;
								}
								
								

								double billingdltrateD = billing_add_fixed_rate.trim().length()<1?0D:Double.parseDouble(billing_add_fixed_rate);
								if(billingdltrateD==0) {
									
									pstmt.setDouble(13,0D)	;

								}else {
									
									BigDecimal rounded = new BigDecimal(billingdltrateD).setScale(12, RoundingMode.HALF_UP);
									double result = rounded.doubleValue();
									pstmt.setDouble(13,result)	;
								}
								
								
								pstmt.setLong(14, Long.parseLong(data.get("received")));								
								pstmt.setLong(15, Long.parseLong(data.get("submit")));								
								pstmt.setLong(16, Long.parseLong(data.get("delivery")));								
								pstmt.setLong(17, Long.parseLong(data.get("failed")));								
								pstmt.setLong(18, Long.parseLong(data.get("dnexpired")));
								pstmt.setLong(19, Long.parseLong(data.get("platformreject")));
								pstmt.setLong(20, Long.parseLong(data.get("outofcredit")));
								pstmt.setLong(21, Long.parseLong(data.get("nulldn")));

								pstmt.setString(22, msg_tag);
								pstmt.setString(23, msg_tag1);
								pstmt.setString(24, msg_tag2);
								pstmt.setString(25, msg_tag3);
								pstmt.setString(26, msg_tag4);
								pstmt.setString(27, msg_tag5);
								pstmt.setString(28, file_id);
								pstmt.setString(29, file_name);

								pstmt.addBatch();

							} catch (Exception e) {
								StatisticsLog.log("error : "+ErrorMessage.getStackTraceAsString(e));

							}
					
					} catch (Exception e) {
						StatisticsLog.log("error : "+ErrorMessage.getStackTraceAsString(e));

					}
				});
				
			
		});
	}

	
	private static void addTraffic(Map<String, Map<String, String>> cli_id_infomap, 
			Map<String, Map<String, Map<String, String>>> hourlyStatisticsdata, final PreparedStatement pstmt) {

    	SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd");
    

		hourlyStatisticsdata.forEach((key,datedata)->{
			
			StringTokenizer st=new StringTokenizer(key,"~");

			String cli_id=st.nextToken();
			String cli_hdr=st.nextToken();
			String intf_type=st.nextToken();
			String country=st.nextToken();
			String sms_rate=st.nextToken();
			String dlt_rate=st.nextToken();
			String billing_currency=st.nextToken();
			String billing_sms_rate=st.nextToken();
			String billing_add_fixed_rate=st.nextToken();
		
	    	
			
				datedata.forEach((datestring,data)->{
					
					try {
						Date receiveDate=new Date(sdf.parse(datestring).getTime());
						
							
							try {
								pstmt.setString(1, UUID.randomUUID().toString());
								pstmt.setDate(2, receiveDate);
								pstmt.setLong(3, Long.parseLong(cli_id));								
								pstmt.setString(4, cli_hdr);
								pstmt.setString(5, intf_type);
								pstmt.setString(6, country);

								double smsrateD = sms_rate.trim().length()<1?0D:Double.parseDouble(sms_rate);
								if(smsrateD==0) {
									
									pstmt.setDouble(7,0D)	;

								}else {
									
									BigDecimal rounded = new BigDecimal(smsrateD).setScale(4, RoundingMode.HALF_UP);
									double result = rounded.doubleValue();
									pstmt.setDouble(7,result)	;
								}
								
								

								double dltrateD = dlt_rate.trim().length()<1?0D:Double.parseDouble(dlt_rate);
								if(dltrateD==0) {
									
									pstmt.setDouble(8,0D)	;

								}else {
									
									BigDecimal rounded = new BigDecimal(dltrateD).setScale(4, RoundingMode.HALF_UP);
									double result = rounded.doubleValue();
									pstmt.setDouble(8,result)	;
								}
					
								
								pstmt.setString(9, billing_currency);

								
								double billingsmsrateD = billing_sms_rate.trim().length()<1?0D:Double.parseDouble(billing_sms_rate);
								if(billingsmsrateD==0) {
									
									pstmt.setDouble(10,0D)	;

								}else {
									
									BigDecimal rounded = new BigDecimal(billingsmsrateD).setScale(12, RoundingMode.HALF_UP);
									double result = rounded.doubleValue();
									pstmt.setDouble(10,result)	;
								}
								
								

								double billingdltrateD = billing_add_fixed_rate.trim().length()<1?0D:Double.parseDouble(billing_add_fixed_rate);
								if(billingdltrateD==0) {
									
									pstmt.setDouble(11,0D)	;

								}else {
									
									BigDecimal rounded = new BigDecimal(billingdltrateD).setScale(12, RoundingMode.HALF_UP);
									double result = rounded.doubleValue();
									pstmt.setDouble(11,result)	;
								}
								
								
								pstmt.setLong(12, Long.parseLong(data.get("received")));								
								pstmt.setLong(13, Long.parseLong(data.get("submit")));								
								pstmt.setLong(14, Long.parseLong(data.get("delivery")));								
								pstmt.setLong(15, Long.parseLong(data.get("failed")));								
								pstmt.setLong(16, Long.parseLong(data.get("dnexpired")));
								pstmt.setLong(17, Long.parseLong(data.get("platformreject")));
								pstmt.setLong(18, Long.parseLong(data.get("outofcredit")));
								pstmt.setLong(19, Long.parseLong(data.get("nulldn")));


								pstmt.addBatch();

							} catch (Exception e) {
								StatisticsLog.log("error : "+ErrorMessage.getStackTraceAsString(e));

							}
					
					} catch (Exception e) {
						StatisticsLog.log("error : "+ErrorMessage.getStackTraceAsString(e));

					}
				});
				
			
		});
	}
	
	private static void delete(Connection con, Set<String> dateset,String tablename) {
    	PreparedStatement pstmt = null;

    	String sql="delete from "+tablename+" where recv_date=?";
    	try {
    		
    		pstmt=con.prepareStatement(sql);
    		addDate(dateset,pstmt);
    		pstmt.executeBatch();
    		
    	}catch(Exception e) {
			StatisticsLog.log("error : "+ErrorMessage.getStackTraceAsString(e));

    	}finally {
            CommonUtility.closeStatement(pstmt);
     
        }
		
	}

	private static void addDate(Set<String> dateset,final PreparedStatement pstmt) {
    	SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd");

		dateset.forEach((datestrng)->{
			
			try {
				pstmt.setDate(1,new Date(sdf.parse(datestrng).getTime()));
				pstmt.addBatch();
			} catch (Exception e) {
				StatisticsLog.log("error : "+ErrorMessage.getStackTraceAsString(e));

			}
		});
		
		
	}
}
