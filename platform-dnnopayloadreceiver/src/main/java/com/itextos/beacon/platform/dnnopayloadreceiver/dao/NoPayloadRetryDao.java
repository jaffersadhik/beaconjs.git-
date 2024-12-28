package com.itextos.beacon.platform.dnnopayloadreceiver.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.itextos.beacon.commonlib.commondbpool.DBDataSourceFactory;
import com.itextos.beacon.commonlib.commondbpool.DatabaseSchema;
import com.itextos.beacon.commonlib.commondbpool.JndiInfoHolder;
import com.itextos.beacon.commonlib.constants.MiddlewareConstant;
import com.itextos.beacon.commonlib.constants.exception.ItextosException;
import com.itextos.beacon.commonlib.message.DeliveryObject;
import com.itextos.beacon.commonlib.message.IMessage;
import com.itextos.beacon.commonlib.utility.CommonUtility;

public class NoPayloadRetryDao
{

    private static final Log    log        = LogFactory.getLog(NoPayloadRetryDao.class);

    private static final String MOD_VALUE  = System.getProperty("modvalue"); // valid values are 0,1,2,3

    private static final String INSERT_SQL = "insert into no_payload_dn_retry (payload_rid, expiry_count, cluster, dn_payload) values (?,?,?,?)";
    private static final String SELECT_SQL = "select sno, payload_rid, expiry_count, cluster, dn_payload from no_payload_dn_retry where cluster={0} and mod(sno,4) in (" + MOD_VALUE
            + ") ORDER by expiry_count DESC limit 500";
    private static final String UPDATE_SQL = "update no_payload_dn_retry set expiry_count=? where sno=?";
    private static final String DELETE_SQL = "delete from no_payload_dn_retry where sno=?";

    private NoPayloadRetryDao()
    {
        if ((MOD_VALUE == null) || MOD_VALUE.isBlank()) {
        	
            //throw new ItextosRuntimeException("Invalid Modvalue set in Runtime...'" + MOD_VALUE + "'");
        	log.error("Invalid Modvalue set in Runtime...'" + MOD_VALUE + "'");
        }

        {
            if (log.isDebugEnabled())
                log.debug("Modvalues passed '" + MOD_VALUE + "'");

            final String[] mods = MOD_VALUE.split(",");

            for (final String s : mods)
            {
                final int mod = CommonUtility.getInteger(s, -999);
                if ((mod == -999) || (mod >= 4)) {
                  //  throw new ItextosRuntimeException("Invalid Modvalue set in Runtime...'" + MOD_VALUE + "'");
                    log.error("Invalid Modvalue set in Runtime...'" + MOD_VALUE + "'");
                }
            }
        }
    }

    public static void storeNoPayloadRetryData(
            List<IMessage> aRecords)
            throws ItextosException
    {
        Connection        lSqlConn      = null;
        PreparedStatement lPreepareStmt = null;

        try
        {
            lSqlConn      = DBDataSourceFactory.getConnection(JndiInfoHolder.getInstance().getJndiInfoUsingName(DatabaseSchema.MESSAGING.getKey()));
            lPreepareStmt = lSqlConn.prepareStatement(INSERT_SQL);

            lSqlConn.setAutoCommit(false);
            insertRecords(lPreepareStmt, aRecords);
            lSqlConn.commit();
        }
        catch (final Exception e)
        {
            CommonUtility.rollbackConnection(lSqlConn);

            final String s = "Excception while inserting the data into 'no_payload_dn_retry'";
            log.error(s, e);
            throw new ItextosException(s, e);
        }
        finally
        {
            CommonUtility.closeStatement(lPreepareStmt);
            CommonUtility.closeConnection(lSqlConn);
        }
    }

    private static void insertRecords(
            PreparedStatement aPstmt,
            List<IMessage> aIMessageList)
            throws SQLException
    {

        for (final IMessage lMessage : aIMessageList)
        {
            final DeliveryObject message = (DeliveryObject) lMessage;
            aPstmt.setString(1, CommonUtility.nullCheck(message.getPayloadRedisId(), true));
            aPstmt.setInt(2, CommonUtility.getInteger(CommonUtility.nullCheck(message.getValue(MiddlewareConstant.MW_NO_PAYLOD_RETRY_EXPIRY_COUNT), true)));
            aPstmt.setString(3, CommonUtility.nullCheck(message.getClusterType().getKey(), true));
            aPstmt.setString(4, message.getJsonString());
            aPstmt.addBatch();
        }

        aPstmt.executeBatch();
    }



}
