package com.itextos.beacon.platform.statistics;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.itextos.beacon.commonlib.commondbpool.DBDataSourceFactory;
import com.itextos.beacon.commonlib.commondbpool.DatabaseSchema;
import com.itextos.beacon.commonlib.commondbpool.JndiIdProperties;
import com.itextos.beacon.commonlib.commondbpool.JndiInfoHolder;
import com.itextos.beacon.commonlib.constants.RedisKeys;
import com.itextos.beacon.commonlib.constants.exception.ItextosException;
import com.itextos.beacon.commonlib.utility.CommonUtility;

public class MasterData {

	static String SQL_CLI_ID="select a.cli_id,a.user,a.pu_id,b.user pu_user,a.su_id,c.user su_user ,a.company from accounts_view a,accounts_view b ,accounts_view c where a.pu_id=b.cli_id and a.su_id=c.cli_id";
	
	static String SQL_CARRIER_NAME="select carrier_name,smscid from carrier_handover.carrier_master a,carrier_handover.carrier_route_map b,carrier_handover.route_configuration c where a.carrier_id=b.carrier_id and b.route_id=c.route_id";

	public static Map<String,Map<String,String>> getCli_idInfoMap(){
		
		Map<String,Map<String,String>> result=new HashMap<String,Map<String,String>>();
	
	      
	           	Connection con =null;
	        	PreparedStatement pstmt = null;
	        	ResultSet rs=null;
	            try 
	            {
	                 con = DBDataSourceFactory.getConnectionFromThin(JndiInfoHolder.getInstance().getJndiInfo(JndiIdProperties.getInstance().getJndiProperty(DatabaseSchema.ACCOUNTS.getKey())));
	                 pstmt = con.prepareStatement(SQL_CLI_ID);
	                 rs=pstmt.executeQuery();
	               while(rs.next()) {
	            	   
	            	   String cli_id=rs.getString("cli_id");
	            	   String user=rs.getString("user");
	            	   String pu_id=rs.getString("pu_id");
	            	   String pu_user=rs.getString("pu_user");
	            	   String su_id=rs.getString("su_id");
	            	   String su_user=rs.getString("su_user");
	            	   String company=rs.getString("company");

	            	   Map<String,String> data=new HashMap<String,String>();
	            	   data.put("cli_id", cli_id);
	            	   data.put("user", user);
	            	   data.put("pu_id", pu_id);
	            	   data.put("pu_user", pu_user);
	            	   data.put("su_id", su_id);
	            	   data.put("su_user", su_user);
	            	   data.put("company", company);
	            	   
	            	   result.put(cli_id, data);

	               }
	            }
	            catch (final Exception e)
	            {
	               
	                e.printStackTrace();
	            }finally {
	                CommonUtility.closeResultSet(rs);	
	                CommonUtility.closeStatement(pstmt);
	                CommonUtility.closeConnection(con);
	         
	            }
	            
	            return result;
	        }
	
	
public static Map<String,String> getCarrierInfoMap(){
		
	Map<String,String> result=new HashMap<String,String>();
	
	      
	           	Connection con =null;
	        	PreparedStatement pstmt = null;
	        	ResultSet rs=null;
	            try 
	            {
	                 con = DBDataSourceFactory.getConnectionFromThin(JndiInfoHolder.getInstance().getJndiInfo(JndiIdProperties.getInstance().getJndiProperty(DatabaseSchema.ACCOUNTS.getKey())));
	                 pstmt = con.prepareStatement(SQL_CARRIER_NAME);
	                 rs=pstmt.executeQuery();
	               while(rs.next()) {
	            	   
	            	   String carrier_name=rs.getString("carrier_name");
	            	   String smscid=rs.getString("smscid");
	            	
	            	   result.put(smscid, carrier_name);
	            	  
	            	   

	               }
	            }
	            catch (final Exception e)
	            {
	               
	                e.printStackTrace();
	            }finally {
	                CommonUtility.closeResultSet(rs);	
	                CommonUtility.closeStatement(pstmt);
	                CommonUtility.closeConnection(con);
	         
	            }
	            
	            return result;
	        }
	
}
