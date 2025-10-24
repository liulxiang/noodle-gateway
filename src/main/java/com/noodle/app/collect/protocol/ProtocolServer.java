package com.noodle.app.collect.protocol;

/**
 * 服务器管理接口
 */
public interface ProtocolServer {
    
    /**
     * 启动服务器
     */
    void start() throws Exception;
    
    /**
     * 停止服务器
     */
    void stop() throws Exception;
    
    /**
     * 检查服务器是否运行中
     */
    boolean isRunning();
    
    /**
     * 获取服务器名称
     */
    String getServerName();
}