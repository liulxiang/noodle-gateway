# 基于moquette 的MQTT协议服务器数据采集项目

基于Spring Boot的多协议服务器，目前支持MQTT协议，集成多种数据存储方案（MySQL、Redis、InfluxDB）和RocketMQ消息队列。

## 🏗️ 系统架构

```
┌─────────────────────────────────────────────────────────────┐
│                    Web管理界面 (Port: 8080)                   │
├─────────────────────────────────────────────────────────────┤
│                     RESTful API Layer                      │
│                  （报警管理、数据查询、系统管理）                  │
├─────────────────────────────────────────────────────────────┤
│  Protocol Services Layer                                   │
│  ┌─────────┐                                               │
│  │  MQTT   │                                               │
│  │ :1884   │                                               │
│  └─────────┘                                               │
├─────────────────────────────────────────────────────────────┤
│                智能报警系统                              │
│  ┌─────────────────────────────────────────────────────────┐ │
│  │     实时阈值监控 | 报警规则管理 | 多级别报警     │ │
│  │     MySQL持久化 + Redis高性能缓存            │ │
│  └─────────────────────────────────────────────────────────┘ │
├─────────────────────────────────────────────────────────────┤
│               Data Processing & Storage Layer               │
│  ┌─────────────────────────────────────────────────────────┐ │
│  │            Unified Data Storage Service               │ │
│  │  ┌─────────┐ ┌─────────┐ ┌─────────────────────────┐  │ │
│  │  │  MySQL  │ │  Redis  │ │      InfluxDB          │  │ │
│  │  │(事务数据) │ │(缓存数据)│ │   (时序数据)           │  │ │
│  │  │(报警规则) │ │(报警缓存)│ │   (IoT传感器数据)        │  │ │
│  │  └─────────┘ └─────────┘ └─────────────────────────┘  │ │
│  └─────────────────────────────────────────────────────────┘ │
└─────────────────────────────────────────────────────────────┘
```

## 🛠 主要功能

- **MQTT协议支持**: 支持QoS 0/1/2, 主题订阅/发布, 客户端管理
- **实时数据模拟**: 内置数据模拟器，支持多种数据类型
- **多种数据存储**: MySQL/Redis/InfluxDB/MyBatis Plus MySQL灵活切换
- **异步批量写入**: 高性能数据持久化，减少IO开销
- **智能报警系统**: 阈值报警、多级别报警、报警记录管理
- **RocketMQ集成**: 报警消息自动推送到消息队列
- **混合存储架构**: MySQL主存储 + Redis缓存
- **启动时数据加载**: 自动从数据库加载配置到缓存
- **Web管理界面**: 直观的系统监控和管理界面
- **RESTful API接口**: 完整的API接口，支持第三方集成
- **完整的配置管理**: 灵活的配置系统，支持多环境
- **日志记录和监控**: 全面的日志系统和性能监控
- **数据统计和查询**: 多维度数据统计和灵活查询

## 快速开始

### 环境要求

- Java 8+
- Maven 3.6+
- MySQL 8.0+ (可选)
- Redis 6.0+ (可选)
- InfluxDB 2.0+ (可选)
- RocketMQ 4.9+ (可选，用于消息队列功能)

### 构建和运行

```bash
# 克隆项目
git clone <repository-url>
cd noodle-gateway

# 编译项目
mvn clean compile

# 运行项目
mvn spring-boot:run
```

## 协议详情

### MQTT服务器

- **端口**: 1884
- **WebSocket端口**: 8883
- **功能**: 支持QoS 0/1/2, 主题订阅/发布, 客户端管理
- **实现**: 基于开源Moquette MQTT Broker

#### MQTT配置示例

```
mqtt:
  server:
    enabled: true
    port: 1884
    host: 0.0.0.0
    websocket-port: 8883
    max-message-size: 8192
    keep-alive-timeout: 60
    implementation: moquette
  # MQTT客户端配置（用于连接外部MQTT服务器）
  client:
    enabled: true
    broker-url: tcp://127.0.0.1:1884
    client-id: protocol-server-client
    username: 
    password: 
    keep-alive: 60
    clean-session: true
    auto-reconnect: true
    reconnect-delay: 5000  # 重连延迟（毫秒）
    max-reconnect-delay: 60000  # 最大重连延迟（毫秒）
    topics:
      - topic: "testtopic/+"  # 订阅的主题，支持通配符
        qos: 1
      - topic: "sensor/+/temperature"
        qos: 1
      - topic: "sensor/+/humidity"
        qos: 1
      - topic: "device/+/status"
        qos: 0
```

#### MQTT公共服务接口

- `GET /api/mqtt/status` - MQTT服务状态
- `GET /api/mqtt/clients` - 获取连接的客户端列表
- `POST /api/mqtt/client/publish` - 通过MQTT客户端发布消息
- `GET /api/mqtt/topics` - 获取活跃主题列表
- `POST /api/mqtt/client/connect` - 手动连接MQTT客户端

## 监控和管理

### Web管理界面

访问 http://localhost:8080 可以看到：

- 服务器运行状态
- 实时数据监控
- 系统信息显示

### 日志配置

日志文件位置: `logs/protocol-server.log`

可以通过修改`application.yml`中的日志配置来调整日志级别：

```
logging:
  level:
    com.noodle.app.collect.protocol: DEBUG
    org.springframework.web: DEBUG
    org.thymeleaf: DEBUG
    root: INFO
    # 添加Moquette MQTT服务器的DEBUG日志
    com.noodle.app.collect.protocol.mqtt.MoquetteMqttServer: DEBUG
  pattern:
    console: "%d{yyyy-MM-dd HH:mm:ss} [%thread] %-5level %logger{36} - %msg%n"
  file:
    name: logs/protocol-server.log
```

## 开发指南

### 项目结构

```
src/main/java/com/noodle/app/collect/protocol/
├── config/          # 配置类
├── mqtt/            # MQTT服务实现
│   ├── MoquetteMqttServer.java  # MQTT服务器实现
│   ├── MqttClientService.java   # MQTT客户端服务
│   ├── MqttController.java      # MQTT REST接口
│   └── 其他测试类
├── alarm/           # 报警系统实现
│   ├── model/       # 报警数据模型
│   ├── entity/      # MySQL实体类
│   ├── repository/  # 数据访问层
│   ├── service/     # 报警业务逻辑
│   ├── controller/  # 报警REST接口
│   └── util/        # 工具类
├── storage/         # 数据存储接口及实现
└── controller/      # REST控制器
```

### MQTT实现说明

项目当前使用Moquette作为MQTT服务器实现，具有以下特点：

1. **功能丰富**: 支持完整的MQTT 3.1.1协议
2. **高性能**: 基于Netty实现，支持高并发连接
3. **易于扩展**: 提供拦截器机制，方便扩展功能
4. **持久化**: 支持会话和消息持久化

### MQTT客户端功能

项目还实现了MQTT客户端功能，用于连接外部MQTT服务器：

1. **自动重连**: 支持断线重连机制
2. **主题订阅**: 支持订阅多个主题
3. **消息发布**: 支持通过REST API发布消息
4. **数据存储**: 接收到的消息会自动存储到配置的数据存储中