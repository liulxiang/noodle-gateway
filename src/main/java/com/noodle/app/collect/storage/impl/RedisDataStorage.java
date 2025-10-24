package com.noodle.app.collect.storage.impl;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.noodle.app.collect.protocol.config.DataStorageConfig;
import com.noodle.app.collect.storage.DataStorage;
import com.noodle.app.collect.storage.model.ProtocolData;

/**
 * Redis数据存储实现
 */
@Component
@ConditionalOnProperty(name = "data.storage.type", havingValue = "redis")
public class RedisDataStorage implements DataStorage {

    private static final Logger logger = LoggerFactory.getLogger(RedisDataStorage.class);

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Autowired
    private DataStorageConfig storageConfig;

    @Autowired
    private ObjectMapper objectMapper;

    @PostConstruct
    @Override
    public void initialize() {
        logger.info("Initializing Redis data storage...");
        if (isConnected()) {
            logger.info("Redis data storage initialized successfully");
        } else {
            logger.error("Failed to connect to Redis");
        }
    }

    @PreDestroy
    @Override
    public void destroy() {
        logger.info("Destroying Redis data storage...");
    }

    @Override
    public void store(ProtocolData data) {
        try {
            String key = buildKey(data.getProtocol(), data.getDeviceId(), data.getAddress());
            String latestKey = buildLatestKey(data.getProtocol(), data.getDeviceId(), data.getAddress());
            
            // 存储到时序集合（使用时间戳作为分数）
            double score = data.getTimestamp().toEpochMilli();
            String jsonData = objectMapper.writeValueAsString(data);
            
            redisTemplate.opsForZSet().add(key, jsonData, score);
            
            // 存储最新值
            redisTemplate.opsForValue().set(latestKey, jsonData);
            
            // 设置过期时间
            if (storageConfig.getRedis().isUseKeyExpiration()) {
                long ttl = storageConfig.getRedis().getTimeToLive();
                redisTemplate.expire(key, ttl, TimeUnit.SECONDS);
                redisTemplate.expire(latestKey, ttl, TimeUnit.SECONDS);
            }
            
            logger.debug("Stored data to Redis: {}", data);
            
        } catch (Exception e) {
            logger.error("Failed to store data to Redis: {}", e.getMessage(), e);
        }
    }

    @Override
    public void storeBatch(List<ProtocolData> dataList) {
        if (dataList == null || dataList.isEmpty()) {
            return;
        }

        try {
            // 按key分组批量操作
            Map<String, List<ProtocolData>> groupedData = dataList.stream()
                    .collect(Collectors.groupingBy(data -> 
                            buildKey(data.getProtocol(), data.getDeviceId(), data.getAddress())));

            for (Map.Entry<String, List<ProtocolData>> entry : groupedData.entrySet()) {
                String key = entry.getKey();
                List<ProtocolData> dataGroup = entry.getValue();
                
                // 批量添加到ZSet
                Set<ZSetOperations.TypedTuple<Object>> tuples = new HashSet<>();
                ProtocolData latestData = null;
                
                for (ProtocolData data : dataGroup) {
                    try {
                        String jsonData = objectMapper.writeValueAsString(data);
                        double score = data.getTimestamp().toEpochMilli();
                        tuples.add(ZSetOperations.TypedTuple.of(jsonData, score));
                        
                        if (latestData == null || data.getTimestamp().isAfter(latestData.getTimestamp())) {
                            latestData = data;
                        }
                    } catch (Exception e) {
                        logger.error("Failed to serialize data: {}", e.getMessage());
                    }
                }
                
                if (!tuples.isEmpty()) {
                    redisTemplate.opsForZSet().add(key, tuples);
                    
                    // 更新最新值
                    if (latestData != null) {
                        try {
                            String latestKey = buildLatestKey(latestData.getProtocol(), 
                                    latestData.getDeviceId(), latestData.getAddress());
                            String latestJsonData = objectMapper.writeValueAsString(latestData);
                            redisTemplate.opsForValue().set(latestKey, latestJsonData);
                            
                            // 设置过期时间
                            if (storageConfig.getRedis().isUseKeyExpiration()) {
                                long ttl = storageConfig.getRedis().getTimeToLive();
                                redisTemplate.expire(key, ttl, TimeUnit.SECONDS);
                                redisTemplate.expire(latestKey, ttl, TimeUnit.SECONDS);
                            }
                        } catch (Exception e) {
                            logger.error("Failed to store latest data: {}", e.getMessage());
                        }
                    }
                }
            }
            
            logger.debug("Stored {} data points to Redis in batch", dataList.size());
            
        } catch (Exception e) {
            logger.error("Failed to store batch data to Redis: {}", e.getMessage(), e);
        }
    }

    @Override
    public String getStorageType() {
        return "redis";
    }

    @Override
    public boolean isConnected() {
        try {
            redisTemplate.opsForValue().set("test:connection", "ok", 1, TimeUnit.SECONDS);
            String result = (String) redisTemplate.opsForValue().get("test:connection");
            return "ok".equals(result);
        } catch (Exception e) {
            logger.error("Redis connection test failed: {}", e.getMessage());
            return false;
        }
    }

    /**
     * 构建存储键
     */
    private String buildKey(String protocol, String deviceId, String address) {
        return String.format("%s%s:%s:%s", 
                storageConfig.getRedis().getKeyPrefix(), protocol, deviceId, address);
    }

    /**
     * 构建最新值键
     */
    private String buildLatestKey(String protocol, String deviceId, String address) {
        return String.format("%s%s:%s:%s:latest", 
                storageConfig.getRedis().getKeyPrefix(), protocol, deviceId, address);
    }

    /**
     * 构建键模式
     */
    private String buildKeyPattern(String protocol) {
        return String.format("%s%s:*", 
                storageConfig.getRedis().getKeyPrefix(), protocol);
    }
}