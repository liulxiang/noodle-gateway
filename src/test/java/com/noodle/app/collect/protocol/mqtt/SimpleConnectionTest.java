package com.noodle.app.collect.protocol.mqtt;

import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

import java.util.UUID;

/**
 * 简化版MQTT连接测试类
 * 用于快速验证MQTT服务器连接问题
 */
public class SimpleConnectionTest {

    public static void main(String[] args) {
        String broker = "tcp://127.0.0.1:1884";
        String clientId = "simple-test-client-" + UUID.randomUUID().toString();
        
        System.out.println("=== 简化版MQTT连接测试 ===");
        System.out.println("服务器地址: " + broker);
        System.out.println("客户端ID: " + clientId);
        
        MqttClient client = null;
        
        try {
            // 创建客户端
            client = new MqttClient(broker, clientId, new MemoryPersistence());
            
            // 配置连接选项
            MqttConnectOptions options = new MqttConnectOptions();
            options.setCleanSession(true);
            options.setMqttVersion(MqttConnectOptions.MQTT_VERSION_3_1_1);
            options.setConnectionTimeout(10);
            options.setKeepAliveInterval(30);
            options.setAutomaticReconnect(false);
            
            System.out.println("尝试连接...");
            client.connect(options);
            
            if (client.isConnected()) {
                System.out.println("✓ 连接成功!");
                // 立即断开
                client.disconnect();
                System.out.println("✓ 已断开连接");
            } else {
                System.out.println("✗ 连接失败");
            }
            
        } catch (MqttException e) {
            System.err.println("连接异常: " + e.getMessage());
            System.err.println("原因码: " + e.getReasonCode());
            e.printStackTrace();
        } catch (Exception e) {
            System.err.println("其他异常: " + e.getMessage());
            e.printStackTrace();
        } finally {
            if (client != null) {
                try {
                    if (client.isConnected()) {
                        client.disconnect();
                    }
                    client.close();
                } catch (MqttException e) {
                    System.err.println("关闭客户端时出错: " + e.getMessage());
                }
            }
            System.out.println("测试完成");
        }
    }
}