package com.winnovature.dltfileprocessor.daos;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.winnovature.dltfileprocessor.utils.Constants;
import com.winnovature.utils.singletons.ConnectionFactoryForAccountsDB;
import com.winnovature.utils.singletons.ConnectionFactoryForCMDB;

public class GenericDAO {
	static Log log = LogFactory.getLog(Constants.FileUploadLogger);
	private static final String className = "[GenericDAO]";
	
	
	public Map<String, String> getDltTemplate(String telco) throws Exception {
		String methodName = " [getDltTemplate] ";
		if (log.isDebugEnabled()) {
			log.debug(className + methodName + "begin ...");
		}
		Map<String, String> columnMapping = new HashMap<String, String>();
		ResultSet rs = null;
		Connection con = null;
		PreparedStatement pstmt = null;
		String sqlQuery = "select * from dlt_template_column_mapping where lower(telco)=?";
		try {
			if (log.isDebugEnabled()) {
				log.debug(className + methodName + "sqlQuery = " + sqlQuery);
			}

			con = ConnectionFactoryForCMDB.getInstance().getConnection();
			pstmt = con.prepareStatement(sqlQuery);
			pstmt.setString(1, telco.trim().toLowerCase());

			rs = pstmt.executeQuery();
			if(rs.next()) {
				columnMapping.put("header", rs.getString("header"));
				columnMapping.put("template_id", rs.getString("template_id"));
				columnMapping.put("template_name", rs.getString("template_name"));
				columnMapping.put("template_content", rs.getString("template_content"));
				columnMapping.put("template_type", rs.getString("template_type"));
				columnMapping.put("telemarketer", rs.getString("telemarketer"));
				columnMapping.put("category", rs.getString("category"));
				columnMapping.put("registered_dlt", rs.getString("registered_dlt"));
				columnMapping.put("registered_on", rs.getString("registered_on"));
				columnMapping.put("status_date", rs.getString("status_date"));
				columnMapping.put("approval_status", rs.getString("approval_status"));
				columnMapping.put("status", rs.getString("status"));
				columnMapping.put("consent_type", rs.getString("consent_type"));
				columnMapping.put("datetime_format", rs.getString("datetime_format"));
				columnMapping.put("approval_text", rs.getString("approval_text"));
			}
		} catch (Exception e) {
			log.error(className + methodName, e);
			throw e;
		} finally {
			closeConnection(rs, pstmt, con);
		}
		if (log.isDebugEnabled()) {
			log.debug(className + methodName + " Telco ["+telco+"] Details = " + columnMapping);
		}
		return columnMapping;
	}
	

	
	public List<String> getUsersWithDltTemplateGroupId(String dlt_templ_grp_id) throws Exception {
		String methodName = " [getUsersWithDltTemplateGroupId] ";
		ResultSet rs = null;
		Connection con = null;
		PreparedStatement pstmt = null;
		String sqlQuery = "select cli_id from accounts.user_config where dlt_templ_grp_id=?";
		List<String> clientIds = new ArrayList<String>();
		try {
			if (log.isDebugEnabled()) {
				log.debug(className + methodName + "sqlQuery = " + sqlQuery);
			}

			con = ConnectionFactoryForAccountsDB.getInstance().getConnection();
			pstmt = con.prepareStatement(sqlQuery);
			pstmt.setString(1, dlt_templ_grp_id);

			rs = pstmt.executeQuery();
			while(rs.next()) {
				clientIds.add(rs.getString("cli_id"));
			}
		} catch (Exception e) {
			log.error(className + methodName, e);
			throw e;
		} finally {
			closeConnection(rs, pstmt, con);
		}
		if (log.isDebugEnabled()) {
			log.debug(className + methodName + " ClientIds with ["+dlt_templ_grp_id+"] are " + clientIds);
		}
		return clientIds;
	}
	

	private void closeConnection(ResultSet rs, PreparedStatement ps, Connection con) throws Exception {
		try {
			if (rs != null) {
				rs.close();
			}
			if (ps != null) {
				ps.close();
			}
			if (con != null) {
				con.close();
			}
		} catch (Exception e) {
			log.error(className + " [closeConnection] ", e);
		}
	}
	
}
