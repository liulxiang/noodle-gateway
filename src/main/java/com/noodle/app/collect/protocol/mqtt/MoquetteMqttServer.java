package com.noodle.app.collect.protocol.mqtt;

import com.noodle.app.collect.protocol.AbstractProtocolServer;
import com.noodle.app.collect.protocol.config.MqttServerConfig;
import com.noodle.app.collect.storage.DataStorageService;
import com.noodle.app.collect.storage.model.ProtocolData;

import io.moquette.broker.Server;
import io.moquette.broker.config.IConfig;
import io.moquette.broker.config.MemoryConfig;
import io.moquette.interception.AbstractInterceptHandler;
import io.moquette.interception.InterceptHandler;
import io.moquette.interception.messages.InterceptConnectMessage;
import io.moquette.interception.messages.InterceptDisconnectMessage;
import io.moquette.interception.messages.InterceptPublishMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Map;
import java.util.Properties;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 基于Moquette的MQTT服务器
 */
@Component
@ConditionalOnProperty(name = "mqtt.server.implementation", havingValue = "moquette", matchIfMissing = false)
public class MoquetteMqttServer extends AbstractProtocolServer {

    @Autowired
    private MqttServerConfig config;
    @Autowired(required = false)
    private DataStorageService dataStorageService;

    private Server mqttBroker;
    
    // 客户端会话管理
    private final Map<String, String> clientSessions = new ConcurrentHashMap<>();

    @Override
    public String getServerName() {
        return "MQTT (Moquette)";
    }

    @Override
    protected void doInit() throws Exception {
        // Moquette初始化不需要特殊处理
        logger.info("Initializing Moquette MQTT server");
    }

    @Override
    protected void doStart() throws Exception {
        mqttBroker = new Server();
        
        // 创建消息拦截器
        InterceptHandler interceptHandler = new MqttMessageInterceptHandler();
        
        // 配置Moquette服务器
        Properties properties = new Properties();
        properties.setProperty(IConfig.PORT_PROPERTY_NAME, String.valueOf(config.getPort()));
        properties.setProperty(IConfig.HOST_PROPERTY_NAME, config.getHost());
        properties.setProperty(IConfig.ALLOW_ANONYMOUS_PROPERTY_NAME, "true");
        properties.setProperty(IConfig.ENABLE_TELEMETRY_NAME, "false");
        
        // WebSocket配置
        if (config.getWebsocketPort() > 0) {
            properties.setProperty(IConfig.WEB_SOCKET_PORT_PROPERTY_NAME, String.valueOf(config.getWebsocketPort()));
        }
        
        MemoryConfig memoryConfig = new MemoryConfig(properties);
        
        try {
            // 启动服务器并添加拦截器
            mqttBroker.startServer(memoryConfig, Collections.singletonList(interceptHandler));
            logger.info("Moquette MQTT Server started on {}:{}", config.getHost(), config.getPort());
        } catch (IOException e) {
            throw new RuntimeException("Failed to start Moquette MQTT server", e);
        }
    }

    @Override
    protected void doStop() throws Exception {
        if (mqttBroker != null) {
            mqttBroker.stopServer();
            logger.info("Moquette MQTT Server stopped");
        }
    }
    
    /**
     * 获取当前连接的客户端数量
     */
    public int getClientCount() {
        return clientSessions.size();
    }
    
    /**
     * 获取所有连接的客户端ID
     */
    public java.util.Set<String> getConnectedClients() {
        return clientSessions.keySet();
    }
    
    /**
     * MQTT消息拦截处理器
     */
    private class MqttMessageInterceptHandler extends AbstractInterceptHandler {
        
        @Override
        public String getID() {
            return "NoodleGatewayMessageHandler";
        }
        
        @Override
        public void onConnect(InterceptConnectMessage msg) {
            String clientId = msg.getClientID();
            clientSessions.put(clientId, clientId);
            logger.info("Client connected: {}", clientId);
        }
        
        @Override
        public void onDisconnect(InterceptDisconnectMessage msg) {
            String clientId = msg.getClientID();
            clientSessions.remove(clientId);
            logger.info("Client disconnected: {}", clientId);
        }
        
        @Override
        public void onPublish(InterceptPublishMessage msg) {
            String topic = msg.getTopicName();
            String clientId = msg.getClientID();
            
            // 安全地获取payload字节数组
            byte[] payload;
            if (msg.getPayload().hasArray()) {
                payload = msg.getPayload().array();
            } else {
                // 对于没有直接数组支持的ByteBuf，需要复制数据
                payload = new byte[msg.getPayload().readableBytes()];
                msg.getPayload().getBytes(msg.getPayload().readerIndex(), payload);
            }
            
            String payloadStr = new String(payload);
            
            logger.debug("Received MQTT message from client {}: topic={}, payload={}", 
                        clientId, topic, payloadStr);
            
            // 存储MQTT消息数据
            if (dataStorageService != null && clientId != null) {
                try {
                    ProtocolData data = new ProtocolData();
                    data.setClient(clientId);
                    data.setAddress(topic);
                    data.setOrgData(payloadStr);
                    dataStorageService.store(data);
                } catch (Exception e) {
                    logger.error("Failed to store MQTT publish data: {}", e.getMessage());
                }
            }
        }
        
        @Override
        public void onSessionLoopError(Throwable error) {
            logger.error("MQTT session loop error", error);
        }
    }
}