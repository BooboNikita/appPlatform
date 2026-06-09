# AppPlatform 项目

AppPlatform 是一个集成了应用管理、日志收集和事件跟踪的综合性平台。项目采用前后端分离 + 微服务架构，后端基于 Spring Cloud，前端使用 Vue 3。

## 项目结构

```text
appPlatform/
├── appPlatform/                    # 单体后端 (旧版，已迁移至微服务)
├── appPlatform-microservices/      # 微服务后端 (Spring Cloud)
│   ├── app-platform-gateway/       # API 网关
│   ├── app-platform-common/        # 公共模块
│   ├── auth-service/               # 认证服务
│   ├── app-service/                # 应用管理服务
│   ├── log-service/                # 日志服务
│   ├── event-service/              # 事件追踪服务
│   ├── crash-service/              # 崩溃报告服务
│   ├── file-service/               # 文件服务
│   ├── store-service/              # 商店链接服务
│   ├── perf-service/               # 绩效评估服务
│   ├── infrastructure/             # 基础设施 (Docker Compose)
│   ├── docker-compose.app.yml      # 应用服务编排
│   └── start.sh                    # 快速启动脚本
└── appplatform-manager/            # 前端管理后台 (Vue 3 + Vite + TS)
```

## 技术栈

### 微服务后端 (appPlatform-microservices)

- **框架**: Spring Boot 3.5.7 + Spring Cloud 2023.0.0 + Spring Cloud Alibaba
- **JDK**: Java 17
- **服务治理**: Nacos (注册中心 + 配置中心)
- **网关**: Spring Cloud Gateway
- **熔断限流**: Sentinel
- **持久层**: MyBatis + PageHelper + MySQL 8.0
- **安全**: Spring Security + JWT
- **缓存**: Redis
- **消息队列**: RabbitMQ (用于事件处理)
- **对象存储**: MinIO (用于文件及日志存储)
- **实时通信**: WebSocket
- **监控**: Prometheus + Grafana

### 前端 (appplatform-manager)

- **框架**: Vue 3 (Composition API)
- **构建工具**: Vite
- **语言**: TypeScript
- **UI 组件库**: Element Plus
- **状态管理**: Pinia
- **路由**: Vue Router
- **工具库**: Axios, Moment.js, QRCode

---

## 服务清单

| 服务名称 | 端口 | 数据库 | 功能描述 | 网关路由 |
|---------|------|--------|----------|----------|
| app-platform-gateway | 8080 | - | API 网关，统一入口 | - |
| auth-service | 8001 | auth_db | 用户认证、JWT 签发 | /auth/** |
| app-service | 8002 | app_db | 应用管理、模块管理、动态配置 | /api-app/**, /api-modules/**, /api-dynamic-config/** |
| log-service | 8003 | log_db | 日志上传、查询、请求管理 | /api-logs/** |
| event-service | 8004 | event_db | 埋点数据收集、实时推送 | /api-events/** |
| crash-service | 8005 | crash_db | Flutter Crash SDK 数据接收 | /api/crash/** |
| file-service | 8007 | MinIO | 文件上传、下载、管理 | /api-files/** |
| store-service | 8008 | store_db | 应用商店链接配置 | /store-link-config/** |
| perf-service | 8009 | perf_db | 部门绩效评估配置 | /api-performance-review/** |

---

## 快速开始

### 1. 环境准备

- JDK 17+
- MySQL 8.0+
- Redis
- RabbitMQ
- MinIO
- Docker & Docker Compose
- Node.js 18+ & npm/pnpm

### 2. 后端启动 (微服务)

#### 方式一：快速启动脚本 (推荐)

```bash
cd appPlatform-microservices

# 启动所有服务 (基础设施 + 数据库初始化 + 应用服务)
./start.sh all

# 或分步执行:
./start.sh infra       # 启动基础设施 (Nacos, Prometheus)
./start.sh init-db     # 初始化数据库
./start.sh app         # 启动应用服务
./start.sh status      # 查看服务状态
./start.sh stop        # 停止所有服务
```

启动成功后访问：
- API 网关: `http://localhost:8080`
- Nacos 控制台: `http://localhost:8848/nacos`
- Prometheus: `http://localhost:9090`

#### 方式二：IDE 本地启动

1. **启动基础设施**:
   ```bash
   cd appPlatform-microservices
   ./start.sh infra
   ```

2. **数据库初始化**:
   - 在 MySQL 中创建各服务数据库 (`auth_db`, `app_db`, `log_db`, `event_db`, `crash_db`, `store_db`, `perf_db`)
   - 依次执行 `infrastructure/init-scripts/` 目录下的 SQL 脚本

3. **启动各微服务**:
   - 在 IDE 中依次运行各服务的 `*Application` 主类
   - 确保各服务的 `application.yml` 中数据库、Nacos 配置正确

### 3. 前端启动 (appplatform-manager)

1. **安装依赖**:
   ```bash
   cd appplatform-manager
   npm install
   ```

2. **运行**:
   ```bash
   npm run dev
   ```

3. **访问**:
   - 默认前端端口: `5174`
   - 访问地址: `http://localhost:5174/appPlatform/`
   - 前端已配置代理，所有请求将转发至 API 网关 `http://localhost:8080`。

---

## 单体 vs 微服务

| | 单体 (appPlatform/) | 微服务 (appPlatform-microservices/) |
|---|---|---|
| 架构 | 单体 Spring Boot | Spring Cloud 微服务 |
| 入口端口 | 8080 | 8080 (Gateway) |
| 部署方式 | 单个 Jar | Docker Compose 编排 |
| 数据库 | 单库 `app_platform` | 每服务独立数据库 |
| 服务治理 | 无 | Nacos 注册/配置中心 |
| 当前状态 | **旧版，保留参考** | **推荐使用** |

---

## 主要功能

- **应用管理**: 应用信息的增删改查、APK 文件上传与下载、动态配置下发。
- **模块管理**: 针对不同应用模块的精细化配置。
- **日志系统**: 收集并展示移动端上传的运行日志。
- **事件跟踪**: 实时监控应用内的关键事件，支持 WebSocket 实时推送。
- **崩溃报告**: 接收并分析 Flutter Crash SDK 上报的崩溃数据。
- **文件管理**: 文件上传、下载、MinIO 对象存储集成。
- **商店链接**: 配置应用在各应用商店的下发链接。
- **绩效评估**: 部门绩效考核指标配置与管理。
- **系统配置**: 灵活配置平台运行参数。

## 开发注意事项

- 微服务默认通过 Nacos 进行配置管理，配置优先级: Nacos > application.yml
- 前端使用了 `base: "/appPlatform/"`，请确保访问路径正确
- 前端代理指向 API 网关 `http://localhost:8080`，由 Gateway 路由到具体微服务
- 文件上传使用 MinIO，配置了多个 Bucket: `configfile` (配置), `applogs` (日志), `apps` (安装包)
