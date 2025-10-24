package com.noodle.app.collect.protocol.mqtt;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

/**
 * MQTT连接测试类
 * 专门用于测试与Netty MQTT服务器的连接
 */
public class ConnectionTest {
    
    public static void main(String[] args) {
        // 确保MQTT服务器配置正确
        String broker = "tcp://192.168.1.8:1884"; // 使用IP地址而不是localhost
        // 使用UUID确保每次运行都有唯一的客户端ID
        String clientId = "connection-test-client-" + UUID.randomUUID().toString();
        String topic = "test/connection";
        String content = "Connection test message";
        
        System.out.println("=== MQTT连接测试开始 ===");
        System.out.println("服务器地址: " + broker);
        System.out.println("客户端ID: " + clientId);
        System.out.println("请确保MQTT服务器正在端口1884上运行...");
        
        MqttClient client = null;
        
        try {
            // 检查类加载
            System.out.println("检查MQTT客户端库是否可用...");
            Class.forName("org.eclipse.paho.client.mqttv3.MqttClient");
            System.out.println("✓ MQTT客户端库加载成功");
            
            // 创建MQTT客户端
            System.out.println("创建MQTT客户端...");
            client = new MqttClient(broker, clientId, new MemoryPersistence());
            System.out.println("✓ MQTT客户端创建成功");
            
            // 设置连接选项
            System.out.println("配置连接选项...");
            MqttConnectOptions options = new MqttConnectOptions();
            options.setCleanSession(true); // 确保使用clean session
            options.setKeepAliveInterval(60);
            // 确保使用MQTT 3.1.1版本（版本4），这是服务器支持的唯一版本
            options.setMqttVersion(MqttConnectOptions.MQTT_VERSION_3_1_1);
            options.setConnectionTimeout(30);
            options.setAutomaticReconnect(false); // 关闭自动重连以避免连接冲突
            
            // 设置遗嘱消息
            options.setWill(topic, "Client disconnected unexpectedly".getBytes(), 1, false);
            
            // 添加更多兼容性设置
            options.setMaxInflight(10); // 限制在飞消息数
            options.setKeepAliveInterval(30); // 设置合理的keep-alive时间
            
            // 连接到MQTT服务器
            System.out.println("使用MQTT版本: 3.1.1");
            System.out.println("客户端ID: " + clientId);
            System.out.println("尝试连接到MQTT服务器...");
            System.out.println("连接参数: 超时=" + options.getConnectionTimeout() + "秒, KeepAlive=" + options.getKeepAliveInterval() + "秒");
            
            try {
                System.out.println("开始连接...");
                client.connect(options);
                
                if (client.isConnected()) {
                    System.out.println("✓ 成功连接到MQTT服务器!");
                    System.out.println("服务器地址: " + client.getCurrentServerURI());
                    System.out.println("连接状态: " + client.isConnected());
                    
                    // 发送测试消息
                    System.out.println("正在发送测试消息到主题: " + topic);
                    Map<String, Object> meterData =new HashMap<String, Object>();
                    meterData.put("aaaa", "bbb");
                    content=meterData.toString();
                    MqttMessage message = new MqttMessage(content.getBytes());
                    message.setQos(1);
                    client.publish(topic, message);
                    System.out.println("✓ 消息发送成功: " + content);
                    
                    // 等待一小段时间确保消息发送完成
                    Thread.sleep(1000);
                    
                    // 断开连接
                    System.out.println("正在断开连接...");
                    client.disconnectForcibly(5000, 5000, false); // 强制断开连接
                    System.out.println("✓ 连接已断开");
                } else {
                    System.out.println("✗ 连接对象创建但未实际连接成功");
                }
            } catch (MqttException e) {
                System.err.println("✗ MQTT连接失败");
                System.err.println("错误代码: " + e.getReasonCode());
                System.err.println("错误消息: " + e.getMessage());
                System.err.println("常见原因:");
                System.err.println("1. MQTT服务器未启动或未在端口1884运行");
                System.err.println("2. 网络连接问题或防火墙阻止");
                System.err.println("3. 服务器需要认证但未提供凭据");
                System.err.println("4. MQTT协议版本不兼容");
                System.err.println("完整错误栈:");
                e.printStackTrace();
                
                // 提供验证建议
                System.err.println("\n建议验证步骤:");
                System.err.println("1. 确认MQTT服务器已在端口1884启动: netstat -an | grep 1884");
                System.err.println("2. 尝试连接公共MQTT服务器验证客户端: tcp://broker.emqx.io:1883");
                System.err.println("3. 检查服务器日志查看连接被拒绝的具体原因");
            }
            
        } catch (ClassNotFoundException e) {
            System.err.println("✗ MQTT客户端库未找到");
            e.printStackTrace();
        } catch (Exception e) {
            System.err.println("✗ 发生意外错误: " + e.getMessage());
            e.printStackTrace();
        } finally {
            System.out.println("=== 测试结束 ===");
            if (client != null) {
                try {
                    System.out.println("清理客户端资源...");
                    if (client.isConnected()) {
                        System.out.println("强制关闭客户端连接...");
                        client.disconnectForcibly(1000, 1000, false); // 强制断开
                    }
                    client.close(true); // 强制关闭并清理资源
                    System.out.println("✓ 客户端资源已清理");
                } catch (MqttException e) {
                    System.err.println("关闭客户端时发生错误: " + e.getMessage());
                    e.printStackTrace();
                }
            }
        }
    }
}