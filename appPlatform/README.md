# AppPlatform 后端工程

本项目是 AppPlatform 的后端核心服务，基于 Spring Boot 3 框架构建，主要负责应用管理、日志分析、事件分发以及文件存储等业务逻辑。

## 核心技术选型

- **核心框架**: [Spring Boot 3.5.7](https://spring.io/projects/spring-boot)
- **安全认证**: Spring Security + JWT (JSON Web Token)
- **数据库**: MySQL 8.0
- **ORM 框架**: MyBatis + PageHelper (分页插件)
- **文件存储**: MinIO (高性能分布式对象存储)
- **消息中间件**: RabbitMQ (处理异步事件)
- **实时通信**: WebSocket (Native 处理器实现)
- **监控与审计**: Spring Boot Actuator + Spring Boot Admin
- **文档工具**: SpringDoc OpenAPI (Swagger UI)

## 目录结构说明

```text
src/main/java/com/app/appplatform/
├── common/         # 公共返回对象 (Result, PageResult)
├── config/         # 系统配置类 (Security, MinIO, RabbitMQ, WebSocket等)
├── controller/     # RESTful API 接口
├── dto/            # 数据传输对象
├── entity/         # 数据库映射实体类
├── enums/          # 系统枚举定义
├── filter/         # 安全过滤器 (JWT Filter)
├── mapper/         # MyBatis Mapper 接口
├── model/          # 业务模型/请求参数模型
├── service/        # 业务逻辑接口及实现
├── util/           # 工具类 (JWT, JSON, File等)
└── websocket/      # WebSocket 处理器实现
```

## 关键模块介绍

### 1. 安全认证 (`/auth/**`)
- 采用 JWT 无状态认证机制。
- 登录接口: `POST /auth/login`。
- 注册接口: `POST /auth/register`。
- 密码使用 `BCryptPasswordEncoder` 进行加密存储。

### 2. 应用管理 (`/api-app/**`)
- 支持 APK 文件的上传、下载及版本控制。
- 集成 MinIO 存储，支持断点续传或流式下载。

### 3. 日志与事件 (`/api-logs/**`, `/api-event/**`)
- **日志**: 接收移动端上传的日志文件，支持分类存储。
- **事件**: 关键业务事件通过 RabbitMQ 进行异步解耦处理。
- **实时**: 通过 WebSocket 向管理后台推送实时事件。

### 4. 文件存储 (MinIO)
- 配置了多个 Bucket: `configfile` (配置), `applogs` (日志), `apps` (安装包)。

## 开发环境配置

### 配置文件
主要配置文件位于 `src/main/resources/`：
- `application.properties`: 基础配置及公共参数。
- `application-dev.properties`: 开发环境专用配置（数据库、RabbitMQ 地址等）。
- `application-prod.properties`: 生产环境配置。

### 数据库初始化
脚本路径: `src/main/resources/sql/`
1. `userInfo.sql`: 用户表。
2. `appInfo.sql`: 应用信息。
3. `appModule.sql`: 应用模块配置。
4. `logInfo.sql`: 日志记录。
5. `eventlog.sql`: 事件追踪。
6. `sysConfig.sql`: 系统配置。

## 常用命令

- **本地运行**:
  ```bash
  ./mvnw spring-boot:run
  ```
- **编译打包**:
  ```bash
  ./mvnw clean package -Dmaven.test.skip=true
  ```
- **运行测试**:
  ```bash
  ./mvnw test
  ```

## 接口文档
项目运行后，可通过以下地址访问 Swagger 文档：
`http://localhost:8080/swagger-ui/index.html`
