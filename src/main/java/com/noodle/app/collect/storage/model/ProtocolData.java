package com.noodle.app.collect.storage.model;

import java.time.Instant;

import lombok.Data;

/**
 * 协议数据模型
 */
@Data
public class ProtocolData {
    /**
     * 协议类型（mqtt）
     */
    private String protocol;
    private String client;
    /**
     * 设备ID或连接ID
     */
    private String deviceId;
    /**
     * 数据点地址或主题 topic
     */
    private String address;
    private Object orgData;
    /**
     * 时间戳
     */
    private Instant timestamp;
    
}