package com.itextos.beacon.platform.smppdlrhandover;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.itextos.beacon.commonlib.componentconsumer.processor.ModuleProcessorInfo;
import com.itextos.beacon.commonlib.componentconsumer.processor.ProcessorInfo;
import com.itextos.beacon.commonlib.constants.ClusterType;
import com.itextos.beacon.commonlib.constants.Component;
import com.itextos.beacon.commonlib.redisconnectionprovider.RedisConnectionProvider;
import com.itextos.beacon.platform.smppdlrhandover.fbp.SmppDlrFallbackPollerHolder;
import com.itextos.beacon.platform.smppdlrhandover.inmemq.InmemoryQueueReaper;
import com.itextos.beacon.platform.smppdlrhandover.util.SmppDlrRedis;

public class SpecialStartApplication
{

    private static final Log       log            = LogFactory.getLog(SpecialStartApplication.class);
    private static final Component THIS_COMPONENT = Component.SMPP_DLR;

    public static void main(
            String[] args)
    {
        if (log.isDebugEnabled())
            log.debug("Starting the application " + THIS_COMPONENT);

        try
        {
        	final String cluster1=System.getProperty("cluster");
            
            if(cluster1==null) {
            
            	System.setProperty("cluster", System.getenv("cluster"));
            }

            final String modvalue=System.getProperty("modvalue");
            
            if(modvalue==null) {
            	
            	System.setProperty("modvalue", System.getenv("modvalue"));

            }
            final ModuleProcessorInfo lProcessor = new ModuleProcessorInfo(THIS_COMPONENT);
            lProcessor.process();

            // ClientWiseCounter.getInstance().init(null, 0);

             String   lClusters    = System.getProperty("cluster");
            
            if(lClusters==null) {
            
            	System.out.println("System.getenv(\"cluster\")"+" : "+System.getenv("cluster"));
          
            	lClusters=System.getenv("cluster");
            }
            
            String[] lClusterList = null;
            
            if(lClusters.indexOf(',')>0) {
            	
            	lClusterList= lClusters.split(",");
            }else {
            
            	lClusterList = new String[1];
            	
            	lClusterList[0]= lClusters;
            	
            }
               

            for (final String lCluster : lClusterList)
            {
                final ClusterType lClusterType = ClusterType.getCluster(lCluster);

                new InmemoryQueueReaper(lClusterType);
            }

            final int lRedisPoolCnt = RedisConnectionProvider.getInstance().getRedisPoolCount(ClusterType.COMMON, Component.SMPP_SESSION);

            for (int redisIndex = 0; redisIndex < lRedisPoolCnt; redisIndex++)
            {
                if (log.isDebugEnabled())
                    log.debug("RedisIndex:'" + redisIndex + "', Component:'" + Component.SMPP_SESSION + "'");

                final SmppDlrRedis lSmppDlrRedis = new SmppDlrRedis((redisIndex + 1));
            }

            SmppDlrFallbackPollerHolder.getInstance();
        }
        catch (final Exception e)
        {
            log.error("Exception while starting component '" + THIS_COMPONENT + "'", e);
            System.exit(-1);
        }
    }

}
