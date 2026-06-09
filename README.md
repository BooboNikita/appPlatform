# AppPlatform 项目

AppPlatform 是一个集成了应用管理、日志收集和事件跟踪的综合性平台。项目采用前后端分离架构，包含 Spring Boot 后端和 Vue 3 前端。

## 项目结构

```text
appPlatform/
├── appPlatform/            # 后端工程 (Spring Boot)
└── appplatform-manager/    # 前端管理后台 (Vue 3 + Vite + TS)
```

## 技术栈

### 后端 (appPlatform)

- **框架**: Spring Boot 3.5.7
- **JDK**: Java 17
- **持久层**: MyBatis + MySQL 8.0
- **安全**: Spring Security + JWT
- **消息队列**: RabbitMQ (用于事件处理)
- **存储**: MinIO (用于文件及日志存储)
- **实时通信**: WebSocket
- **监控**: Spring Boot Admin + Actuator
- **API 文档**: SpringDoc OpenAPI (Swagger)

### 前端 (appplatform-manager)

- **框架**: Vue 3 (Composition API)
- **构建工具**: Vite
- **语言**: TypeScript
- **UI 组件库**: Element Plus
- **状态管理**: Pinia
- **路由**: Vue Router
- **工具库**: Axios, Moment.js, QRCode

---

## 快速开始

### 1. 环境准备

- JDK 17+
- MySQL 8.0+
- RabbitMQ
- MinIO
- Node.js 18+ & npm/pnpm

### 2. 后端启动 (appPlatform)

1. **数据库初始化**:
   - 创建数据库 `app_platform`。
   - 依次执行 `appPlatform/src/main/resources/sql/` 目录下的 SQL 脚本。
2. **修改配置**:
   - 检查 `appPlatform/src/main/resources/application.properties` 中的数据库、RabbitMQ 和 MinIO 配置。
3. **运行**:
   - 使用 IDE (IntelliJ IDEA/Eclipse) 运行 `AppPlatformApplication`。
   - 或者使用命令行:
     ```bash
     cd appPlatform
     ./mvnw spring-boot:run
     ```
   - 默认后端端口: `8080`
   - Swagger 文档: `http://localhost:8080/swagger-ui/index.html`

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
   - 前端已配置代理，所有 `/api` 请求将转发至 `http://localhost:8080`。

---

## 主要功能

- **应用管理**: 应用信息的增删改查、APK 文件上传与下载。
- **模块管理**: 针对不同应用模块的精细化配置。
- **日志系统**: 收集并展示移动端上传的运行日志。
- **事件跟踪**: 实时监控应用内的关键事件。
- **系统配置**: 灵活配置平台运行参数。

## 开发注意事项

- 后端默认激活 `dev` profile。
- 文件上传默认存储在 `/app/uploads` (可在 properties 中修改) 或 MinIO (取决于配置)。
- 前端使用了 `base: "/appPlatform/"`，请确保访问路径正确。
