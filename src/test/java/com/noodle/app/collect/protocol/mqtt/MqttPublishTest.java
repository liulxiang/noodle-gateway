package com.noodle.app.collect.protocol.mqtt;

import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

/**
 * MQTT消息发布测试类
 */
public class MqttPublishTest {
    
    public static void main(String[] args) {
        String broker = "tcp://localhost:1884";
        String clientId = "TestPublisher";
        String topic = "test/topic";
        String content = "Hello MQTT World!";
        
        try {
            // 创建MQTT客户端
            MqttClient client = new MqttClient(broker, clientId, new MemoryPersistence());
            
            // 设置连接选项
            MqttConnectOptions options = new MqttConnectOptions();
            options.setCleanSession(true);
            
            // 连接到服务器
            System.out.println("Connecting to broker: " + broker);
            client.connect(options);
            System.out.println("Connected");
            
            // 创建消息
            MqttMessage message = new MqttMessage(content.getBytes());
            message.setQos(1);
            
            // 发布消息
            System.out.println("Publishing message: " + content);
            client.publish(topic, message);
            System.out.println("Message published");
            
            // 断开连接
            client.disconnect();
            System.out.println("Disconnected");
            
            // 关闭客户端
            client.close();
            
        } catch (MqttException e) {
            System.err.println("MQTT Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}