package com.itextos.beacon.platform.redisstore;

import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;

import redis.clients.jedis.Jedis;

public class RedisHashMapStorage {
	
	public static  Map<String, Object> get(String keyname) {
        // Connect to Redis
        Jedis jedis = null;

        try {
        	jedis= getJedisConnection();
        	
            // Get JSON string from Redis
            String json = jedis.get(keyname);
            
            // Convert JSON back to HashMap
            ObjectMapper objectMapper = new ObjectMapper();
            Map<String, Object> retrievedMap = objectMapper.readValue(json, Map.class);
            
           return retrievedMap;

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            jedis.close();
        }
        
        return null;
    }
	
    private static Jedis getJedisConnection() {
		// TODO Auto-generated method stub
		return null;
	}

	public static void put(String keyname, Map<String, Object> largeMap) {
        // Connect to Redis
        Jedis jedis = null;
        
        // Create large HashMap
       

        try {
        	
        	jedis= getJedisConnection();

            // Convert HashMap to JSON string
            ObjectMapper objectMapper = new ObjectMapper();
            String json = objectMapper.writeValueAsString(largeMap);
            
            // Store JSON in Redis
            jedis.set(keyname, json);
           

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            jedis.close();
        }
    }
}
