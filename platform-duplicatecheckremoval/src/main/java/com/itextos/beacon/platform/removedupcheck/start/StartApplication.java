package com.itextos.beacon.platform.removedupcheck.start;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.itextos.beacon.commonlib.constants.ClusterType;
import com.itextos.beacon.commonlib.constants.Component;
import com.itextos.beacon.commonlib.redis.RedisConnectionProvider;
import com.itextos.beacon.platform.removedupcheck.ExpiryRemovalTask;

public class StartApplication
{

    private static final Log log = LogFactory.getLog(StartApplication.class);

    public static void main(
            String[] args)
    {
        if (log.isDebugEnabled())
            log.debug("Starting the DuplicateCheck Expiry Processor... ");

        try
        {
            final int lRedispoolcnt = RedisConnectionProvider.getInstance().getRedisPoolCount(ClusterType.COMMON, Component.DUPLICATE_CHK);

            if (log.isDebugEnabled())
                log.debug("Total number of redis configured : " + lRedispoolcnt);

            for (int lRedisIndex = 1; lRedisIndex <= lRedispoolcnt; lRedisIndex++)
            {
                if (log.isDebugEnabled())
                    log.debug("Redis index pool : " + lRedisIndex);

                new ExpiryRemovalTask(lRedisIndex);
            }
        }
        catch (final Exception e)
        {
            log.error("Exception occer while starting DuplicateCheck Expiry Processor ..", e);
            System.exit(-1);
        }
    }

}
