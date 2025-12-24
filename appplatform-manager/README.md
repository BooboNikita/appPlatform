# AppPlatform Manager

AppPlatform Manager 是应用管理平台的后台前端工程，基于 Vue 3、TypeScript 和 Vite 构建。它为管理员提供了应用发布、日志监控、埋点分析及模块配置的可视化操作界面。

## 技术栈说明

- **前端框架**: [Vue 3](https://vuejs.org/) (Composition API)
- **构建工具**: [Vite 7](https://vite.dev/)
- **语言**: [TypeScript](https://www.typescriptlang.org/)
- **UI 组件库**: [Element Plus](https://element-plus.org/)
- **状态管理**: [Pinia](https://pinia.vuejs.org/)
- **路由管理**: [Vue Router 4](https://router.vuejs.org/)
- **网络请求**: [Axios](https://axios-http.com/)
- **样式**: Sass (SCSS)
- **其他工具**: Moment.js (时间处理), QRCode (二维码生成)

## 目录结构

```text
src/
├── api/            # API 接口请求定义 (按模块划分)
├── assets/         # 静态资源 (图片、SVG)
├── components/     # 公共组件
├── router/         # 路由配置及守卫
├── stores/         # Pinia 状态管理 (用户信息、Token)
├── styles/         # 全局样式及变量
├── types/          # TypeScript 类型定义
├── utils/          # 工具函数 (请求封装、埋点处理等)
└── views/          # 页面组件
    ├── app/        # 应用管理 (列表、上传、下载、埋点统计)
    ├── layout/     # 页面整体布局
    ├── login/      # 登录页面
    ├── logs/       # 日志管理 (列表、详情)
    └── modules/    # 模块配置管理
```

## 核心功能模块

### 1. 应用管理

- **应用列表**: 展示所有已上传的 APP，支持按名称、包名筛选。
- **上传应用**: 支持 APK 文件上传，自动解析或手动输入应用元数据。
- **埋点统计**: 可视化展示应用的活跃度及关键事件触发情况。

### 2. 日志监控

- **日志列表**: 汇总移动端上报的日志。
- **详情查看**: 支持查看详细的日志文本、HTML 或下载日志原始文件。

### 3. 模块配置

- 针对应用内不同业务模块的动态参数配置与编辑。

### 4. 辅助工具

- **二维码登录**: 生成登录二维码，方便移动端快捷接入。
- **外链集成**: 快速访问 Jenkins 构建平台等外部工具。

## 开发与部署

### 环境配置

配置文件位于根目录：

- `.env.development`: 开发环境配置 (API 基础路径、标题等)。
- `.env.production`: 生产环境配置。

### 常用命令

```bash
# 安装依赖
npm install

# 启动开发服务器 (默认端口 5174)
npm run dev

# 构建生产版本
npm run build

# 预览构建产物
npm run preview
```

### 网络代理

开发环境下，Vite 配置了 `proxy` 代理，将 `/api` 开头的请求转发至后端：

- 目标地址: `http://localhost:8080` (可在 `vite.config.ts` 修改)
- 访问路径: `http://localhost:5174/appPlatform/`

## 注意事项

- 项目使用了 `base: "/appPlatform/"`，部署到二级目录时请注意资源路径。
- 所有 API 请求均会自动携带 `Authorization: Bearer <token>` 请求头。
