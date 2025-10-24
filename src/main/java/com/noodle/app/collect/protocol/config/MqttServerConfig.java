package com.noodle.app.collect.protocol.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * MQTT服务器配置
 */
@ConfigurationProperties(prefix = "mqtt.server")
public class MqttServerConfig {
    
    private boolean enabled = true;
    private int port = 1883;
    private String host = "0.0.0.0";
    private int websocketPort = 8883;
    private int maxMessageSize = 8192;
    private int keepAliveTimeout = 60;
    private String implementation = "netty"; // "netty" or "moquette"
    
    // Getters and Setters
    public boolean isEnabled() {
        return enabled;
    }
    
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
    
    public int getPort() {
        return port;
    }
    
    public void setPort(int port) {
        this.port = port;
    }
    
    public String getHost() {
        return host;
    }
    
    public void setHost(String host) {
        this.host = host;
    }
    
    public int getWebsocketPort() {
        return websocketPort;
    }
    
    public void setWebsocketPort(int websocketPort) {
        this.websocketPort = websocketPort;
    }
    
    public int getMaxMessageSize() {
        return maxMessageSize;
    }
    
    public void setMaxMessageSize(int maxMessageSize) {
        this.maxMessageSize = maxMessageSize;
    }
    
    public int getKeepAliveTimeout() {
        return keepAliveTimeout;
    }
    
    public void setKeepAliveTimeout(int keepAliveTimeout) {
        this.keepAliveTimeout = keepAliveTimeout;
    }
    
    public String getImplementation() {
        return implementation;
    }
    
    public void setImplementation(String implementation) {
        this.implementation = implementation;
    }
}