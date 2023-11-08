package com.itextos.beacon.inmemory.spamcheck.util;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.itextos.beacon.commonlib.dbpool.DBDataSourceFactory;
import com.itextos.beacon.commonlib.dbpool.DatabaseSchema;
import com.itextos.beacon.commonlib.dbpool.JndiInfoHolder;
import com.itextos.beacon.commonlib.utility.CommonUtility;

public class SpamLoggerReaper
        extends
        Thread
{

    private static final Log log = LogFactory.getLog(SpamLoggerReaper.class);

    @Override
    public void run()
    {
        if (log.isInfoEnabled())
            log.info("In memory Reaper starting....");

        while (true)
        {
            final List<SpamCheckObject> spamList = SpamLogger.getInstance().getSpamCheckObjects();

            if (!spamList.isEmpty())
                spamWordLogging(spamList);
            else
                CommonUtility.sleepForAWhile(10 * 1000);
        }
    }

    private static void spamWordLogging(
            List<SpamCheckObject> aSpamWordList)
    {
        if (log.isDebugEnabled())
            log.debug("spamWordLogging() started");

        final String sql = "insert into potential_spam values (now(),?,?,?,?,now())";

        try (
                final Connection connection = DBDataSourceFactory.getConnection(JndiInfoHolder.getInstance().getJndiInfoUsingName(DatabaseSchema.LOGGING.getKey()));
                final PreparedStatement statement = connection.prepareStatement(sql);)
        {

            for (final SpamCheckObject spamCheckObject : aSpamWordList)
            {
                statement.setString(1, spamCheckObject.getClientId());
                statement.setString(2, spamCheckObject.getMid());
                statement.setString(3, spamCheckObject.getMessage());
                statement.setInt(4, spamCheckObject.getSpamAction().getSpamWordCount());
                statement.addBatch();
            }

            final int[] executeBatch = statement.executeBatch();

            if (log.isDebugEnabled())
                log.debug("spamWordLogging() result:" + executeBatch.length);
        }
        catch (final Exception e)
        {
            log.error("spamWordLogging(); Not able to insert into potential_spam table... ", e);
            if (log.isInfoEnabled())
                log.info("Missed spam details" + aSpamWordList);
        }
    }

}