package com.noodle.app.collect.protocol;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 抽象协议服务器基类
 */
public abstract class AbstractProtocolServer implements ProtocolServer {
    
    protected final Logger logger = LoggerFactory.getLogger(getClass());
    protected volatile boolean running = false;
    
    @Override
    public boolean isRunning() {
        return running;
    }
    
    protected void setRunning(boolean running) {
        this.running = running;
    }
    
    /**
     * 服务器启动前的初始化工作
     */
    protected abstract void doInit() throws Exception;
    
    /**
     * 服务器启动
     */
    protected abstract void doStart() throws Exception;
    
    /**
     * 服务器停止
     */
    protected abstract void doStop() throws Exception;
    
    @Override
    public void start() throws Exception {
        if (running) {
            logger.warn("{} is already running", getServerName());
            return;
        }
        
        try {
            logger.info("Starting {} server...", getServerName());
            doInit();
            doStart();
            setRunning(true);
            logger.info("{} server started successfully", getServerName());
        } catch (Exception e) {
            logger.error("Failed to start {} server", getServerName(), e);
            setRunning(false);
            throw e;
        }
    }
    
    @Override
    public void stop() throws Exception {
        if (!running) {
            logger.warn("{} is not running", getServerName());
            return;
        }
        
        try {
            logger.info("Stopping {} server...", getServerName());
            doStop();
            setRunning(false);
            logger.info("{} server stopped successfully", getServerName());
        } catch (Exception e) {
            logger.error("Failed to stop {} server", getServerName(), e);
            throw e;
        }
    }
}