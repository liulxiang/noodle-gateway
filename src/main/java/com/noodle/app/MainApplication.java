package com.noodle.app;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * 多协议服务器主应用程序
 * 
 * 集成了以下协议服务：
 * - MQTT 服务器 (基于Moquette)
 * - 多种数据存储：Redis InfluxDB
 */
@EnableAsync
@EnableScheduling
@ComponentScan(basePackages = {"com.noodle"})
@SpringBootApplication(exclude = { DataSourceAutoConfiguration.class }, scanBasePackages = "com.noodle")
public class MainApplication {
    public static void main(String[] args) {
        // 设置系统属性
        System.setProperty("java.net.preferIPv4Stack", "true");
        System.setProperty("io.netty.leakDetectionLevel", "SIMPLE");
        // 启动Spring Boot应用
        SpringApplication app = new SpringApplication(MainApplication.class);
        // 设置默认配置文件
        java.util.Properties defaultProperties = new java.util.Properties();
        defaultProperties.put("spring.application.name", "noodle-gateway");
        defaultProperties.put("server.port", "8080");
        app.setDefaultProperties(defaultProperties);
        app.run(args);
    }
}