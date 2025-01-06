package com.itextos.beacon.commonlib.redisstatistics.monitor.stats;

import java.io.StringReader;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;
import java.util.TreeMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.itextos.beacon.commonlib.utility.CommonUtility;
import com.itextos.beacon.commonlib.utility.DateTimeUtility;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.Pipeline;
import redis.clients.jedis.Response;

public class RedisMonitor implements Runnable {

    private static final Log log = LogFactory.getLog(RedisMonitor.class);

    private static final String REDIS_VERSION = "redis_version";
    private static final String OPERATING_SYSTEMS = "os";
    private static final String TCP_PORT = "tcp_port";
    private static final String UPTIME_IN_DAYS = "uptime_in_days";
    private static final String CONNECTED_CLIENTS = "connected_clients";
    private static final String BLOCKED_CLIENTS = "blocked_clients";
    private static final String TRACKING_CLIENTS = "tracking_clients";
    private static final String USED_MEMORY = "used_memory";
    private static final String USED_MEMORY_RSS = "used_memory_rss";
    private static final String TOTAL_SYSTEM_MEMORY = "total_system_memory";
    private static final String USED_MEMORY_DATASET = "used_memory_dataset";
    private static final String USED_MEMORY_LUA = "used_memory_lua";
    private static final String MAXMEMORY = "maxmemory";
    private static final String TOTAL_CONNECTIONS_RECEIVED = "total_connections_received";
    private static final String TOTAL_COMMANDS_PROCESSED = "total_commands_processed";
    private static final String REJECTED_CONNECTIONS = "rejected_connections";

    private final Jedis mJedis;
    private final boolean mStatsCheck;

    private Date mLastUpdatedTime = null;
    private ServerInfo mServerInfo = null;
    private ClientInfo mClientInfo = null;
    private MemoryInfo mMemoryInfo = null;
    private ConnectionInfo mConnectionInfo = null;
    private final Map<String, Long> mListQSize = new TreeMap<>();

    public RedisMonitor(Jedis aJedis, boolean aStatsCheck) {
        this(null, aJedis, aStatsCheck);
    }

    public RedisMonitor(RedisMonitor aFirstObject, Jedis aJedis, boolean aStatsCheck) {
        this.mJedis = aJedis;
        this.mStatsCheck = aStatsCheck;

        if (!aStatsCheck && (aFirstObject != null)) {
            this.mServerInfo = aFirstObject.mServerInfo;
            this.mClientInfo = aFirstObject.mClientInfo;
            this.mMemoryInfo = aFirstObject.mMemoryInfo;
            this.mConnectionInfo = aFirstObject.mConnectionInfo;
        }
    }

    @Override
    public void run() {
      //  String host = null;
     //   int port = -1;

        try {
      //      host = mJedis.getClient().getHost();
     //       port = mJedis.getClient().getPort();

  ///          if (log.isDebugEnabled())
      //          log.debug("Gathering statistics information from '" + host + "' Port '" + port + "' Server Info Req '" + mStatsCheck + "'");

            final long startTime = DateTimeUtility.getCurrentTimeInNanos();

            if (mStatsCheck) {
                final String info = mJedis.info();

                if (log.isDebugEnabled())
                    log.debug("Info from Redis " + info);

                final Properties props = new Properties();
                final StringReader sr = new StringReader(info);
                props.load(sr);

                updateServerInfo(props);
                updateClientInfo(props);
                updateMemoryInfo(props);
                updateConnectionInfo(props);
            }

            // Assuming all the Qs are starting with Q and of Type List.
            final Set<String> keys = mJedis.keys("Q:*");

            try (Pipeline pipe = mJedis.pipelined()) {
                final Map<String, Response<Long>> listSizes = new HashMap<>();

                for (final String key : keys) {
                    final Response<Long> llen = pipe.llen(key);
                    listSizes.put(key, llen);
                }
                pipe.sync();

                for (final Entry<String, Response<Long>> entry : listSizes.entrySet()) {
                    mListQSize.put(entry.getKey(), entry.getValue().get());
                }
            }

            if (log.isDebugEnabled())
                log.debug("Q Size retrieved from the Redis. Total time taken " + DateTimeUtility.getTimeDifferenceInMillisFromNanoSecond(startTime) + " Millis");

            mLastUpdatedTime = new Date();
        } catch (final Exception e) {
          //  log.error("Exception while getting the details from Redis. Host '" + host + "' Port '" + port + "'", e);
            e.printStackTrace();
        } finally {
            // Close the Jedis connection
            mJedis.close();
        }
    }

    private void updateServerInfo(Properties aProps) {
        mServerInfo = new ServerInfo(
                CommonUtility.nullCheck(aProps.get(REDIS_VERSION), true),
                CommonUtility.nullCheck(aProps.get(OPERATING_SYSTEMS), true),
                CommonUtility.nullCheck(aProps.get(TCP_PORT), true),
                CommonUtility.nullCheck(aProps.get(UPTIME_IN_DAYS), true)
        );
    }

    private void updateClientInfo(Properties aProps) {
        mClientInfo = new ClientInfo(
                CommonUtility.nullCheck(aProps.get(CONNECTED_CLIENTS), true),
                CommonUtility.nullCheck(aProps.get(BLOCKED_CLIENTS), true),
                CommonUtility.nullCheck(aProps.get(TRACKING_CLIENTS), true)
        );
    }

    private void updateMemoryInfo(Properties aProps) {
        mMemoryInfo = new MemoryInfo(
                CommonUtility.nullCheck(aProps.get(USED_MEMORY), true),
                CommonUtility.nullCheck(aProps.get(USED_MEMORY_RSS), true),
                CommonUtility.nullCheck(aProps.get(TOTAL_SYSTEM_MEMORY), true),
                CommonUtility.nullCheck(aProps.get(USED_MEMORY_DATASET), true),
                CommonUtility.nullCheck(aProps.get(USED_MEMORY_LUA), true),
                CommonUtility.nullCheck(aProps.get(MAXMEMORY), true)
        );
    }

    private void updateConnectionInfo(Properties aProps) {
        mConnectionInfo = new ConnectionInfo(
                CommonUtility.nullCheck(aProps.get(TOTAL_CONNECTIONS_RECEIVED), true),
                CommonUtility.nullCheck(aProps.get(TOTAL_COMMANDS_PROCESSED), true),
                CommonUtility.nullCheck(aProps.get(REJECTED_CONNECTIONS), true)
        );
    }

    public ServerInfo getServerInfo() {
        return mServerInfo;
    }

    public ClientInfo getClientInfo() {
        return mClientInfo;
    }

    public MemoryInfo getMemoryInfo() {
        return mMemoryInfo;
    }

    public ConnectionInfo getConnectionInfo() {
        return mConnectionInfo;
    }

    public Map<String, Long> getListQSize() {
        return mListQSize;
    }

    public Date getLastUpdatedTime() {
        return mLastUpdatedTime;
    }
}