package com.itextos.beacon.interfaces.cloudacceptor.servlets;

import java.io.IOException;
import java.util.Map;
import java.util.Map.Entry;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.itextos.beacon.commonlib.constants.Component;
import com.itextos.beacon.commonlib.constants.InterfaceType;
import com.itextos.beacon.commonlib.messageidentifier.MessageIdentifier;
import com.itextos.beacon.commonlib.redis.RedisConnectionProvider;
import com.itextos.beacon.inmemory.loader.InmemoryLoaderCollection;
import com.itextos.beacon.inmemory.loader.process.InmemoryId;
import com.itextos.beacon.interfaces.cloudacceptor.common.RedisPusher;
import com.itextos.beacon.interfaces.clouddatautil.common.CloudDataConfig;
import com.itextos.beacon.interfaces.clouddatautil.common.CloudDataConfigInfo;

@WebServlet(
        value = "/initservlet",
        loadOnStartup = 1)
public final class InitServlet
        extends
        HttpServlet
{

    private static final long serialVersionUID = 8051819642012902265L;
    private final static Log  log              = LogFactory.getLog(InitServlet.class);

    public InitServlet()
    {}

    @Override
    public void init()
            throws ServletException
    {
        log.warn("Initializing the RedisConnectionPool and RedisPusher");

        MessageIdentifier.getInstance().init(InterfaceType.CLOUD_API);
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

        for (final Entry<String, CloudDataConfig> entry : map.entrySet())
        {
            final String key = entry.getKey();

            for (int index = 0; index < maxRedisCount; index++)
            {
                final RedisPusher rp = new RedisPusher(index + 1, key);
                final Thread      t  = new Thread(rp, "RedisPusher:" + key + ("-" + (index + 1)));
                t.start();
            }
        }
    }

    @Override
    protected void doGet(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException,
            IOException
    {
        // Nothing to do here
    }

    @Override
    protected void doPost(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException,
            IOException
    {
        // Nothing to do here
    }

}
