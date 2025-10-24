package com.noodle.app.collect.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * RocketMQ配置类
 * 
 * 通过rocketmq.enabled控制是否启用RocketMQ消息队列功能
 * - true: 启用RocketMQ消息推送（默认）
 * - false: 禁用RocketMQ消息推送
 */
@Component
@ConfigurationProperties(prefix = "rocketmq")
public class RocketMqConfig {
    
    /**
     * RocketMQ总开关
     * true: 启用所有RocketMQ相关功能
     * false: 禁用所有RocketMQ相关功能
     */
    private boolean enabled = false;  // 默认关闭，需要明确配置才开启
    
    /**
     * Name Server地址
     */
    private String nameServer = "127.0.0.1:9876";
    
    /**
     * 报警主题名称
     */
    private String alarmTopic = "alarm-topic";
    
    /**
     * 生产者配置
     */
    private Producer producer = new Producer();
    
    /**
     * 消费者配置
     */
    private Consumer consumer = new Consumer();
    
    /**
     * 生产者配置类
     */
    public static class Producer {
        private String group = "alarm-producer-group";
        private int sendMessageTimeout = 3000;
        private int retryTimesWhenSendFailed = 2;
        private int retryTimesWhenSendAsyncFailed = 2;
        private int maxMessageSize = 4096;
        private int compressMessageBodyThreshold = 4096;
        
        // Getters and Setters
        public String getGroup() {
            return group;
        }
        
        public void setGroup(String group) {
            this.group = group;
        }
        
        public int getSendMessageTimeout() {
            return sendMessageTimeout;
        }
        
        public void setSendMessageTimeout(int sendMessageTimeout) {
            this.sendMessageTimeout = sendMessageTimeout;
        }
        
        public int getRetryTimesWhenSendFailed() {
            return retryTimesWhenSendFailed;
        }
        
        public void setRetryTimesWhenSendFailed(int retryTimesWhenSendFailed) {
            this.retryTimesWhenSendFailed = retryTimesWhenSendFailed;
        }
        
        public int getRetryTimesWhenSendAsyncFailed() {
            return retryTimesWhenSendAsyncFailed;
        }
        
        public void setRetryTimesWhenSendAsyncFailed(int retryTimesWhenSendAsyncFailed) {
            this.retryTimesWhenSendAsyncFailed = retryTimesWhenSendAsyncFailed;
        }
        
        public int getMaxMessageSize() {
            return maxMessageSize;
        }
        
        public void setMaxMessageSize(int maxMessageSize) {
            this.maxMessageSize = maxMessageSize;
        }
        
        public int getCompressMessageBodyThreshold() {
            return compressMessageBodyThreshold;
        }
        
        public void setCompressMessageBodyThreshold(int compressMessageBodyThreshold) {
            this.compressMessageBodyThreshold = compressMessageBodyThreshold;
        }
    }
    
    /**
     * 消费者配置类
     */
    public static class Consumer {
        private String group = "alarm-consumer-group";
        private int consumeMessageBatchMaxSize = 1;
        private int consumeTimeout = 15;
        
        // Getters and Setters
        public String getGroup() {
            return group;
        }
        
        public void setGroup(String group) {
            this.group = group;
        }
        
        public int getConsumeMessageBatchMaxSize() {
            return consumeMessageBatchMaxSize;
        }
        
        public void setConsumeMessageBatchMaxSize(int consumeMessageBatchMaxSize) {
            this.consumeMessageBatchMaxSize = consumeMessageBatchMaxSize;
        }
        
        public int getConsumeTimeout() {
            return consumeTimeout;
        }
        
        public void setConsumeTimeout(int consumeTimeout) {
            this.consumeTimeout = consumeTimeout;
        }
    }
    
    // Getters and Setters
    public boolean isEnabled() {
        return enabled;
    }
    
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
    
    public String getNameServer() {
        return nameServer;
    }
    
    public void setNameServer(String nameServer) {
        this.nameServer = nameServer;
    }
    
    public String getAlarmTopic() {
        return alarmTopic;
    }
    
    public void setAlarmTopic(String alarmTopic) {
        this.alarmTopic = alarmTopic;
    }
    
    public Producer getProducer() {
        return producer;
    }
    
    public void setProducer(Producer producer) {
        this.producer = producer;
    }
    
    public Consumer getConsumer() {
        return consumer;
    }
    
    public void setConsumer(Consumer consumer) {
        this.consumer = consumer;
    }
    
    @Override
    public String toString() {
        return "RocketMqConfig{" +
                "enabled=" + enabled +
                ", nameServer='" + nameServer + '\'' +
                ", alarmTopic='" + alarmTopic + '\'' +
                ", producerGroup='" + producer.getGroup() + '\'' +
                ", consumerGroup='" + consumer.getGroup() + '\'' +
                ", sendTimeout=" + producer.getSendMessageTimeout() +
                ", retryTimes=" + producer.getRetryTimesWhenSendFailed() +
                '}';
    }
    
    /**
     * 检查RocketMQ是否可用
     */
    public boolean isAvailable() {
        return enabled && nameServer != null && !nameServer.trim().isEmpty();
    }
}