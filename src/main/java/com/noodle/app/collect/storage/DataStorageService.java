package com.noodle.app.collect.storage;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

import com.noodle.app.collect.protocol.config.DataStorageConfig;
import com.noodle.app.collect.storage.model.ProtocolData;

/**
 * 数据存储服务
 */
@Service
public class DataStorageService {

    private static final Logger logger = LoggerFactory.getLogger(DataStorageService.class);

    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    private DataStorageConfig storageConfig;
    

    private DataStorage dataStorage;
    private final BlockingQueue<ProtocolData> dataQueue = new LinkedBlockingQueue<>();
    private ScheduledExecutorService scheduledExecutor;
    private ExecutorService batchExecutor;

    @PostConstruct
    public void initialize() {
        if (!storageConfig.isEnabled()) {
            logger.info("Data storage is disabled");
            return;
        }

        try {
            // 根据配置选择存储实现
            String storageType = storageConfig.getType();
            Map<String, DataStorage> storageMap = applicationContext.getBeansOfType(DataStorage.class);
            
            dataStorage = storageMap.values().stream()
                    .filter(storage -> storage.getStorageType().equals(storageType))
                    .findFirst()
                    .orElse(null);

            if (dataStorage == null) {
                logger.error("No storage implementation found for type: {}", storageType);
                return;
            }

            dataStorage.initialize();
            logger.info("Using {} storage implementation", storageType);

            // 启动批量写入服务
            startBatchWriteService();
            // 启动清理任务
            startCleanupTask();

        } catch (Exception e) {
            logger.error("Failed to initialize data storage service: {}", e.getMessage(), e);
        }
    }

    /**
     * 存储数据（异步）
     */
    public void store(ProtocolData data) {
        if (!isEnabled() || data == null) {
            return;
        }
        try {
            // 触发报警检查
            dataQueue.offer(data);
            logger.debug("Queued data for storage: {}", data);
        } catch (Exception e) {
            logger.error("Failed to queue data for storage: {}", e.getMessage(), e);
        }
    }

    /**
     * 存储数据（同步）
     */
    public void storeSync(ProtocolData data) {
        if (!isEnabled() || data == null) {
            return;
        }

        try {
            // 触发报警检查
            dataStorage.store(data);
        } catch (Exception e) {
            logger.error("Failed to store data synchronously: {}", e.getMessage(), e);
        }
    }

    /**
     * 批量存储数据
     */
    public void storeBatch(List<ProtocolData> dataList) {
        if (!isEnabled() || dataList == null || dataList.isEmpty()) {
            return;
        }
        try {
            // 对每个数据触发报警检查
            dataStorage.storeBatch(dataList);
        } catch (Exception e) {
            logger.error("Failed to store batch data: {}", e.getMessage(), e);
        }
    }

    /**
     * 检查存储是否启用
     */
    public boolean isEnabled() {
        return storageConfig.isEnabled() && dataStorage != null;
    }

    /**
     * 获取存储类型
     */
    public String getStorageType() {
        return dataStorage != null ? dataStorage.getStorageType() : "none";
    }

    /**
     * 检查连接状态
     */
    public boolean isConnected() {
        return dataStorage != null && dataStorage.isConnected();
    }

    /**
     * 启动批量写入服务
     */
    private void startBatchWriteService() {
        batchExecutor = Executors.newSingleThreadExecutor(r -> {
            Thread thread = new Thread(r, "data-storage-batch");
            thread.setDaemon(true);
            return thread;
        });

        batchExecutor.submit(() -> {
            List<ProtocolData> batch = new ArrayList<>();
            long lastFlushTime = System.currentTimeMillis();

            while (!Thread.currentThread().isInterrupted()) {
                try {
                    // 从队列中取数据
                    ProtocolData data = dataQueue.poll(1, TimeUnit.SECONDS);
                    if (data != null) {
                        batch.add(data);
                    }

                    // 检查是否需要刷新批次
                    long currentTime = System.currentTimeMillis();
                    boolean shouldFlush = batch.size() >= storageConfig.getBatchSize() ||
                            (currentTime - lastFlushTime) >= storageConfig.getWriteInterval();

                    if (shouldFlush && !batch.isEmpty()) {
                        try {
                            dataStorage.storeBatch(new ArrayList<>(batch));
                            logger.debug("Flushed {} data points to storage", batch.size());
                            batch.clear();
                            lastFlushTime = currentTime;
                        } catch (Exception e) {
                            logger.error("Failed to flush batch data: {}", e.getMessage(), e);
                            // 重新排队失败的数据
                            dataQueue.addAll(batch);
                            batch.clear();
                        }
                    }

                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                } catch (Exception e) {
                    logger.error("Error in batch write service: {}", e.getMessage(), e);
                }
            }

            // 处理剩余数据
            if (!batch.isEmpty()) {
                try {
                    dataStorage.storeBatch(batch);
                    logger.info("Flushed remaining {} data points on shutdown", batch.size());
                } catch (Exception e) {
                    logger.error("Failed to flush remaining data on shutdown: {}", e.getMessage(), e);
                }
            }
        });

        logger.info("Batch write service started");
    }

    /**
     * 启动清理任务
     */
    private void startCleanupTask() {
        scheduledExecutor = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread thread = new Thread(r, "data-storage-cleanup");
            thread.setDaemon(true);
            return thread;
        });
        logger.info("Cleanup task started");
    }

    /**
     * 关闭服务
     */
    public void shutdown() {
        logger.info("Shutting down data storage service...");
        
        if (batchExecutor != null) {
            batchExecutor.shutdown();
            try {
                if (!batchExecutor.awaitTermination(10, TimeUnit.SECONDS)) {
                    batchExecutor.shutdownNow();
                }
            } catch (InterruptedException e) {
                batchExecutor.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }

        if (scheduledExecutor != null) {
            scheduledExecutor.shutdown();
        }

        if (dataStorage != null) {
            dataStorage.destroy();
        }

        logger.info("Data storage service shutdown completed");
    }
}