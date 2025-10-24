package com.noodle.app.collect.protocol.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 数据存储配置
 */
@ConfigurationProperties(prefix = "data.storage")
public class DataStorageConfig {
    
    /**
     * 存储类型: influxdb, redis
     */
    private String type = "influxdb";
    
    /**
     * 是否启用数据存储
     */
    private boolean enabled = true;
    
    /**
     * 数据保留时间（秒）
     */
    private long retentionTime = 86400; // 24小时
    
    /**
     * 批量写入大小
     */
    private int batchSize = 100;
    
    /**
     * 写入间隔（毫秒）
     */
    private long writeInterval = 5000; // 5秒
    
    /**
     * InfluxDB配置
     */
    private InfluxConfig influx = new InfluxConfig();
    

    
    /**
     * Redis配置
     */
    private RedisConfig redis = new RedisConfig();
    
    // Getters and Setters
    public String getType() {
        return type;
    }
    
    public void setType(String type) {
        this.type = type;
    }
    
    public boolean isEnabled() {
        return enabled;
    }
    
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
    
    public long getRetentionTime() {
        return retentionTime;
    }
    
    public void setRetentionTime(long retentionTime) {
        this.retentionTime = retentionTime;
    }
    
    public int getBatchSize() {
        return batchSize;
    }
    
    public void setBatchSize(int batchSize) {
        this.batchSize = batchSize;
    }
    
    public long getWriteInterval() {
        return writeInterval;
    }
    
    public void setWriteInterval(long writeInterval) {
        this.writeInterval = writeInterval;
    }
    
    public InfluxConfig getInflux() {
        return influx;
    }
    
    public void setInflux(InfluxConfig influx) {
        this.influx = influx;
    }
    

    
    public RedisConfig getRedis() {
        return redis;
    }
    
    public void setRedis(RedisConfig redis) {
        this.redis = redis;
    }
    
    /**
     * InfluxDB配置
     */
    @ConfigurationProperties(prefix = "data.storage.influx")
    public static class InfluxConfig {
        private String url;
        private String token;
        private String org;
        private String bucket;
        private String table;
        private int connectionTimeout = 10000;
        private int readTimeout = 30000;
        private int writeTimeout = 10000;
        
        public String getTable() {
			return table;
		}

		public void setTable(String table) {
			this.table = table;
		}

		// Getters and Setters
        public String getUrl() {
            return url;
        }
        
        public void setUrl(String url) {
            this.url = url;
        }
        
        public String getToken() {
            return token;
        }
        
        public void setToken(String token) {
            this.token = token;
        }
        
        public String getOrg() {
            return org;
        }
        
        public void setOrg(String org) {
            this.org = org;
        }
        
        public String getBucket() {
            return bucket;
        }
        
        public void setBucket(String bucket) {
            this.bucket = bucket;
        }
        
        public int getConnectionTimeout() {
            return connectionTimeout;
        }
        
        public void setConnectionTimeout(int connectionTimeout) {
            this.connectionTimeout = connectionTimeout;
        }
        
        public int getReadTimeout() {
            return readTimeout;
        }
        
        public void setReadTimeout(int readTimeout) {
            this.readTimeout = readTimeout;
        }
        
        public int getWriteTimeout() {
            return writeTimeout;
        }
        
        public void setWriteTimeout(int writeTimeout) {
            this.writeTimeout = writeTimeout;
        }
    }
    

    
    /**
     * Redis配置
     */
    @ConfigurationProperties(prefix = "data.storage.redis")
    public static class RedisConfig {
        private String keyPrefix = "protocol:";
        private int database = 0;
        private long timeToLive = 86400; // 24小时
        private boolean useKeyExpiration = true;
        private String serialization = "json"; // json, binary
        
        // Getters and Setters
        public String getKeyPrefix() {
            return keyPrefix;
        }
        
        public void setKeyPrefix(String keyPrefix) {
            this.keyPrefix = keyPrefix;
        }
        
        public int getDatabase() {
            return database;
        }
        
        public void setDatabase(int database) {
            this.database = database;
        }
        
        public long getTimeToLive() {
            return timeToLive;
        }
        
        public void setTimeToLive(long timeToLive) {
            this.timeToLive = timeToLive;
        }
        
        public boolean isUseKeyExpiration() {
            return useKeyExpiration;
        }
        
        public void setUseKeyExpiration(boolean useKeyExpiration) {
            this.useKeyExpiration = useKeyExpiration;
        }
        
        public String getSerialization() {
            return serialization;
        }
        
        public void setSerialization(String serialization) {
            this.serialization = serialization;
        }
    }
}