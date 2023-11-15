package com.itextos.beacon.interfaces.clouddataprocessor;

import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.itextos.beacon.commonlib.constants.Component;
import com.itextos.beacon.commonlib.redis.RedisConnectionProvider;
import com.itextos.beacon.inmemory.loader.InmemoryLoaderCollection;
import com.itextos.beacon.inmemory.loader.process.InmemoryId;
import com.itextos.beacon.interfaces.clouddataprocessor.process.RequestProcess;
import com.itextos.beacon.interfaces.clouddatautil.common.CloudDataConfig;
import com.itextos.beacon.interfaces.clouddatautil.common.CloudDataConfigInfo;

public class StartApplication
{

    private final static Log log = LogFactory.getLog(StartApplication.class);

    public static void main(
            String[] args)
    {
        final CloudDataConfigInfo clientConfigurationInfo = (CloudDataConfigInfo) InmemoryLoaderCollection.getInstance().getInmemoryCollection(InmemoryId.CLOUD_INTERFACE_CONFIGURATION);
        while (!clientConfigurationInfo.getLoadedFrstTime())
            try
            {
                Thread.sleep(10);
            }
            catch (final InterruptedException e)
            {
                e.printStackTrace();
            }
        final int                          maxRedisCount = RedisConnectionProvider.getInstance().getRedisPoolCount(Component.CLOUD_ACCEPTOR);
        final Map<String, CloudDataConfig> map           = clientConfigurationInfo.getCloudDataConfig();

        log.warn("******************* started the Request process ************************** ");

        for (final Entry<String, CloudDataConfig> entry : map.entrySet())
        {
            final String key            = entry.getKey();

            final int    maxThreadToHit = entry.getValue().getTotalThreadsToHit();

            for (int index = 1; index <= maxThreadToHit; index++)
            {
                final RequestProcess requestProcess = new RequestProcess(((index % maxRedisCount) + 1), key);
                final Thread         t              = new Thread(requestProcess, "RequestProcess:" + (index));
                t.start();
            }
        }
    }

}