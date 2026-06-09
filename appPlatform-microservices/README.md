# AppPlatform 微服务架构

## 项目概述

本项目是 AppPlatform 的微服务化改造版本，将原有的单体应用拆分为多个独立的微服务，每个服务负责特定的业务领域。

## 架构图

```
┌─────────────────────────────────────────────────────────────────┐
│                        API Gateway                               │
│                   (Port: 8080)                                   │
│              Spring Cloud Gateway                                │
└─────────────────────────────────────────────────────────────────┘
                                │
        ┌───────────────────────┼───────────────────────┐
        │                       │                       │
        ▼                       ▼                       ▼
┌───────────────┐    ┌───────────────────┐    ┌───────────────┐
│  认证服务      │    │    应用管理服务    │    │   日志服务     │
│  auth-service │    │   app-service     │    │ log-service   │
│   (Port:8001) │    │    (Port:8002)    │    │  (Port:8003)  │
│   auth_db     │    │     app_db        │    │   log_db      │
└───────────────┘    └───────────────────┘    └───────────────┘
        │                       │                       │
        ▼                       ▼                       ▼
┌───────────────┐    ┌───────────────────┐    ┌───────────────┐
│  事件追踪服务  │    │   崩溃报告服务     │    │   文件服务     │
│ event-service │    │  crash-service    │    │ file-service  │
│   (Port:8004) │    │    (Port:8005)    │    │  (Port:8007)  │
│   event_db    │    │    crash_db       │    │    MinIO      │
└───────────────┘    └───────────────────┘    └───────────────┘
        │                       │
        ▼                       ▼
┌───────────────┐    ┌───────────────────┐
│  商店链接服务  │    │   绩效评估服务     │
│ store-service │    │   perf-service    │
│   (Port:8008) │    │    (Port:8009)    │
│   store_db    │    │     perf_db       │
└───────────────┘    └───────────────────┘
```

## 服务清单

| 服务名称 | 端口 | 数据库 | 功能描述 |
|---------|------|--------|----------|
| app-platform-gateway | 8080 | - | API 网关，统一入口 |
| auth-service | 8001 | auth_db | 用户认证、JWT 签发 |
| app-service | 8002 | app_db | 应用管理、模块管理、动态配置 |
| log-service | 8003 | log_db | 日志上传、查询、请求管理 |
| event-service | 8004 | event_db | 埋点数据收集、实时推送 |
| crash-service | 8005 | crash_db | Flutter Crash SDK 数据接收 |
| file-service | 8007 | MinIO | 文件上传、下载、管理 |
| store-service | 8008 | store_db | 应用商店链接配置 |
| perf-service | 8009 | perf_db | 部门绩效评估配置 |

## 技术栈

- **框架**: Spring Boot 3.5.7, Spring Cloud 2023.0.0, Spring Cloud Alibaba 2023.0.0.0
- **服务治理**: Nacos (注册中心 + 配置中心)
- **网关**: Spring Cloud Gateway
- **服务调用**: OpenFeign
- **熔断限流**: Sentinel
- **数据库**: MySQL 8.0
- **缓存**: Redis
- **消息队列**: RabbitMQ
- **对象存储**: MinIO
- **监控**: Prometheus + Grafana

## 快速开始

### 1. 启动基础设施

```bash
cd infrastructure
docker-compose up -d
```

这将启动：
- MySQL (端口: 3306)
- Redis (端口: 6379)
- RabbitMQ (端口: 5672, 管理界面: 15672)
- Nacos (端口: 8848)
- MinIO (端口: 9000, 控制台: