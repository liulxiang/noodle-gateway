package com.noodle.app.collect.protocol.mqtt;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.noodle.app.collect.protocol.ProtocolServer;

/**
 * MQTT REST API控制器
 */
@RestController
@RequestMapping("/api/mqtt")
@ConditionalOnProperty(name = "mqtt.server.enabled", havingValue = "true", matchIfMissing = true)
public class MqttController {

    private static final Logger logger = LoggerFactory.getLogger(MqttController.class);
    
    @Autowired
    private ProtocolServer mqttServer;
    
    @Autowired(required = false)
    private MqttClientService mqttClientService;

    /**
     * 获取服务器状态
     */
    @GetMapping("/status")
    public Map<String, Object> getStatus() {
        Map<String, Object> status = new HashMap<>();
        status.put("running", mqttServer.isRunning());
        status.put("serverName", mqttServer.getServerName());
        status.put("connections", 0); // 暂时返回0，后续可以实现连接计数
        return status;
    }

    /**
     * 获取会话统计
     */
    @GetMapping("/sessions")
    public Map<String, Object> getSessions() {
        Map<String, Object> result = new HashMap<>();
        try {
            result.put("success", true);
            // 现在只有Moquette实现，暂时返回0
            result.put("totalSessions", 0);
            result.put("activeSessions", 0); // 简化处理
        } catch (Exception e) {
            result.put("success", false);
            result.put("error", e.getMessage());
        }
        return result;
    }

    /**
     * 获取当前连接的客户端列表
     */
    @GetMapping("/clients")
    public Map<String, Object> getConnectedClients() {
        Map<String, Object> result = new HashMap<>();
        try {
            result.put("success", true);
            
            // 检查服务器是否运行
            if (!mqttServer.isRunning()) {
                result.put("clients", new String[0]);
                result.put("count", 0);
                return result;
            }
            
            // 获取连接的客户端列表
            // 现在只有Moquette实现
            if (mqttServer instanceof MoquetteMqttServer) {
                MoquetteMqttServer moquetteServer = (MoquetteMqttServer) mqttServer;
                java.util.Set<String> clients = moquetteServer.getConnectedClients();
                result.put("clients", clients.toArray(new String[0]));
                result.put("count", clients.size());
            } else {
                // 其他实现，暂时返回空列表
                result.put("clients", new String[0]);
                result.put("count", 0);
            }
        } catch (Exception e) {
            result.put("success", false);
            result.put("error", e.getMessage());
        }
        return result;
    }

    /**
     * 发布消息到指定主题
     */
    @PostMapping("/publish")
    public Map<String, Object> publishMessage(@RequestParam String topic, 
                                             @RequestParam String message,
                                             @RequestParam(defaultValue = "0") int qos) {
        Map<String, Object> result = new HashMap<>();
        try {
            // 注意：此功能需要Moquette支持直接发布消息的接口
            // 当前实现仅作为占位符
            logger.info("Publishing message to topic '{}': {}", topic, message);
            result.put("success", true);
            result.put("topic", topic);
            result.put("message", message);
            result.put("qos", qos);
            result.put("warning", "此功能需要Moquette支持直接发布消息的接口，当前仅作为占位符");
        } catch (Exception e) {
            result.put("success", false);
            result.put("error", e.getMessage());
            logger.error("Failed to publish message to topic '{}': {}", topic, e.getMessage());
        }
        return result;
    }

    /**
     * 获取主题订阅信息
     */
    @GetMapping("/topics")
    public Map<String, Object> getTopics() {
        Map<String, Object> result = new HashMap<>();
        try {
            result.put("success", true);
            result.put("totalTopics", 0); // 暂时返回0
            result.put("topics", new HashMap<>());
        } catch (Exception e) {
            result.put("success", false);
            result.put("error", e.getMessage());
        }
        return result;
    }
    
    /**
     * 测试 MQTT 服务器写入数据
     * 该接口用于测试向 MQTT 服务器发布各种类型的数据
     */
    @PostMapping("/test/publish")
    public Map<String, Object> testMqttPublish(
            @RequestParam(defaultValue = "test/sensor/temperature") String topic,
            @RequestParam(defaultValue = "25.6") String value,
            @RequestParam(defaultValue = "0") int qos,
            @RequestParam(required = false) String deviceId,
            @RequestParam(required = false) String unit) {
        
        Map<String, Object> result = new HashMap<>();
        try {
            // 构造测试数据
            Map<String, Object> testData = new HashMap<>();
            testData.put("timestamp", System.currentTimeMillis());
            testData.put("value", value);
            
            if (deviceId != null && !deviceId.isEmpty()) {
                testData.put("deviceId", deviceId);
            }
            
            if (unit != null && !unit.isEmpty()) {
                testData.put("unit", unit);
            }
            
            // 转换为 JSON 字符串
            ObjectMapper objectMapper = new ObjectMapper();
            String message = objectMapper.writeValueAsString(testData);
            
            // 注意：此功能需要Moquette支持直接发布消息的接口
            // 当前实现仅作为占位符
            result.put("success", true);
            result.put("topic", topic);
            result.put("message", message);
            result.put("qos", qos);
            result.put("testData", testData);
            result.put("warning", "此功能需要Moquette支持直接发布消息的接口，当前仅作为占位符");
            
            logger.info("测试 MQTT 消息发布成功 - 主题: {}, 消息: {}", topic, message);
            
        } catch (Exception e) {
            result.put("success", false);
            result.put("error", e.getMessage());
            logger.error("测试 MQTT 消息发布失败: {}", e.getMessage());
        }
        
        return result;
    }

    /**
     * 获取MQTT服务器摘要信息
     */
    @GetMapping("/summary")
    public Map<String, Object> getSummary() {
        Map<String, Object> summary = new HashMap<>();
        try {
            summary.put("success", true);
            summary.put("serverStatus", mqttServer.isRunning() ? "running" : "stopped");
            summary.put("serverName", mqttServer.getServerName());
            summary.put("totalConnections", 0);
            summary.put("activeConnections", 0);
            summary.put("totalTopics", 0);
            summary.put("messagesPublished", 0);
            summary.put("messagesReceived", 0);
            
            // 添加MQTT客户端信息
            if (mqttClientService != null) {
                summary.put("clientConnected", mqttClientService.isConnected());
                summary.put("clientStatistics", mqttClientService.getStatistics());
            } else {
                summary.put("clientConnected", false);
                summary.put("clientStatistics", "MQTT客户端未启用");
            }
        } catch (Exception e) {
            summary.put("success", false);
            summary.put("error", e.getMessage());
        }
        return summary;
    }
    
    // ================================ MQTT客户端管理接口 ================================
    
    /**
     * 获取MQTT客户端状态
     */
    @GetMapping("/client/status")
    public Map<String, Object> getClientStatus() {
        Map<String, Object> result = new HashMap<>();
        try {
            if (mqttClientService != null) {
                result.put("success", true);
                result.put("connected", mqttClientService.isConnected());
                result.putAll(mqttClientService.getStatistics());
            } else {
                result.put("success", false);
                result.put("error", "MQTT客户端服务未启用");
            }
        } catch (Exception e) {
            result.put("success", false);
            result.put("error", e.getMessage());
        }
        return result;
    }
    
    /**
     * 通过MQTT客户端发布消息
     */
    @PostMapping("/client/publish")
    public Map<String, Object> publishClientMessage(@RequestParam String topic,
                                                   @RequestParam String message,
                                                   @RequestParam(defaultValue = "1") int qos) {
        Map<String, Object> result = new HashMap<>();
        try {
            if (mqttClientService != null && mqttClientService.isConnected()) {
                mqttClientService.publishMessage(topic, message, qos);
                result.put("success", true);
                result.put("topic", topic);
                result.put("message", message);
                result.put("qos", qos);
                logger.info("通过MQTT客户端发布消息: {}", topic);
            } else {
                result.put("success", false);
                result.put("error", "MQTT客户端未连接或未启用");
            }
        } catch (Exception e) {
            result.put("success", false);
            result.put("error", e.getMessage());
            logger.error("发布MQTT客户端消息失败: {}", e.getMessage());
        }
        return result;
    }
    
    /**
     * 手动连接MQTT客户端
     */
    @PostMapping("/client/connect")
    public Map<String, Object> connectClient() {
        Map<String, Object> result = new HashMap<>();
        try {
            if (mqttClientService != null) {
                boolean connected = mqttClientService.connect();
                result.put("success", connected);
                result.put("connected", connected);
                result.put("message", connected ? "MQTT客户端连接成功" : "MQTT客户端连接失败");
            } else {
                result.put("success", false);
                result.put("error", "MQTT客户端服务未启用");
            }
        } catch (Exception e) {
            result.put("success", false);
            result.put("error", e.getMessage());
            logger.error("连接MQTT客户端失败: {}", e.getMessage());
        }
        return result;
    }
    
    /**
     * 获取MQTT客户端统计信息
     */
    @GetMapping("/client/statistics")
    public Map<String, Object> getClientStatistics() {
        Map<String, Object> result = new HashMap<>();
        try {
            if (mqttClientService != null) {
                result.put("success", true);
                result.putAll(mqttClientService.getStatistics());
            } else {
                result.put("success", false);
                result.put("error", "MQTT客户端服务未启用");
            }
        } catch (Exception e) {
            result.put("success", false);
            result.put("error", e.getMessage());
        }
        return result;
    }
    
    // ================================ 电表数据采集接口 ================================
    
    /**
     * 模拟采集电表数据
     * 生成模拟的电表度数、电压、电流等数据
     */
    @GetMapping("/meter/data")
    public Map<String, Object> getMeterData(@RequestParam(defaultValue = "meter001") String deviceId) {
        Map<String, Object> result = new HashMap<>();
        try {
            Random random = new Random();
            
            // 生成模拟电表数据
            Map<String, Object> meterData = new HashMap<>();
            meterData.put("deviceId", deviceId);
            meterData.put("timestamp", System.currentTimeMillis());
            
            // 电表度数（kWh）- 模拟递增
            double baseReading = 1000.0 + random.nextDouble() * 5000.0;
            double currentReading = baseReading + random.nextDouble() * 10.0;
            meterData.put("energyReading", String.format("%.2f", currentReading));
            meterData.put("energyUnit", "kWh");
            
            // 电压（V）- 正常范围 220V ± 10%
            double voltage = 220.0 + (random.nextDouble() - 0.5) * 44.0;
            meterData.put("voltage", String.format("%.1f", voltage));
            meterData.put("voltageUnit", "V");
            
            // 电流（A）- 根据功率和电压计算
            double power = 1000.0 + random.nextDouble() * 5000.0; // 功率范围 1-6kW
            double current = power / voltage;
            meterData.put("current", String.format("%.2f", current));
            meterData.put("currentUnit", "A");
            
            // 功率因数
            double powerFactor = 0.85 + random.nextDouble() * 0.15;
            meterData.put("powerFactor", String.format("%.2f", powerFactor));
            
            // 有功功率（kW）
            double activePower = power / 1000.0;
            meterData.put("activePower", String.format("%.2f", activePower));
            meterData.put("activePowerUnit", "kW");
            
            // 无功功率（kVar）
            double reactivePower = activePower * Math.tan(Math.acos(powerFactor));
            meterData.put("reactivePower", String.format("%.2f", reactivePower));
            meterData.put("reactivePowerUnit", "kVar");
            
            // 视在功率（kVA）
            double apparentPower = Math.sqrt(Math.pow(activePower, 2) + Math.pow(reactivePower, 2));
            meterData.put("apparentPower", String.format("%.2f", apparentPower));
            meterData.put("apparentPowerUnit", "kVA");
            
            // 频率（Hz）
            double frequency = 50.0 + (random.nextDouble() - 0.5) * 0.2;
            meterData.put("frequency", String.format("%.1f", frequency));
            meterData.put("frequencyUnit", "Hz");
            
            result.put("success", true);
            result.put("deviceId", deviceId);
            result.put("data", meterData);
            result.put("timestamp", System.currentTimeMillis());
            
            logger.info("采集电表数据成功 - 设备ID: {}", deviceId);
            
        } catch (Exception e) {
            result.put("success", false);
            result.put("error", e.getMessage());
            logger.error("采集电表数据失败: {}", e.getMessage());
        }
        return result;
    }
    
    /**
     * 批量采集多个电表数据
     */
    @GetMapping("/meter/batch-data")
    public Map<String, Object> getBatchMeterData(@RequestParam(defaultValue = "3") int count) {
        Map<String, Object> result = new HashMap<>();
        try {
            Map<String, Object> batchData = new HashMap<>();
            
            for (int i = 1; i <= count; i++) {
                String deviceId = "meter" + String.format("%03d", i);
                Map<String, Object> meterData = (Map<String, Object>) getMeterData(deviceId).get("data");
                batchData.put(deviceId, meterData);
            }
            
            result.put("success", true);
            result.put("totalDevices", count);
            result.put("data", batchData);
            result.put("timestamp", System.currentTimeMillis());
            
            logger.info("批量采集电表数据成功 - 设备数量: {}", count);
            
        } catch (Exception e) {
            result.put("success", false);
            result.put("error", e.getMessage());
            logger.error("批量采集电表数据失败: {}", e.getMessage());
        }
        return result;
    }
    
    /**
     * 通过MQTT发布电表数据
     */
    @PostMapping("/meter/publish")
    public Map<String, Object> publishMeterData(@RequestParam(defaultValue = "meter001") String deviceId,
                                              @RequestParam(defaultValue = "devices/meter/data") String topic) {
        Map<String, Object> result = new HashMap<>();
        try {
            // 获取电表数据
            Map<String, Object> meterData = getMeterData(deviceId);
            
            if (Boolean.TRUE.equals(meterData.get("success"))) {
                // 检查MQTT客户端是否可用
                if (mqttClientService != null && mqttClientService.isConnected()) {
                    // 通过MQTT客户端发布数据
                    Map<String, Object> publishResult = publishClientMessage(topic, meterData.toString(), 1);
                    
                    if (Boolean.TRUE.equals(publishResult.get("success"))) {
                        result.put("success", true);
                        result.put("deviceId", deviceId);
                        result.put("topic", topic);
                        result.put("data", meterData.get("data"));
                        result.put("message", "电表数据发布成功");
                        result.put("published", true);
                    } else {
                        result.put("success", false);
                        result.put("error", "MQTT发布失败: " + publishResult.get("error"));
                        result.put("published", false);
                    }
                } else {
                    // MQTT客户端不可用，只返回数据
                    result.put("success", true);
                    result.put("deviceId", deviceId);
                    result.put("data", meterData.get("data"));
                    result.put("message", "电表数据采集成功（MQTT客户端未启用，数据未发布）");
                    result.put("published", false);
                    result.put("mqttClientAvailable", false);
                }
            } else {
                result.put("success", false);
                result.put("error", "电表数据采集失败: " + meterData.get("error"));
            }
            
        } catch (Exception e) {
            result.put("success", false);
            result.put("error", e.getMessage());
            logger.error("发布电表数据失败: {}", e.getMessage());
        }
        return result;
    }
}