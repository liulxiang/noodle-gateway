package com.noodle.app.collect.protocol.config;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.noodle.app.collect.protocol.ProtocolServer;

/**
 * 协议服务器启动配置
 */
@Configuration
@EnableConfigurationProperties({
    MqttServerConfig.class,
    DataStorageConfig.class
})
public class ProtocolServerAutoConfig {

    private static final Logger logger = LoggerFactory.getLogger(ProtocolServerAutoConfig.class);

    @Autowired
    private ApplicationContext applicationContext;

    /**
     * 自动启动所有协议服务器
     */
    @Bean
    public ApplicationRunner protocolServerRunner() {
        return args -> {
            logger.info("Starting Protocol Servers...");
            
            // 获取所有实现了ProtocolServer接口的Bean
            Map<String, ProtocolServer> servers = applicationContext.getBeansOfType(ProtocolServer.class);
            
            int startedServers = 0;
            int failedServers = 0;
            
            for (Map.Entry<String, ProtocolServer> entry : servers.entrySet()) {
                String beanName = entry.getKey();
                ProtocolServer server = entry.getValue();
                
                try {
                    logger.info("Starting {} server (bean: {})...", server.getServerName(), beanName);
                    server.start();
                    startedServers++;
                    logger.info("{} server started successfully", server.getServerName());
                } catch (Exception e) {
                    failedServers++;
                    logger.error("Failed to start {} server (bean: {}): {}", 
                               server.getServerName(), beanName, e.getMessage(), e);
                }
            }
            
            logger.info("Protocol Server startup completed. Started: {}, Failed: {}, Total: {}", 
                       startedServers, failedServers, servers.size());
            
            if (startedServers > 0) {
                printServerSummary();
            }
        };
    }

    /**
     * 打印服务器摘要信息
     */
    private void printServerSummary() {
        logger.info("==========================================");
        logger.info("    多协议服务器启动成功");
        logger.info("==========================================");
        logger.info("HTTP管理接口: http://localhost:8080");
        logger.info("MQTT服务器: tcp://localhost:1884");
        logger.info("==========================================");
        logger.info("API接口文档:");
        logger.info("- MQTT状态: GET /api/mqtt/status");
        logger.info("==========================================");
    }
}