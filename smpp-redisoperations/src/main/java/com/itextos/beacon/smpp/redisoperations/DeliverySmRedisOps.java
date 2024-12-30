package com.itextos.beacon.smpp.redisoperations;

import java.lang.reflect.Type;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.itextos.beacon.platform.smpputil.DeliverSmInfo;

import redis.clients.jedis.Jedis;

public class DeliverySmRedisOps
{

    private static final Log log = LogFactory.getLog(DeliverySmRedisOps.class);

    private DeliverySmRedisOps()
    {}

    public static List<DeliverSmInfo> lpopDeliverSm(
            String aClientId)
    {

        try (
                Jedis jedis = SmppRedisConnectionProvider.getSmppDlrRedis(aClientId);)
        {
            final byte[] lPopString = jedis.lpop((RedisKeyConstants.SMPP_DN_QUEUE + aClientId).getBytes());

            if ((lPopString != null) && (lPopString.length > 0))
            {
                final String strFromRedis = new String(lPopString);

                if (log.isDebugEnabled())
                    log.info("strFromRedis=" + strFromRedis);

                final Type                type            = new TypeToken<List<DeliverSmInfo>>()
                                                          {}.getType();
                final List<DeliverSmInfo> listOfDeliverSm = new Gson().fromJson(strFromRedis, type);

                return listOfDeliverSm;
            }
        }
        catch (final Exception exp)
        {
            log.error("problem popping dlr list...", exp);
        }

        return null;
    }
    
    /*
    
    public static Set<String> getKeys()
    {

    	Set<String> result=new HashSet<String>();
    	
        try (
                Jedis jedis = SmppRedisConnectionProvider.getSmppDlrRedis(aClientId);)
        {
        	Set<byte[]> keys=jedis.keys((RedisKeyConstants.SMPP_DN_QUEUE + ".*").getBytes());

        	keys.forEach((lPopString)->{
        		
        		   if ((lPopString != null) && (lPopString.length > 0))
                   {
                        String strFromRedis = new String(lPopString);

                       result.add(strFromRedis);
                    
                   }
        	});

         
        }
        catch (final Exception exp)
        {
            log.error("problem popping dlr list...", exp);
        }

        return result;
    }

*/
    public static boolean lpushDeliverSm(
            String aClientId,
            String aDliverySmJson)
    {
        boolean pushed = false;

        try (
                Jedis jedis = SmppRedisConnectionProvider.getSmppDlrRedis(aClientId))
        {
            pushed = jedis.lpush((RedisKeyConstants.SMPP_DN_QUEUE + aClientId), aDliverySmJson) > 0;
        }
        catch (final Exception exp)
        {
            log.error("problem pushing back dlr...", exp);
        }
        return pushed;
    }

}
