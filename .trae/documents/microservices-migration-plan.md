# AppPlatform 微服务拆分方案

## 1. 项目现状分析

### 1.1 当前架构
AppPlatform 是一个基于 Spring Boot 3.5.7 的**单体应用**，采用前后端分离架构：

- **后端**: Spring Boot 单体应用 (端口 8080)
- **前端**: Vue 3 + Vite 管理后台 (端口 5174)
- **数据库**: MySQL 8.0 (单库)
- **中间件**: RabbitMQ、Redis、MinIO

### 1.2 业务模块梳理

| 模块 | 功能描述 | 数据库表 | API 前缀 |
|------|----------|----------|----------|
| **用户认证** | JWT 登录/注册/权限 | user | `/auth/**` |
| **应用管理** | APK 上传/下载/版本控制 | app_info | `/api-app/**` |
| **模块管理** | 应用模块配置 | app_module | `/api-modules/**` |
| **日志系统** | 日志上传/查询/请求 | log_info, log_request | `/api-logs/**` |
| **事件追踪** | 埋点数据收集/分析 | app_event | `/api-events/**` |
| **崩溃报告** | Flutter Crash SDK 数据 | crash_reports | `/api/crash/**` |
| **动态配置** | 客户端动态配置管理 | dynamic_config, dynamic_config_history | `/api-dynamic-config/**` |
| **绩效评估** | 部门封面/截止时间管理 | performance_review | `/api-performance-review/**` |
| **商店链接** | 应用商店链接配置 | store_link_config | `/store-link-config/**` |
| **文件存储** | MinIO 文件操作 | - | `/api-files/**` |

### 1.3 技术依赖分析

```
共享组件:
├── 数据库连接 (MySQL)
├── Redis 缓存
├── RabbitMQ (事件队列)
├── MinIO (文件存储)
├── JWT 认证
├── WebSocket
└── Spring Security
```

---

## 2. 新增数据库列表

### 2.1 数据库拆分策略

采用**数据库 per 服务**模式，共需新建 **8 个数据库**：

```
┌──────────────────────────────────────────────────────────────┐
│                    MySQL 实例                                 │
│  ┌─────────────┬─────────────┬─────────────┬─────────────┐  │
│  │ auth_db     │ app_db      │ log_db      │ event_db    │  │
│  │   (UTF8)    │   (UTF8)    │   (UTF8)    │   (UTF8)    │  │
│  │             │             │             │             │  │
│  │ • user      │ • app_info  │ • log_info  │ • app_event │  │
│  │             │ • app_module│ • log_request│            │  │
│  │             │ • dynamic_config         │             │  │
│  │             │ • dynamic_config_history │             │  │
│  │             │ • sys_config│             │             │  │
│  └─────────────┴─────────────┴─────────────┴─────────────┘  │
│  ┌─────────────┬─────────────┬─────────────┬─────────────┐  │
│  │ crash_db    │ store_db    │ perf_db     │             │  │
│  │   (UTF8)    │   (UTF8)    │   (UTF8)    │             │  │
│  │             │             │             │             │  │
│  │ • crash_reports            │ • performance_review     │  │
│  │             │ • store_link_config      │             │  │
│  └─────────────┴─────────────┴─────────────┴─────────────┘  │
└──────────────────────────────────────────────────────────────┘
```

### 2.2 数据库详细清单

| 序号 | 数据库名称 | 所属服务 | 用途说明 | 字符集 | 数据表 |
|------|-----------|----------|----------|--------|--------|
| 1 | `auth_db` | auth-service | 用户认证数据 | utf8mb4 | user |
| 2 | `app_db` | app-service | 应用管理数据 | utf8mb4 | app_info, app_module, dynamic_config, dynamic_config_history, sys_config |
| 3 | `log_db` | log-service | 日志系统数据 | utf8mb4 | log_info, log_request |
| 4 | `event_db` | event-service | 事件追踪数据 | utf8mb4 | app_event |
| 5 | `crash_db` | crash-service | 崩溃报告数据 | utf8mb4 | crash_reports |
| 6 | `store_db` | store-service | 商店链接配置 | utf8mb4 | store_link_config |
| 7 | `perf_db` | perf-service | 绩效评估数据 | utf8mb4 | performance_review |

**说明**:
- 原单体应用的 `app_platform` 数据库将被拆分到上述 7 个数据库
- 每个数据库独立管理，避免服务间直接访问对方数据库
- 服务间数据交互通过 API 或消息队列完成

### 2.3 数据库初始化脚本清单

```
appPlatform-microservices/
├── database/
│   ├── init/
│   │   ├── 01_create_databases.sql      # 创建所有数据库
│   │   ├── 02_auth_db_schema.sql        # auth_db 表结构
│   │   ├── 03_app_db_schema.sql         # app_db 表结构
│   │   ├── 04_log_db_schema.sql         # log_db 表结构
│   │   ├── 05_event_db_schema.sql       # event_db 表结构
│   │   ├── 06_crash_db_schema.sql       # crash_db 表结构
│   │   ├── 07_store_db_schema.sql       # store_db 表结构
│   │   ├── 08_perf_db_schema.sql        # perf_db 表结构
│   │   └── 09_migration_data.sql        # 数据迁移脚本
│   └── docker/
│       └── docker-compose.db.yml        # 数据库容器编排
```

---

## 3. 新增技术栈列表

### 3.1 微服务基础设施

| 类别 | 技术组件 | 版本 | 用途说明 |
|------|----------|------|----------|
| **服务框架** | Spring Cloud Alibaba | 2023.0.0.0 | 微服务全家桶 |
| **注册中心** | Nacos | 2.3.0 | 服务注册发现与配置管理 |
| **API 网关** | Spring Cloud Gateway | 4.1.x | 统一入口、路由转发 |
| **负载均衡** | Spring Cloud LoadBalancer | 4.1.x | 客户端负载均衡 |
| **服务调用** | OpenFeign | 4.1.x | 声明式 HTTP 客户端 |
| **熔断限流** | Sentinel | 1.8.8 | 流量控制、熔断降级 |

### 3.2 数据与缓存

| 类别 | 技术组件 | 版本 | 用途说明 |
|------|----------|------|----------|
| **分布式缓存** | Redisson | 3.25.0 | 分布式锁、缓存 |
| **分布式事务** | Seata | 1.8.0 | 分布式事务管理 (可选) |

### 3.3 监控与运维

| 类别 | 技术组件 | 版本 | 用途说明 |
|------|----------|------|----------|
| **指标采集** | Micrometer | 1.12.x | 应用指标采集 |
| **监控系统** | Prometheus | 2.50.0 | 时序数据库、告警 |
| **可视化** | Grafana | 10.3.0 | 监控仪表盘 |
| **链路追踪** | SkyWalking | 9.7.0 | 分布式链路追踪 |
| **日志收集** | ELK Stack | 8.12.x | Elasticsearch + Logstash + Kibana |

### 3.4 容器化与部署

| 类别 | 技术组件 | 版本 | 用途说明 |
|------|----------|------|----------|
| **容器化** | Docker | 25.0.x | 应用容器化 |
| **编排工具** | Docker Compose | 2.24.x | 本地开发环境编排 |
| **容器编排** | Kubernetes | 1.29.x | 生产环境容器编排 (可选) |

### 3.5 开发工具

| 类别 | 技术组件 | 版本 | 用途说明 |
|------|----------|------|----------|
| **API 文档** | SpringDoc OpenAPI | 2.3.0 | API 文档生成 (已有) |
| **代码生成** | MyBatis Plus | 3.5.5 | 代码生成器 (可选) |
| **测试工具** | Testcontainers | 1.19.x | 集成测试容器支持 |

### 3.6 完整技术栈架构图

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                              前端层 (Frontend)                               │
│                         Vue 3 + Vite + TypeScript                            │
└─────────────────────────────────────────────────────────────────────────────┘
                                      │
                                      ▼
┌─────────────────────────────────────────────────────────────────────────────┐
│                            API 网关层 (Gateway)                              │
│                    Spring Cloud Gateway + Sentinel                           │
│  ┌─────────────┬─────────────┬─────────────┬─────────────┬─────────────┐   │
│  │  路由转发    │  认证鉴权    │  限流熔断    │  日志记录    │  跨域处理    │   │
│  └─────────────┴─────────────┴─────────────┴─────────────┴─────────────┘   │
└─────────────────────────────────────────────────────────────────────────────┘
                                      │
                                      ▼
┌─────────────────────────────────────────────────────────────────────────────┐
│                          服务注册中心 (Nacos)                                │
│              服务注册发现 + 配置中心 + 服务健康检查                             │
└─────────────────────────────────────────────────────────────────────────────┘
                                      │
        ┌─────────────────────────────┼─────────────────────────────┐
        │                             │                             │
        ▼                             ▼                             ▼
┌───────────────┐           ┌───────────────────┐           ┌───────────────┐
│  业务服务层    │           │    基础设施层      │           │   监控运维层   │
│               │           │                   │           │               │
│ auth-service  │◄─────────►│   Redis Cluster   │           │  Prometheus   │
│ app-service   │◄─────────►│   RabbitMQ        │           │  Grafana      │
│ log-service   │◄─────────►│   MinIO           │           │  SkyWalking   │
│ event-service │◄─────────►│   MySQL           │           │  ELK Stack    │
│ crash-service │           │                   │           │               │
│ file-service  │           │                   │           │               │
│ store-service │           │                   │           │               │
│ perf-service  │           │                   │           │               │
└───────────────┘           └───────────────────┘           └───────────────┘
```

---

## 4. 微服务拆分策略

### 4.1 拆分原则

1. **业务边界清晰**: 按业务领域划分服务
2. **数据独立性**: 每个服务拥有独立的数据库
3. **松耦合**: 服务间通过 API 或消息队列通信
4. **可独立部署**: 各服务可独立开发、测试、部署

### 4.2 拆分方案

#### 方案 A: 领域驱动拆分 (推荐)

```
┌─────────────────────────────────────────────────────────────────┐
│                        API Gateway                               │
│              (Spring Cloud Gateway / Nginx)                      │
└─────────────────────────────────────────────────────────────────┘
                                │
        ┌───────────────────────┼───────────────────────┐
        │                       │                       │
        ▼                       ▼                       ▼
┌───────────────┐    ┌───────────────────┐    ┌───────────────┐
│  认证服务      │    │    应用管理服务    │    │   日志服务     │
│  auth-service │    │   app-service     │    │ log-service   │
│   (Port:8001) │    │    (Port:8002)    │    │  (Port:8003)  │
└───────────────┘    └───────────────────┘    └───────────────┘
        │                       │                       │
        ▼                       ▼                       ▼
┌───────────────┐    ┌───────────────────┐    ┌───────────────┐
│  事件追踪服务  │    │   崩溃报告服务     │    │   配置服务     │
│ event-service │    │  crash-service    │    │config-service │
│   (Port:8004) │    │    (Port:8005)    │    │  (Port:8006)  │
└───────────────┘    └───────────────────┘    └───────────────┘
        │                       │                       │
        ▼                       ▼                       ▼
┌───────────────┐    ┌───────────────────┐    ┌───────────────┐
│  文件存储服务  │    │   商店链接服务     │    │   绩效服务     │
│  file-service │    │  store-service    │    │   perf-service│
│   (Port:8007) │    │    (Port:8008)    │    │  (Port:8009)  │
└───────────────┘    └───────────────────┘    └───────────────┘
```

#### 方案 B: 聚合拆分 (简化版)

适合初期过渡，将相关性高的模块合并：

```
┌─────────────────────────────────────────────────────────────────┐
│                        API Gateway                               │
└─────────────────────────────────────────────────────────────────┘
                                │
        ┌───────────────────────┼───────────────────────┐
        │                       │                       │
        ▼                       ▼                       ▼
┌───────────────┐    ┌───────────────────┐    ┌───────────────┐
│  认证服务      │    │   核心应用服务     │    │   数据服务     │
│  auth-service │    │   core-service    │    │ data-service  │
│               │    │  (应用+模块+配置)  │    │(日志+事件+崩溃)│
└───────────────┘    └───────────────────┘    └───────────────┘
        │                       │                       │
        ▼                       ▼                       ▼
┌───────────────┐    ┌───────────────────┐
│  文件服务      │    │   业务扩展服务     │
│  file-service │    │  ext-service      │
│               │    │(商店链接+绩效评估) │
└───────────────┘    └───────────────────┘
```

---

## 5. 详细拆分设计

### 5.1 认证服务 (auth-service)

**职责**: 用户认证、JWT 签发、权限管理

**数据库表**:
- `user` - 用户表

**API 接口**:
```
POST   /auth/login
POST   /auth/register
GET    /auth/userinfo
```

**依赖**:
- MySQL (auth_db)
- Redis (Token 缓存)

**代码迁移**:
- `JwtAuthenticationController`
- `JwtAuthenticationService`
- `JwtUserDetailsService`
- `JwtTokenUtil`
- `SecurityConfig` (部分)

---

### 5.2 应用管理服务 (app-service)

**职责**: APK 上传/下载、版本管理、模块配置、动态配置

**数据库表**:
- `app_info` - 应用信息
- `app_module` - 应用模块
- `dynamic_config` - 动态配置
- `dynamic_config_history` - 配置历史
- `sys_config` - 系统配置

**API 接口**:
```
# 应用管理
POST   /api-app/upload
GET    /api-app/apps
GET    /api-app/app/{id}
GET    /api-app/download/{id}
PUT    /api-app/update/{id}
DELETE /api-app/delete/{id}
POST   /api-app/check-version

# 模块管理
GET    /api-modules/allActive
GET    /api-modules/all
POST   /api-modules/create
PUT    /api-modules/update/{id}
DELETE /api-modules/{id}

# 动态配置
POST   /api-dynamic-config/upload
GET    /api-dynamic-config/list
GET    /api-dynamic-config/match
PUT    /api-dynamic-config/{id}
DELETE /api-dynamic-config/{id}
```

**代码迁移**:
- `AppInfoController`, `AppInfoService`, `AppInfoMapper`
- `AppModuleController`, `AppModuleService`, `AppModuleMapper`
- `DynamicConfigController`, `DynamicConfigService`, `DynamicConfigMapper`
- `ConfigService`, `SysConfigMapper`

---

### 5.3 日志服务 (log-service)

**职责**: 日志上传、日志查询、日志请求管理

**数据库表**:
- `log_info` - 日志记录
- `log_request` - 日志请求

**API 接口**:
```
POST   /api-logs/upload
GET    /api-logs/list
GET    /api-logs/{id}
POST   /api-logs/file
DELETE /api-logs/{id}
POST   /api-logs/request
GET    /api-logs/request/check
GET    /api-logs/request/list
DELETE /api-logs/request/{id}
```

**代码迁移**:
- `LogController`, `LogService`, `LogMapper`
- `LogRequestService`, `LogRequestMapper`

---

### 5.4 事件追踪服务 (event-service)

**职责**: 埋点数据收集、实时推送、数据分析

**数据库表**:
- `app_event` - 事件日志

**API 接口**:
```
POST   /api-events/submit
POST   /api-events/batch
GET    /api-events/recent
GET    /api-events/tracking/status
POST   /api-events/tracking/set-status
```

**消息队列**:
- 消费 `app.event.queue` 队列

**代码迁移**:
- `AppEventController`, `AppEventService`, `AppEventMapper`
- `EventConsumer`
- `EventWebSocketController`, `NativeWebSocketHandler`

---

### 5.5 崩溃报告服务 (crash-service)

**职责**: Flutter Crash SDK 数据接收、查询、统计

**数据库表**:
- `crash_reports` - 崩溃报告

**API 接口**:
```
POST   /api/crash
GET    /api/crash/list
GET    /api/crash/{id}
DELETE /api/crash/{id}
GET    /api/crash/statistics
```

**代码迁移**:
- `CrashController`, `CrashService`, `CrashReportMapper`
- `DeviceInfoUtil`

---

### 5.6 配置服务 (config-service)

**职责**: 系统配置管理

**数据库表**:
- `sys_config` - 系统配置

**API 接口**:
```
GET    /api-config/{key}
POST   /api-config/{key}
DELETE /api-config/{key}
```

**代码迁移**:
- `ConfigService`, `SysConfigMapper`

---

### 5.7 文件存储服务 (file-service)

**职责**: 文件上传、下载、管理

**数据库表**: 无 (仅操作 MinIO)

**API 接口**:
```
POST   /api-files/upload/logs
POST   /api-files/upload/apps
POST   /api-files/upload/media
GET    /api-files/download/logs/{objectName}
GET    /api-files/download/apps/{objectName}
GET    /api-files/media/{objectName}
DELETE /api-files/delete/logs/{objectName}
DELETE /api-files/delete/apps/{objectName}
```

**代码迁移**:
- `FileController`
- `MinioService`, `MinioConfig`
- `MediaService`
- `FileDownloadUtil`

---

### 5.8 商店链接服务 (store-service)

**职责**: 应用商店链接配置管理

**数据库表**:
- `store_link_config` - 商店链接配置

**API 接口**:
```
GET    /store-link-config/enabled
GET    /store-link-config/brand/{deviceBrand}
GET    /store-link-config
GET    /store-link-config/{id}
POST   /store-link-config
PUT    /store-link-config/{id}
DELETE /store-link-config/{id}
PUT    /store-link-config/{id}/enabled
```

**代码迁移**:
- `StoreLinkConfigController`, `StoreLinkConfigService`, `StoreLinkConfigMapper`

---

### 5.9 绩效评估服务 (perf-service)

**职责**: 部门绩效评估配置管理

**数据库表**:
- `performance_review` - 绩效评估配置

**API 接口**:
```
GET    /api-performance-review/cover/{deptId}
GET    /api-performance-review/deadline/{deptId}
GET    /api-performance-review/name/{deptId}
GET    /api-performance-review/config/{deptId}
POST   /api-performance-review/cover/{deptId}
POST   /api-performance-review/deadline/{deptId}
POST   /api-performance-review/name/{deptId}
PUT    /api-performance-review/config/{deptId}
DELETE /api-performance-review/config/{deptId}
GET    /api-performance-review/list
POST   /api-performance-review/batch
GET    /api-performance-review/exists/{deptId}
```

**代码迁移**:
- `PerformanceReviewController`, `PerformanceReviewService`, `PerformanceReviewMapper`

---

## 6. 服务间通信设计

### 6.1 同步通信 (REST API)

服务间需要实时数据时使用 REST API：

```java
// 示例: 日志服务需要获取应用信息
@FeignClient(name = "app-service", path = "/api-app")
public interface AppServiceClient {
    @GetMapping("/app/{id}")
    AppInfo getAppById(@PathVariable Integer id);
}
```

### 6.2 异步通信 (消息队列)

使用 RabbitMQ 进行异步解耦：

```
┌─────────────────┐     ┌──────────────┐     ┌─────────────────┐
│   事件追踪服务   │────▶│  app.event   │────▶│   数据分析服务   │
│                 │     │   .exchange  │     │                 │
└─────────────────┘     └──────────────┘     └─────────────────┘
                               │
                               ▼
                        ┌──────────────┐
                        │  app.event   │
                        │   .queue     │
                        └──────────────┘
```

---

## 7. 服务注册与发现

### 7.1 方案选择

推荐使用 **Nacos** 作为服务注册中心：

```yaml
# 各服务配置
spring:
  cloud:
    nacos:
      discovery:
        server-addr: localhost:8848
        namespace: app-platform
```

### 7.2 服务配置

```yaml
# bootstrap.yml 示例
spring:
  application:
    name: auth-service
  profiles:
    active: dev
  cloud:
    nacos:
      config:
        server-addr: localhost:8848
        file-extension: yaml
      discovery:
        server-addr: localhost:8848
```

---

## 8. API 网关设计

### 8.1 网关职责

- 路由转发
- 认证鉴权
- 限流熔断
- 日志记录

### 8.2 路由配置

```yaml
spring:
  cloud:
    gateway:
      routes:
        - id: auth-service
          uri: lb://auth-service
          predicates:
            - Path=/auth/**
          
        - id: app-service
          uri: lb://app-service
          predicates:
            - Path=/api-app/**, /api-modules/**, /api-dynamic-config/**
          filters:
            - AuthFilter
          
        - id: log-service
          uri: lb://log-service
          predicates:
            - Path=/api-logs/**
          filters:
            - AuthFilter
        # ... 其他路由
```

---

## 9. 项目结构重组

### 9.1 新目录结构

```
appPlatform-microservices/
├── README.md
├── docker-compose.yml                    # 本地开发环境
├── pom.xml                               # 父 POM
│
├── app-platform-gateway/                 # API 网关
│   ├── pom.xml
│   └── src/
│
├── app-platform-common/                  # 公共模块
│   ├── pom.xml
│   └── src/
│       ├── java/
│       │   ├── common/Result.java
│       │   ├── common/PageResult.java
│       │   ├── exception/
│       │   └── util/
│       └── resources/
│
├── auth-service/                         # 认证服务
│   ├── pom.xml
│   ├── Dockerfile
│   └── src/
│       ├── main/
│       │   ├── java/
│       │   │   └── com/app/auth/
│       │   │       ├── AuthServiceApplication.java
│       │   │       ├── controller/
│       │   │       ├── service/
│       │   │       ├── mapper/
│       │   │       ├── entity/
│       │   │       └── config/
│       │   └── resources/
│       │       ├── application.yml
│       │       └── mapper/
│       └── test/
│
├── app-service/                          # 应用管理服务
│   ├── pom.xml
│   ├── Dockerfile
│   └── src/
│       └── ...
│
├── log-service/                          # 日志服务
│   ├── pom.xml
│   ├── Dockerfile
│   └── src/
│       └── ...
│
├── event-service/                        # 事件追踪服务
│   ├── pom.xml
│   ├── Dockerfile
│   └── src/
│       └── ...
│
├── crash-service/                        # 崩溃报告服务
│   ├── pom.xml
│   ├── Dockerfile
│   └── src/
│       └── ...
│
├── file-service/                         # 文件存储服务
│   ├── pom.xml
│   ├── Dockerfile
│   └── src/
│       └── ...
│
├── store-service/                        # 商店链接服务
│   ├── pom.xml
│   ├── Dockerfile
│   └── src/
│       └── ...
│
├── perf-service/                         # 绩效评估服务
│   ├── pom.xml
│   ├── Dockerfile
│   └── src/
│       └── ...
│
└── appplatform-manager/                  # 前端 (保持不变)
    └── ...
```

### 9.2 父 POM 配置

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project>
    <modelVersion>4.0.0</modelVersion>
    <groupId>com.app</groupId>
    <artifactId>app-platform-parent</artifactId>
    <version>1.0.0-SNAPSHOT</version>
    <packaging>pom</packaging>
    
    <parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>3.5.7</version>
    </parent>
    
    <modules>
        <module>app-platform-common</module>
        <module>app-platform-gateway</module>
        <module>auth-service</module>
        <module>app-service</module>
        <module>log-service</module>
        <module>event-service</module>
        <module>crash-service</module>
        <module>file-service</module>
        <module>store-service</module>
        <module>perf-service</module>
    </modules>
    
    <properties>
        <java.version>17</java.version>
        <spring-cloud.version>2023.0.0</spring-cloud.version>
        <nacos.version>2023.0.0.0</nacos.version>
    </properties>
    
    <dependencyManagement>
        <dependencies>
            <dependency>
                <groupId>org.springframework.cloud</groupId>
                <artifactId>spring-cloud-dependencies</artifactId>
                <version>${spring-cloud.version}</version>
                <type>pom</type>
                <scope>import</scope>
            </dependency>
            <dependency>
                <groupId>com.alibaba.cloud</groupId>
                <artifactId>spring-cloud-alibaba-dependencies</artifactId>
                <version>${nacos.version}</version>
                <type>pom</type>
                <scope>import</scope>
            </dependency>
        </dependencies>
    </dependencyManagement>
</project>
```

---

## 10. 实施路线图

### 阶段 1: 基础设施搭建 (2 周)

- [ ] 搭建 Nacos 服务注册中心
- [ ] 搭建 API Gateway
- [ ] 搭建公共模块 (app-platform-common)
- [ ] 配置 Docker 开发环境
- [ ] 数据库拆分脚本准备

### 阶段 2: 核心服务拆分 (3 周)

- [ ] 认证服务 (auth-service)
- [ ] 应用管理服务 (app-service)
- [ ] 文件存储服务 (file-service)
- [ ] 前端适配新接口

### 阶段 3: 数据服务拆分 (2 周)

- [ ] 日志服务 (log-service)
- [ ] 事件追踪服务 (event-service)
- [ ] 崩溃报告服务 (crash-service)

### 阶段 4: 业务服务拆分 (2 周)

- [ ] 配置服务 (config-service)
- [ ] 商店链接服务 (store-service)
- [ ] 绩效评估服务 (perf-service)

### 阶段 5: 测试与优化 (2 周)

- [ ] 集成测试
- [ ] 性能测试
- [ ] 监控告警配置
- [ ] 文档完善

---

## 11. 风险评估与应对

| 风险 | 影响 | 应对措施 |
|------|------|----------|
| 数据一致性 | 高 | 采用 Saga 模式处理分布式事务 |
| 服务间调用延迟 | 中 | 引入缓存、异步化非关键调用 |
| 运维复杂度增加 | 中 | 完善监控告警、自动化部署 |
| 团队学习成本 | 中 | 提前培训、渐进式拆分 |
| 回滚困难 | 高 | 保留单体应用分支、灰度发布 |

---

## 12. 总结

### 推荐方案: 方案 A (领域驱动拆分)

**优势**:
- 业务边界清晰，易于理解和维护
- 各服务可独立扩展，提高系统弹性
- 技术栈可根据服务需求差异化选型
- 故障隔离，单点故障影响范围小

**实施建议**:
1. 采用**渐进式拆分**，先拆分相对独立的模块
2. 保持**数据库 per 服务**，避免共享数据库
3. 优先拆分**读多写少**的服务，降低风险
4. 建立完善的**监控和日志**体系

**预期收益**:
- 系统可扩展性提升
- 团队并行开发效率提高
- 技术债务逐步偿还
- 支持持续集成/持续部署
