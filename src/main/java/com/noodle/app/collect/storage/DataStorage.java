package com.noodle.app.collect.storage;

import java.util.List;

import com.noodle.app.collect.storage.model.ProtocolData;

/**
 * 数据存储接口
 */
public interface DataStorage {
    
    /**
     * 存储单个数据点
     */
    void store(ProtocolData data);
    
    /**
     * 批量存储数据点
     */
    void storeBatch(List<ProtocolData> dataList);
    
    /**
     * 获取存储类型
     */
    String getStorageType();
    
    /**
     * 检查连接状态
     */
    boolean isConnected();
    
    /**
     * 初始化存储
     */
    void initialize();
    
    /**
     * 销毁存储
     */
    void destroy();
}