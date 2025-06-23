package com.itextos.beacon.platform.statistics;

import java.sql.BatchUpdateException;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.UUID;

import com.itextos.beacon.commonlib.commondbpool.DBDataSourceFactory;
import com.itextos.beacon.commonlib.constants.ErrorMessage;
import com.itextos.beacon.commonlib.constants.Table2DBInserterId;
import com.itextos.beacon.commonlib.utility.CommonUtility;
import com.itextos.beacon.inmemory.loader.InmemoryLoaderCollection;
import com.itextos.beacon.inmemory.loader.process.InmemoryId;
import com.itextos.beacon.mysqlimport.DBConnection;
import com.itextos.beacon.platform.topic2table.dbinfo.TableInserterInfo;
import com.itextos.beacon.platform.topic2table.dbinfo.TableInserterInfoCollection;

public class LatencySubmission {

	static String NULL="##NULL##";

	private static String SQL="select date(recv_date) recv_date,cli_id,cli_hdr,country,count(*) cnt,"
			+ "SUM(case when sub_lat_sla_in_millis <= 5000 then 1 else 0 end) as LTE_5_SECOND,"
			+ "SUM(case when sub_lat_sla_in_millis >5000 and sub_lat_sla_in_millis <= 10000 then 1 else 0 end) as LTE_10_SECOND,"
			+ "SUM(case when sub_lat_sla_in_millis >10000 and sub_lat_sla_in_millis <= 15000 then 1 else 0 end) as LTE_15_SECOND,"
			+ "SUM(case when sub_lat_sla_in_millis >15000 and sub_lat_sla_in_millis <= 30000 then 1 else 0 end) as LTE_30_SECOND,"
			+ "SUM(case when sub_lat_sla_in_millis >30000 and sub_lat_sla_in_millis <= 45000 then 1 else 0 end) as LTE_45_SECOND,"
			+ "SUM(case when sub_lat_sla_in_millis >45000 and sub_lat_sla_in_millis <= 60000 then 1 else 0 end) as LTE_60_SECOND,"
			+ "SUM(case when sub_lat_sla_in_millis >60000 and sub_lat_sla_in_millis <= 120000 then 1 else 0 end) as LTE_120_SECOND,"
			+ "SUM(case when sub_lat_sla_in_millis > 120000 then 1 else 0 end) as GT_2_MINUTE "
			+ "from billing_{0}.submission_{1} group by recv_date,cli_id,cli_hdr,country";


private static String getYesterdayQuery(int days) {
		
		
		String formattedSQL2 = MessageFormat.format(SQL, getYesderdayMonthString(days), getYesderdayString(days));

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
				 String country=rs.getString("country");
				 String cnt=rs.getString("cnt");

				 String LTE_5_SECOND=rs.getString("LTE_5_SECOND");
				 String LTE_10_SECOND=rs.getString("LTE_10_SECOND");
				 String LTE_15_SECOND=rs.getString("LTE_15_SECOND");
				 String LTE_30_SECOND=rs.getString("LTE_30_SECOND");
				 String LTE_45_SECOND=rs.getString("LTE_45_SECOND");
				 

				 String LTE_60_SECOND=rs.getString("LTE_60_SECOND");
				 String LTE_120_SECOND=rs.getString("LTE_120_SECOND");
				 String GT_2_MINUTE=rs.getString("GT_2_MINUTE");

				 Map<String,String> data=new HashMap<String,String>();
				 
				 data.put("cli_id", cli_id);
				 data.put("cli_hdr", cli_hdr);
				 data.put("recv_date", recv_date);
				 data.put("country", country);
				 data.put("cnt", cnt);

				 data.put("LTE_5_SECOND", LTE_5_SECOND);
				 data.put("LTE_10_SECOND", LTE_10_SECOND);
				 data.put("LTE_15_SECOND", LTE_15_SECOND);
				 data.put("LTE_30_SECOND", LTE_30_SECOND);
				 data.put("LTE_45_SECOND", LTE_45_SECOND);

				 data.put("LTE_60_SECOND", LTE_60_SECOND);
				 data.put("LTE_120_SECOND", LTE_120_SECOND);
				 data.put("GT_2_MINUTE", GT_2_MINUTE);
				 

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
	
	private static void setHourlyData(Map<String, Map<String, Map<String,  String>>> result,Map<String,String> carrier_infomap,  String sql) {

		List<Map<String,String>> datalist=getResult( sql);
		
		StatisticsLog.log("datalist : "+datalist.size());
		
		for(int i=0;i<datalist.size();i++) {
			
			Map<String,String> data=datalist.get(i);
			
			 String cli_id=data.get("cli_id");
			 
			 
			 String cli_hdr=data.get("cli_hdr")==null?NULL:data.get("cli_hdr");

			 String country=data.get("country")==null?NULL:data.get("country");


			 
			 Map<String, Map<String,  String>> datewisereport= result.get(cli_id+"~"+cli_hdr+"~"+country);
			 
			 if(datewisereport==null) {
				 
				 datewisereport=new HashMap<String, Map<String, String>>();
				 
				 result.put(cli_id+"~"+cli_hdr+"~"+country, datewisereport);
			 }

			 
			 String recv_date=data.get("recv_date");

			  Map<String, String> report= datewisereport.get(recv_date);
			 
			 if(report==null) {
				 
				 report = new HashMap<String,  String>();
				 
				 datewisereport.put(recv_date, report);
			 }
			 

			 report.put("cnt", data.get("cnt"));
			

			 report.put("LTE_5_SECOND", data.get("LTE_5_SECOND"));

			 report.put("LTE_10_SECOND", data.get("LTE_10_SECOND"));
			 report.put("LTE_15_SECOND", data.get("LTE_15_SECOND"));
			 report.put("LTE_30_SECOND", data.get("LTE_30_SECOND"));
			 report.put("LTE_45_SECOND", data.get("LTE_45_SECOND"));
			 report.put("LTE_60_SECOND", data.get("LTE_60_SECOND"));
			 report.put("LTE_120_SECOND", data.get("LTE_120_SECOND"));


			 report.put("GT_2_MINUTE", data.get("GT_2_MINUTE"));

			
			
	 
		}
	}

	
	
public static void doProcess(Map<String,Map<String,String>> cli_id_infomap,Map<String,String> carrier_infomap) {
		
		long start=System.currentTimeMillis();
		
		Map<String,Map<String,Map<String,String>>> datewiseStatisticsdata=getHourlyData(carrier_infomap);
		
		
		long end=System.currentTimeMillis();
		
		StatisticsLog.log("Total Time Query : "+((end-start)/1000)+" seconds");

		Set<String> dateset=getAvailableDays(datewiseStatisticsdata);
		
		StatisticsLog.log("dateset : "+dateset);

				


		start=System.currentTimeMillis();
		
		

		StatisticsLog.log("datewiseStatisticsdata : size "+datewiseStatisticsdata.size());

		doDateWiseInsertPrimary(cli_id_infomap,carrier_infomap,dateset,datewiseStatisticsdata);
		
		doDateWiseInsertSecondary(cli_id_infomap,carrier_infomap,dateset,datewiseStatisticsdata);

		
		end=System.currentTimeMillis();
		
		StatisticsLog.log("Total Time Insert : "+((end-start)/1000)+" seconds");

		
	}


private static void doDateWiseInsertPrimary(Map<String, Map<String, String>> cli_id_infomap,Map<String,String> carrier_infomap, Set<String> dateset,
		Map<String, Map<String, Map<String, String>>> daywiseStatisticsdata) {
	
	

	
	
   	Connection con =null;
	
	try {
		con=DBConnection.getConnectionPostgresStatisticsPrimary();
		con.setAutoCommit(false);
		delete(con,dateset,"summary.ui_platform_latency_report");
		insert(con,cli_id_infomap,carrier_infomap,daywiseStatisticsdata);
		con.commit();
		
	}catch(Exception e) {
		StatisticsLog.log("error : "+ErrorMessage.getStackTraceAsString(e));
	}finally {
        CommonUtility.closeConnection(con);
 
    }
	

	
}


private static void doDateWiseInsertSecondary(Map<String, Map<String, String>> cli_id_infomap,Map<String,String> carrier_infomap, Set<String> dateset,
		Map<String, Map<String, Map<String, String>>> daywiseStatisticsdata) {
	
	

	
	
   	Connection con =null;
	
	try {
		con=DBConnection.getConnectionPostgresStatisticsSecondary();
		con.setAutoCommit(false);
		delete(con,dateset,"summary.ui_platform_latency_report");
		insert(con,cli_id_infomap,carrier_infomap,daywiseStatisticsdata);
		con.commit();
		
	}catch(Exception e) {
		StatisticsLog.log("error : "+ErrorMessage.getStackTraceAsString(e));
	}finally {
        CommonUtility.closeConnection(con);
 
    }
	

	
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


private static void insert(Connection con, Map<String, Map<String, String>> cli_id_infomap,Map<String,String> carrier_infomap, Map<String, Map<String, Map<String, String>>> daywiseStatisticsdata) {
	
	

	PreparedStatement pstmt = null;
	ResultSet rs=null;

	String sql="insert into summary.ui_platform_latency_report(id,recv_date,cli_id,cli_hdr,"
			+ "country,tot_cnt,lat_0_5_sec_cnt,lat_6_10_sec_cnt,"
			+ "lat_11_15_sec_cnt,lat_16_30_sec_cnt,lat_31_45_sec_cnt,lat_46_60_sec_cnt,"
			+ "lat_61_120_sec_cnt,lat_gt_120_sec_cnt) values(?,?,?,?,?,?,?,?,?,?,"
			+ "?,?,?,?)";
	try {
		
		pstmt=con.prepareStatement(sql);
		add(cli_id_infomap,carrier_infomap,daywiseStatisticsdata,pstmt);
		int [] result=pstmt.executeBatch();
		int count =getCount(result);
		StatisticsLog.log("ui_platform_latency_report inserted : "+count);

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


private static int getCount(int[] result) {
	int count=0;
	for(int i=0;i<result.length;i++){
		
		count+=result[i];
	}
	return count;
}



private static void add(Map<String, Map<String, String>> cli_id_infomap,Map<String,String> carrier_infomap,  Map<String, Map<String, Map<String, String>>> daywiseStatisticsdata,
		PreparedStatement pstmt) {
	
	


	SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd");


	daywiseStatisticsdata.forEach((key,datedata)->{
		

		StringTokenizer st=new StringTokenizer(key,"~");
		String cli_id=st.nextToken();
		String cli_hdr=st.nextToken();
		String country=st.nextToken();

		
		
			datedata.forEach((datestring,data)->{
				
				try {
					Date receiveDate=new Date(sdf.parse(datestring).getTime());
							try {
							pstmt.setString(1, UUID.randomUUID().toString());
							pstmt.setDate(2, receiveDate);

							pstmt.setLong(3, Long.parseLong(cli_id));								
							if(NULL.equals(cli_hdr)) {
								pstmt.setString(4,null);

							}else {
								pstmt.setString(4,cli_hdr);

							}
							pstmt.setString(5, country);
							
							pstmt.setLong(6, Long.parseLong(data.get("cnt")));										
							pstmt.setLong(7, Long.parseLong(data.get("LTE_5_SECOND")));								
							pstmt.setLong(8, Long.parseLong(data.get("LTE_10_SECOND")));
							pstmt.setLong(9, Long.parseLong(data.get("LTE_15_SECOND")));
							pstmt.setLong(10, Long.parseLong(data.get("LTE_30_SECOND")));
							pstmt.setLong(11, Long.parseLong(data.get("LTE_45_SECOND")));								
							pstmt.setLong(12, Long.parseLong(data.get("LTE_60_SECOND")));								
							pstmt.setLong(13, Long.parseLong(data.get("LTE_120_SECOND")));	
							pstmt.setLong(14, Long.parseLong(data.get("GT_2_MINUTE")));

						
							pstmt.addBatch();

						} catch (Exception e) {
							StatisticsLog.log("error : "+data.toString()+"\t"+ErrorMessage.getStackTraceAsString(e));

						}
				
				} catch (Exception e) {
					StatisticsLog.log("error : "+ErrorMessage.getStackTraceAsString(e));

				}
			});
			
		
	});

	
}

private static void nextExceptionLog(BatchUpdateException bue) {
	
    SQLException nextEx = bue.getNextException();
    if (nextEx != null) {
		StatisticsLog.log("error : "+ErrorMessage.getStackTraceAsString(nextEx));

    }

}


}
