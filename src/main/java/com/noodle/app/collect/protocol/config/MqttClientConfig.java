package com.noodle.app.collect.protocol.config;

import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * MQTT客户端配置
 */
@Component
@ConfigurationProperties(prefix = "mqtt.client")
public class MqttClientConfig {
    
    /**
     * 是否启用MQTT客户端
     */
    private boolean enabled = true;
    
    /**
     * MQTT服务器地址
     */
    private String brokerUrl = "tcp://127.0.0.1:1883";
    
    /**
     * 客户端ID
     */
    private String clientId = "protocol-server-client";
    
    /**
     * 用户名
     */
    private String username;
    
    /**
     * 密码
     */
    private String password;
    
    /**
     * 保持连接时间（秒）
     */
    private int keepAlive = 60;
    
    /**
     * 是否清除会话
     */
    private boolean cleanSession = true;
    
    /**
     * 是否自动重连
     */
    private boolean autoReconnect = true;
    
    /**
     * 重连延迟（毫秒）
     */
    private long reconnectDelay = 5000;
    
    /**
     * 最大重连延迟（毫秒）
     */
    private long maxReconnectDelay = 60000;
    
    /**
     * 订阅主题列表
     */
    private List<TopicConfig> topics;
    
    /**
     * 主题配置
     */
    public static class TopicConfig {
        private String topic;
        private int qos = 1;
        
        public TopicConfig() {}
        
        public TopicConfig(String topic, int qos) {
            this.topic = topic;
            this.qos = qos;
        }
        
        // Getters and Setters
        public String getTopic() {
            return topic;
        }
        
        public void setTopic(String topic) {
            this.topic = topic;
        }
        
        public int getQos() {
            return qos;
        }
        
        public void setQos(int qos) {
            this.qos = qos;
        }
        
        @Override
        public String toString() {
            return "TopicConfig{" +
                    "topic='" + topic + '\'' +
                    ", qos=" + qos +
                    '}';
        }
    }
    
    // Getters and Setters
    public boolean isEnabled() {
        return enabled;
    }
    
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
    
    public String getBrokerUrl() {
        return brokerUrl;
    }
    
    public void setBrokerUrl(String brokerUrl) {
        this.brokerUrl = brokerUrl;
    }
    
    public String getClientId() {
        return clientId;
    }
    
    public void setClientId(String clientId) {
        this.clientId = clientId;
    }
    
    public String getUsername() {
        return username;
    }
    
    public void setUsername(String username) {
        this.username = username;
    }
    
    public String getPassword() {
        return password;
    }
    
    public void setPassword(String password) {
        this.password = password;
    }
    
    public int getKeepAlive() {
        return keepAlive;
    }
    
    public void setKeepAlive(int keepAlive) {
        this.keepAlive = keepAlive;
    }
    
    public boolean isCleanSession() {
        return cleanSession;
    }
    
    public void setCleanSession(boolean cleanSession) {
        this.cleanSession = cleanSession;
    }
    
    public boolean isAutoReconnect() {
        return autoReconnect;
    }
    
    public void setAutoReconnect(boolean autoReconnect) {
        this.autoReconnect = autoReconnect;
    }
    
    public long getReconnectDelay() {
        return reconnectDelay;
    }
    
    public void setReconnectDelay(long reconnectDelay) {
        this.reconnectDelay = reconnectDelay;
    }
    
    public long getMaxReconnectDelay() {
        return maxReconnectDelay;
    }
    
    public void setMaxReconnectDelay(long maxReconnectDelay) {
        this.maxReconnectDelay = maxReconnectDelay;
    }
    
    public List<TopicConfig> getTopics() {
        return topics;
    }
    
    public void setTopics(List<TopicConfig> topics) {
        this.topics = topics;
    }
    
    @Override
    public String toString() {
        return "MqttClientConfig{" +
                "enabled=" + enabled +
                ", brokerUrl='" + brokerUrl + '\'' +
                ", clientId='" + clientId + '\'' +
                ", username='" + username + '\'' +
                ", keepAlive=" + keepAlive +
                ", cleanSession=" + cleanSession +
                ", autoReconnect=" + autoReconnect +
                ", reconnectDelay=" + reconnectDelay +
                ", maxReconnectDelay=" + maxReconnectDelay +
                ", topics=" + topics +
                '}';
    }
}