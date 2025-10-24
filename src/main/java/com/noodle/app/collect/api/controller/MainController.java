package com.noodle.app.collect.api.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.noodle.app.collect.protocol.ProtocolServer;

/**
 * 主控制器 - 提供服务器总体状态信息
 */
@RestController
@RequestMapping("/api")
public class MainController {

    @Autowired
    private ApplicationContext applicationContext;

    /**
     * 获取所有协议服务器的状态
     */
    @GetMapping("/status")
    public Map<String, Object> getServerStatus() {
        Map<String, Object> status = new HashMap<>();
        
        // 获取所有协议服务器
        Map<String, ProtocolServer> servers = applicationContext.getBeansOfType(ProtocolServer.class);
        Map<String, Object> serverStatus = new HashMap<>();
        
        int runningCount = 0;
        int totalCount = servers.size();
        
        for (Map.Entry<String, ProtocolServer> entry : servers.entrySet()) {
            ProtocolServer server = entry.getValue();
            Map<String, Object> serverInfo = new HashMap<>();
            serverInfo.put("name", server.getServerName());
            serverInfo.put("running", server.isRunning());
            
            if (server.isRunning()) {
                runningCount++;
            }
            
            serverStatus.put(entry.getKey(), serverInfo);
        }
        
        status.put("servers", serverStatus);
        status.put("totalServers", totalCount);
        status.put("runningServers", runningCount);
        status.put("allRunning", runningCount == totalCount);
        status.put("timestamp", System.currentTimeMillis());
        
        return status;
    }

    /**
     * 获取服务器信息
     */
    @GetMapping("/info")
    public Map<String, Object> getServerInfo() {
        Map<String, Object> info = new HashMap<>();
        
        info.put("application", "Protocol Server");
        info.put("version", "1.0.0");
        info.put("description", "基于Spring Boot和Netty的多协议服务器");
        info.put("protocols", new String[]{"MQTT", "WebSocket", "Modbus TCP", "IEC 60870-5-104"});
        
        // 运行时信息
        Runtime runtime = Runtime.getRuntime();
        Map<String, Object> runtimeInfo = new HashMap<>();
        runtimeInfo.put("javaVersion", System.getProperty("java.version"));
        runtimeInfo.put("totalMemory", runtime.totalMemory() / 1024 / 1024 + " MB");
        runtimeInfo.put("freeMemory", runtime.freeMemory() / 1024 / 1024 + " MB");
        runtimeInfo.put("maxMemory", runtime.maxMemory() / 1024 / 1024 + " MB");
        runtimeInfo.put("processors", runtime.availableProcessors());
        runtimeInfo.put("uptime", System.currentTimeMillis());
        
        info.put("runtime", runtimeInfo);
        info.put("timestamp", System.currentTimeMillis());
        
        return info;
    }

    /**
     * 健康检查
     */
    @GetMapping("/health")
    public Map<String, Object> healthCheck() {
        Map<String, Object> health = new HashMap<>();
        
        Map<String, ProtocolServer> servers = applicationContext.getBeansOfType(ProtocolServer.class);
        boolean allHealthy = servers.values().stream().allMatch(ProtocolServer::isRunning);
        
        health.put("status", allHealthy ? "UP" : "DOWN");
        health.put("timestamp", System.currentTimeMillis());
        
        return health;
    }
}