package com.noodle.app.collect.protocol.mqtt;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import com.noodle.app.collect.protocol.config.MqttClientConfig;
import com.noodle.app.collect.storage.DataStorageService;
import com.noodle.app.collect.storage.model.ProtocolData;

/**
 * MQTT客户端服务
 * 用于连接外部MQTT服务器，订阅主题并将接收的数据写入存储
 */
@Service
@ConditionalOnProperty(name = "mqtt.client.enabled", havingValue = "true", matchIfMissing = false)
public class MqttClientService implements MqttCallback {
    
    private static final Logger logger = LoggerFactory.getLogger(MqttClientService.class);
    
    @Autowired
    private MqttClientConfig config;
    
    @Autowired
    private DataStorageService dataStorageService;
    
    private MqttClient mqttClient;
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(2);
    private volatile boolean connected = false;
    private volatile boolean reconnecting = false;
    private long lastReconnectTime = 0;
    private long currentReconnectDelay;
    
    @PostConstruct
    public void initialize() {
        if (!config.isEnabled()) {
            logger.info("MQTT客户端已禁用");
            return;
        }
        
        logger.info("正在初始化MQTT客户端: {}", config);
        currentReconnectDelay = config.getReconnectDelay();
        
        // 延迟启动MQTT客户端，确保其他服务已启动
        scheduler.schedule(this::connectToMqttBroker, 5, TimeUnit.SECONDS);
    }
    
    /**
     * 连接到MQTT服务器
     */
    private void connectToMqttBroker() {
        String clientId = config.getClientId() + "_" + System.currentTimeMillis();
        try {
            // 创建MQTT客户端
            mqttClient = new MqttClient(config.getBrokerUrl(), clientId, new MemoryPersistence());
            mqttClient.setCallback(this);
            
            // 设置连接选项
            MqttConnectOptions options = new MqttConnectOptions();
            options.setCleanSession(config.isCleanSession());
            options.setKeepAliveInterval(config.getKeepAlive());
            options.setAutomaticReconnect(false); // 手动控制重连
            options.setMqttVersion(MqttConnectOptions.MQTT_VERSION_3_1_1); // 明确指定MQTT版本
            options.setConnectionTimeout(30); // 设置连接超时
            
            // 添加更多兼容性设置
            options.setKeepAliveInterval(60); // 增加keep-alive时间
            options.setMaxInflight(100); // 设置最大在飞消息数
            options.setServerURIs(new String[]{config.getBrokerUrl()}); // 设置服务器URI
            
            if (config.getUsername() != null && !config.getUsername().isEmpty()) {
                options.setUserName(config.getUsername());
            }
            if (config.getPassword() != null && !config.getPassword().isEmpty()) {
                options.setPassword(config.getPassword().toCharArray());
            }
            
            // 连接到MQTT服务器
            logger.info("正在连接到MQTT服务器: {}", config.getBrokerUrl());
            mqttClient.connect(options);
            connected = true;
            reconnecting = false;
            currentReconnectDelay = config.getReconnectDelay(); // 重置重连延迟
            
            logger.info("成功连接到MQTT服务器: {}", config.getBrokerUrl());
            
            // 订阅主题
            subscribeToTopics();
            
        } catch (Exception e) {
            logger.error("连接到MQTT服务器失败: {}", e.getMessage(), e);
            logger.error("MQTT服务器地址: {}", config.getBrokerUrl());
            logger.error("客户端ID: {}", clientId);
            connected = false;
            
            // 如果启用自动重连，安排重连
            if (config.isAutoReconnect()) {
                scheduleReconnect();
            }
        }
    }
    
    /**
     * 订阅主题
     */
    private void subscribeToTopics() {
        if (config.getTopics() != null && !config.getTopics().isEmpty()) {
            for (MqttClientConfig.TopicConfig topicConfig : config.getTopics()) {
                try {
                    mqttClient.subscribe(topicConfig.getTopic(), topicConfig.getQos());
                    logger.info("成功订阅主题: {} (QoS: {})", topicConfig.getTopic(), topicConfig.getQos());
                } catch (Exception e) {
                    logger.error("订阅主题失败: {} - {}", topicConfig.getTopic(), e.getMessage());
                }
            }
        } else {
            logger.warn("没有配置要订阅的主题");
        }
    }
    
    /**
     * 安排重连
     */
    private void scheduleReconnect() {
        if (reconnecting) {
            return;
        }
        
        reconnecting = true;
        long now = System.currentTimeMillis();
        
        // 避免过于频繁的重连尝试
        if (now - lastReconnectTime < 1000) {
            logger.debug("重连过于频繁，跳过此次重连尝试");
            reconnecting = false;
            return;
        }
        
        lastReconnectTime = now;
        
        logger.info("安排在 {} 毫秒后重连到MQTT服务器", currentReconnectDelay);
        scheduler.schedule(() -> {
            if (!connected) {
                connectToMqttBroker();
                
                // 指数退避重连策略
                currentReconnectDelay = Math.min(currentReconnectDelay * 2, config.getMaxReconnectDelay());
            }
            reconnecting = false;
        }, currentReconnectDelay, TimeUnit.MILLISECONDS);
    }
    
    @Override
    public void connectionLost(Throwable cause) {
        logger.warn("与MQTT服务器的连接丢失: {}", cause.getMessage());
        connected = false;
        
        if (config.isAutoReconnect()) {
            scheduleReconnect();
        }
    }
    
    @Override
    public void messageArrived(String topic, MqttMessage message) throws Exception {
        try {
            // 解析消息
            String payload = new String(message.getPayload());
            logger.debug("收到MQTT消息 - 主题: {}, 消息: {}, QoS: {}", topic, payload, message.getQos());
            
            // 创建协议数据对象
            ProtocolData protocolData = createProtocolData(topic, payload, message);
            // 存储数据
            dataStorageService.store(protocolData);
            
            logger.debug("成功存储MQTT数据: 主题={}, 数据={}", topic, payload);
            
        } catch (Exception e) {
            logger.error("处理MQTT消息失败: 主题={}, 错误={}", topic, e.getMessage(), e);
        }
    }
    
    @Override
    public void deliveryComplete(IMqttDeliveryToken token) {
        // 客户端发送消息完成回调（当前实现主要用于接收消息）
        logger.debug("消息发送完成: {}", token.getMessageId());
    }
    
    /**
     * 创建协议数据对象
     */
    private ProtocolData createProtocolData(String topic, String payload, MqttMessage message) {
        ProtocolData data = new ProtocolData();
        data.setTimestamp(Instant.now());
        data.setProtocol("mqtt");
        data.setDeviceId(extractDeviceIdFromTopic(topic));
        data.setAddress(topic);
        data.setOrgData(payload);
        return data;
    }
    
    /**
     * 从主题中提取设备ID
     */
    private String extractDeviceIdFromTopic(String topic) {
        try {
            // 尝试从主题路径中提取设备ID
            // 例如: sensor/device001/temperature -> device001
            String[] parts = topic.split("/");
            if (parts.length >= 2) {
                // 通常设备ID在第二个位置
                return parts[1];
            }
            return "unknown_device";
        } catch (Exception e) {
            return "unknown_device";
        }
    }
    
    /**
     * 确定数据类型
     */
    private String determineDataType(String value) {
        if (value == null || value.isEmpty()) {
            return "string";
        }
        
        // 尝试解析为数字
        try {
            if (value.contains(".")) {
                Double.parseDouble(value);
                return "float";
            } else {
                Long.parseLong(value);
                return "integer";
            }
        } catch (NumberFormatException e) {
            // 检查布尔值
            if ("true".equalsIgnoreCase(value) || "false".equalsIgnoreCase(value)) {
                return "boolean";
            }
            
            // 检查JSON
            if ((value.startsWith("{") && value.endsWith("}")) ||
                (value.startsWith("[") && value.endsWith("]"))) {
                return "json";
            }
            
            return "string";
        }
    }
    
    /**
     * 发布消息到MQTT服务器
     */
    public void publishMessage(String topic, String payload, int qos) {
        if (!connected || mqttClient == null) {
            logger.warn("MQTT客户端未连接，无法发布消息");
            return;
        }
        
        try {
            MqttMessage message = new MqttMessage(payload.getBytes());
            message.setQos(qos);
            mqttClient.publish(topic, message);
            logger.debug("成功发布MQTT消息 - 主题: {}, 消息: {}", topic, payload);
        } catch (Exception e) {
            logger.error("发布MQTT消息失败: 主题={}, 错误={}", topic, e.getMessage());
        }
    }
    
    /**
     * 获取连接状态
     */
    public boolean isConnected() {
        return connected && mqttClient != null && mqttClient.isConnected();
    }
    
    /**
     * 手动连接到MQTT服务器
     */
    public boolean connect() {
        if (!config.isEnabled()) {
            logger.info("MQTT客户端已禁用，无法连接");
            return false;
        }
        
        try {
            connectToMqttBroker();
            return isConnected();
        } catch (Exception e) {
            logger.error("手动连接MQTT服务器失败: {}", e.getMessage(), e);
            return false;
        }
    }
    
    /**
     * 获取客户端统计信息
     */
    public Map<String, Object> getStatistics() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("connected", isConnected());
        stats.put("brokerUrl", config.getBrokerUrl());
        stats.put("clientId", config.getClientId());
        stats.put("reconnecting", reconnecting);
        stats.put("subscribedTopics", config.getTopics() != null ? config.getTopics().size() : 0);
        
        if (mqttClient != null) {
            try {
                stats.put("pendingDeliveryTokens", mqttClient.getPendingDeliveryTokens().length);
            } catch (Exception e) {
                stats.put("pendingDeliveryTokens", "N/A");
            }
        }
        
        return stats;
    }
    
    @PreDestroy
    public void shutdown() {
        try {
            logger.info("正在关闭MQTT客户端...");
            
            if (mqttClient != null && mqttClient.isConnected()) {
                mqttClient.disconnect();
                mqttClient.close();
            }
            
            scheduler.shutdown();
            if (!scheduler.awaitTermination(10, TimeUnit.SECONDS)) {
                scheduler.shutdownNow();
            }
            
            logger.info("MQTT客户端已关闭");
        } catch (Exception e) {
            logger.error("关闭MQTT客户端时发生错误", e);
        }
    }
}