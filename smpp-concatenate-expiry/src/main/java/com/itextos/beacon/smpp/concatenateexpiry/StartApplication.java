package com.itextos.beacon.smpp.concatenateexpiry;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.itextos.beacon.commonlib.constants.ClusterType;
import com.itextos.beacon.commonlib.constants.Component;
import com.itextos.beacon.commonlib.redisconnectionprovider.RedisConnectionProvider;
import com.itextos.beacon.commonlib.utility.CommonUtility;
import com.itextos.beacon.smpp.utils.SmppApplicationParams;
import com.itextos.beacon.smpp.utils.properties.SmppProperties;

public class StartApplication
{

    private static final Log   log         = LogFactory.getLog(StartApplication.class);

    public void start()
    {

        try
        {
          
   
            startConcatenateAndExpiryProcessors();



        }
        catch (final Exception exp)
        {
            log.error("Problem starting server...", exp);
        }
    }

    

    

    private static void startConcatenateAndExpiryProcessors()
    {
        
            startCompletedMessageProcessor();
       
    }

  

    private static void startCompletedMessageProcessor()
    {
        final int lConcatPoolerConsumerCount = SmppProperties.getInstance().getConcatMessagePoolerRedisConsumerCount();
        if (log.isDebugEnabled())
            log.debug("Concat Pooler Consumer Count : " + lConcatPoolerConsumerCount);

        for (final ClusterType lClusterType : SmppApplicationParams.getInstance().getClusters())
        {
            if (log.isDebugEnabled())
                log.debug("Cluster Value : " + lClusterType);

            final int lConcatRedisPool = RedisConnectionProvider.getInstance().getRedisPoolCount(lClusterType, Component.SMPP_CONCAT);
            if (log.isDebugEnabled())
                log.debug(lClusterType + ", Concat Redis Pool : " + lConcatRedisPool);

            final List<String> runtimeRedisIndex = getRedisIndices(lConcatRedisPool);

            for (int redisIndex = 0; redisIndex < lConcatRedisPool; redisIndex++)
            {
                if (log.isDebugEnabled())
                    log.debug(lClusterType + ", Concat Redis Pool Index:'" + redisIndex + "'");

                if (runtimeRedisIndex.contains("" + redisIndex))
                {
                    if (log.isDebugEnabled())
                        log.debug("Cluster : " + lClusterType + " :: Redis Index :" + redisIndex);

                    new CompletedMessageChecker(lClusterType, redisIndex);
                    ExpiryMessageCollectionFactory.getInstance().addExpiryProcessor(lClusterType, redisIndex);

               
                }
            }
        }
    }

    

    private static List<String> getRedisIndices(
            int aConcatRedisPool)
    {
        final String redisIndices = CommonUtility.nullCheck(System.getProperty("concat.redis.index"), true);

        List<String> arr          = null;

        if (!redisIndices.isEmpty())
        {
            final String[] temp = StringUtils.split(redisIndices, ",");
            arr = new ArrayList(temp.length);
            final int index = 0;
            for (final String s : temp)
                arr.add(s);
        }

        final List<String> returnValue = new ArrayList<>();
        if (arr == null)
            for (int index = 0; index < aConcatRedisPool; index++)
                returnValue.add("" + index);
        else
            for (int index = 0; index < aConcatRedisPool; index++)
                if (arr.contains("" + index))
                    returnValue.add("" + index);

        ((ArrayList<String>) returnValue).trimToSize();

        return returnValue;
    }

    public static void main(
            String[] args)
    {
    	
    	System.out.println("System going to Start");
        new StartApplication().start();
    }

}