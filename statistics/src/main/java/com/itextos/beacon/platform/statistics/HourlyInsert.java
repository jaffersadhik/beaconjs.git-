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

public class HourlyInsert {

	public static void doProcess() {
		
		long start=System.currentTimeMillis();
		
		Map<String,Map<String,Map<String,Map<String,String>>>> hourlyStatisticsdata=HourlyQuery.getHourlyData();
		
		
		long end=System.currentTimeMillis();
		
		StatisticsLog.log("Total Time Query : "+((end-start)/1000)+" seconds");

		Set<String> dateset=HourlyQuery.getAvailableDays(hourlyStatisticsdata);
		
		StatisticsLog.log("dateset : "+dateset);

		
		Map<String,Map<String,String>> cli_id_infomap=MasterData.getCli_idInfoMap(); 
		
		StatisticsLog.log("cli_id_infomap : size "+cli_id_infomap.size());

		
		Map<String,String> carrier_infomap=MasterData.getCarrierInfoMap();
		
		StatisticsLog.log("carrier_infomap : size "+carrier_infomap.size());
		
		Map<String,Map<String,Map<String,String>>> daywiseStatisticsdata = getDaywiseStatisticsdata(hourlyStatisticsdata,carrier_infomap);

		StatisticsLog.log("daywiseStatisticsdata : size "+daywiseStatisticsdata.size());

		start=System.currentTimeMillis();
		
		doHourWiseInsert(dateset,cli_id_infomap,carrier_infomap,hourlyStatisticsdata);
		
		doDateWiseInsert(dateset,daywiseStatisticsdata);
		
		end=System.currentTimeMillis();
		
		StatisticsLog.log("Total Time Insert : "+((end-start)/1000)+" seconds");

		
	}

	private static void doDateWiseInsert(Set<String> dateset,
			Map<String, Map<String, Map<String, String>>> daywiseStatisticsdata) {
		
		

		
		
       	Connection con =null;
		
		try {
			con=DBConnection.getConnectionPostgresStatistics();
			con.setAutoCommit(false);
			delete(con,dateset);
			insert(con,daywiseStatisticsdata);
			con.commit();
			
		}catch(Exception e) {
			StatisticsLog.log("error : "+ErrorMessage.getStackTraceAsString(e));
		}finally {
            CommonUtility.closeConnection(con);
     
        }
		
	
		
	}

	private static void insert(Connection con, Map<String, Map<String, Map<String, String>>> daywiseStatisticsdata) {
		
		

    	PreparedStatement pstmt = null;
    	ResultSet rs=null;

    	String sql="insert into summary.daily_traffic_report(id,recv_date,cli_id,username,"
    			+ "pu_id,pu_username,su_id,su_username,company,"
    			+ "carrier_name,total_received,total_submitted,non_promo_sub_count"
    			+ ",delivery_count,failed_count,platform_reject,nulldn,"
    			+ "delivery_pct) values(?,?,?,?,?,?,?,?,?,?,"
    			+ "?,?,?,?"
    			+ ",?,?,?,?)";
    	try {
    		
    		pstmt=con.prepareStatement(sql);
    		add(daywiseStatisticsdata,pstmt);
    		int [] result=pstmt.executeBatch();
    		int count =getCount(result);
    		StatisticsLog.log("Daywise inserted : "+count);

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

	private static void add(Map<String, Map<String, Map<String, String>>> daywiseStatisticsdata,
			PreparedStatement pstmt) {
		
		


    	SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd");
    

    	daywiseStatisticsdata.forEach((key,datedata)->{
			
			StringTokenizer st=new StringTokenizer(key,"~");
			String cli_id=st.nextToken();
			
				datedata.forEach((datestring,data)->{
					
					try {
						Date receiveDate=new Date(sdf.parse(datestring).getTime());
						
							
							try {
								pstmt.setString(1, UUID.randomUUID().toString());
								pstmt.setDate(2, receiveDate);
								pstmt.setLong(3, Long.parseLong(cli_id));								
								pstmt.setString(4, data.get("user"));
								pstmt.setLong(5, Long.parseLong(data.get("pu_id")));
								pstmt.setString(6, data.get("pu_user"));
								pstmt.setLong(7, Long.parseLong(data.get("su_id")));
								pstmt.setString(8, data.get("su_user"));
								pstmt.setString(9, data.get("company"));
								pstmt.setString(10, data.get("carriername"));

								
								pstmt.setLong(11, Long.parseLong(data.get("received")));								
								pstmt.setLong(12, Long.parseLong(data.get("submit")));								
								pstmt.setLong(13, Long.parseLong(data.get("nonpromosubmit")));								
								pstmt.setLong(14, Long.parseLong(data.get("delivery")));								
								pstmt.setLong(15, Long.parseLong(data.get("failed")));								
								pstmt.setLong(16, Long.parseLong(data.get("platformreject")));
								pstmt.setLong(17, Long.parseLong(data.get("nulldn")));
								double value =  Double.parseDouble(data.get("dnpercentage"));
								if(value==0) {
									
									pstmt.setDouble(18,0D)	;

								}else {
									
									BigDecimal rounded = new BigDecimal(value).setScale(2, RoundingMode.HALF_UP);
									double result = rounded.doubleValue();
									pstmt.setDouble(18,result)	;
								}
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

	private static Map<String, Map<String, Map<String, String>>> getDaywiseStatisticsdata(
			Map<String, Map<String, Map<String, Map<String, String>>>> hourlyStatisticsdata,Map<String,String> carrier_infomap) {
		Map<String, Map<String, Map<String, String>>> result=new HashMap<String, Map<String, Map<String, String>>>();
		
		hourlyStatisticsdata.forEach((key,receivedatedata)->{
			
			StringTokenizer st=new StringTokenizer(key,"~");
			String cli_id=st.nextToken();
			String smscid=st.nextToken();
			
			if(smscid.equals("dontdid")) {
				smscid="";
						
			}
			
			final String carriername=carrier_infomap.get(smscid)==null?"":carrier_infomap.get(smscid);
			
			final Map<String, Map<String, String>> resultreceivedatedata=result.get(key)==null?new HashMap<String, Map<String, String>>():result.get(key);
			
			result.put(key, resultreceivedatedata);
		
			
			receivedatedata.forEach((receivedate,hourlydata)->{
				
				final Map<String, String> data=resultreceivedatedata.get(receivedate)==null?new HashMap<String,String>():resultreceivedatedata.get(receivedate);
			
				resultreceivedatedata.put(receivedate, data);
				
				hourlydata.forEach((hour,hourdata)->{
					
					add(carriername,hourdata,data);
				});
			
			});

		});
		return null;
	}

	private static void add(String carriername, Map<String, String> hourdata, Map<String, String> data) {
		
		
		
		
		data.put("carriername", carriername);
		data.put("user", hourdata.get("user"));
		data.put("pu_id", hourdata.get("pu_id"));
		data.put("pu_user", hourdata.get("pu_user"));
		data.put("su_id",  hourdata.get("su_id"));
		data.put("company", hourdata.get("company"));

		long lReceived=data.get("received")==null?0L:Long.parseLong(data.get("received"));
		long lSubmit=data.get("submit")==null?0L:Long.parseLong(data.get("submit"));
		long lNonpromosubmit=data.get("nonpromosubmit")==null?0L:Long.parseLong(data.get("nonpromosubmit"));
		long lDelivery=data.get("delivery")==null?0L:Long.parseLong(data.get("delivery"));
		long lFailed=data.get("failed")==null?0L:Long.parseLong(data.get("failed"));
		long lPlatformreject=data.get("platformreject")==null?0L:Long.parseLong(data.get("platformreject"));
		long lNulldn=data.get("nulldn")==null?0L:Long.parseLong(data.get("nulldn"));

		data.put("received", ""+(lReceived+Long.parseLong(hourdata.get("received"))));
		data.put("submit", ""+(lSubmit+Long.parseLong(hourdata.get("submit"))));
		data.put("nonpromosubmit", ""+(lNonpromosubmit+Long.parseLong(hourdata.get("nonpromosubmit"))));
		data.put("delivery", ""+(lDelivery+Long.parseLong(hourdata.get("delivery"))));
		data.put("failed", ""+(lFailed+Long.parseLong(hourdata.get("failed"))));
		data.put("platformreject", ""+(lPlatformreject+Long.parseLong(hourdata.get("platformreject"))));
		data.put("nulldn", ""+(lNulldn+Long.parseLong(hourdata.get("nulldn"))));

		HourlyQuery.resetdnpercentage(data);
	}

	private static void doHourWiseInsert(Set<String> dateset, Map<String, Map<String, String>> cli_id_infomap,
			Map<String, String> carrier_infomap,
			Map<String, Map<String, Map<String, Map<String, String>>>> hourlyStatisticsdata) {
		
		
       	Connection con =null;
		
		try {
			con=DBConnection.getConnectionPostgresStatistics();
			con.setAutoCommit(false);
			delete(con,dateset);
			insert(con,cli_id_infomap,carrier_infomap,hourlyStatisticsdata);
			con.commit();
			
		}catch(Exception e) {
			StatisticsLog.log("error : "+ErrorMessage.getStackTraceAsString(e));
		}finally {
            CommonUtility.closeConnection(con);
     
        }
		
	}

	private static void insert(Connection con, Map<String, Map<String, String>> cli_id_infomap,
			Map<String, String> carrier_infomap,
			Map<String, Map<String, Map<String, Map<String, String>>>> hourlyStatisticsdata) {
    	PreparedStatement pstmt = null;
    	ResultSet rs=null;

    	String sql="insert into summary.hourly_traffic_report(id,recv_date,recv_hour,cli_id,username,"
    			+ "pu_id,pu_username,su_id,su_username,company,"
    			+ "carrier_name,total_received,total_submitted,non_promo_sub_count"
    			+ ",delivery_count,failed_count,platform_reject,nulldn,"
    			+ "delivery_pct) values(?,?,?,?,?,?,?,?,?,?,"
    			+ "?,?,?,?"
    			+ ",?,?,?,?,?)";
    	try {
    		
    		pstmt=con.prepareStatement(sql);
    		add(cli_id_infomap,carrier_infomap,hourlyStatisticsdata,pstmt);
    		int [] result=pstmt.executeBatch();
    		int count =getCount(result);
    		StatisticsLog.log("inserted : "+count);

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

	private static void add(Map<String, Map<String, String>> cli_id_infomap, Map<String, String> carrier_infomap,
			Map<String, Map<String, Map<String, Map<String, String>>>> hourlyStatisticsdata, final PreparedStatement pstmt) {

    	SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd");
    

		hourlyStatisticsdata.forEach((key,datedata)->{
			
			StringTokenizer st=new StringTokenizer(key,"~");
			String cli_id=st.nextToken();
			String smscid=st.nextToken();
			
			if(smscid.equals("dontdid")) {
				smscid="";
						
			}
			
			final String carriername=carrier_infomap.get(smscid)==null?"":carrier_infomap.get(smscid);
				datedata.forEach((datestring,hourdata)->{
					
					try {
						Date receiveDate=new Date(sdf.parse(datestring).getTime());
						
						hourdata.forEach((hourstring,data)->{
							int hour=Integer.parseInt(hourstring);
							
							try {
								pstmt.setString(1, UUID.randomUUID().toString());
								pstmt.setDate(2, receiveDate);
								pstmt.setInt(3, hour);
								pstmt.setLong(4, Long.parseLong(cli_id));								
								pstmt.setString(5, cli_id_infomap.get(cli_id).get("user"));
								pstmt.setLong(6, Long.parseLong(cli_id_infomap.get(cli_id).get("pu_id")));
								pstmt.setString(7, cli_id_infomap.get(cli_id).get("pu_user"));
								pstmt.setLong(8, Long.parseLong(cli_id_infomap.get(cli_id).get("su_id")));
								pstmt.setString(9, cli_id_infomap.get(cli_id).get("su_user"));
								pstmt.setString(10, cli_id_infomap.get(cli_id).get("company"));
								pstmt.setString(11, carriername);

								
								pstmt.setLong(12, Long.parseLong(data.get("received")));								
								pstmt.setLong(13, Long.parseLong(data.get("submit")));								
								pstmt.setLong(14, Long.parseLong(data.get("nonpromosubmit")));								
								pstmt.setLong(15, Long.parseLong(data.get("delivery")));								
								pstmt.setLong(16, Long.parseLong(data.get("failed")));								
								pstmt.setLong(17, Long.parseLong(data.get("platformreject")));
								pstmt.setLong(18, Long.parseLong(data.get("nulldn")));
								double value =  Double.parseDouble(data.get("dnpercentage"));
								if(value==0) {
									
									pstmt.setDouble(19,0D)	;

								}else {
									
									BigDecimal rounded = new BigDecimal(value).setScale(2, RoundingMode.HALF_UP);
									double result = rounded.doubleValue();
									pstmt.setDouble(19,result)	;
								}
								pstmt.addBatch();

							} catch (Exception e) {
								StatisticsLog.log("error : "+ErrorMessage.getStackTraceAsString(e));

							}
						});
					} catch (Exception e) {
						StatisticsLog.log("error : "+ErrorMessage.getStackTraceAsString(e));

					}
				});
				
			
		});
	}

	private static void delete(Connection con, Set<String> dateset) {
    	PreparedStatement pstmt = null;

    	String sql="delete from summary.hourly_traffic_report where recv_date=?";
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
