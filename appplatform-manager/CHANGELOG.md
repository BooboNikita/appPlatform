# 变更日志

## 2025-12-01 埋点数据管理 - API 配置更新

### 📌 更新内容

#### 1. API 端点配置

- **修改文件**: `src/api/tracking.ts`
- **变更**:
  - 前缀从 `/api-tracking` 改为 `/api-events`
  - 端点从 `/list` 改为 `/recent`
  - 完整 URL: `GET /api-events/recent`

```diff
- const prefix = "/api-tracking";
+ const prefix = "/api-events";

- export const getTrackingList = (params: PageParams) => {
-   return request.get<PageResult<TrackingEvent>>(`${prefix}/list`, { params });
+ export const getTrackingList = (params: PageParams) => {
+   return request.get<ApiResponse<PageResult<TrackingEvent>>>(`${prefix}/recent`, { params });
```

#### 2. WebSocket 连接地址

- **修改文件**: `src/api/tracking.ts`
- **变更**:
  - WebSocket 路径从 `/api-tracking/ws` 改为 `/topic/events`
  - 对应后端 `@SendTo("/topic/events")` 注解

```diff
- const url = `${wsProtocol}//${location.host}${baseUrl}/api-tracking/ws`;
+ const url = `${wsProtocol}//${location.host}${baseUrl}/topic/events`;
```

#### 3. API 响应处理

- **修改文件**: `src/views/app/Tracking.vue`
- **变更**: 处理新的 `ApiResponse` 包装结构

```diff
- const response = await getTrackingList(queryParams);
- if (response.data && response.data.data) {
-   const pageResult = response.data.data;
-   trackingList.value = pageResult.list;
-   total.value = pageResult.total;
+ const response = await getTrackingList(queryParams);
+ if (response.data && response.data.data) {
+   const pageResult = response.data.data;
+   trackingList.value = pageResult.list;
+   total.value = pageResult.total;
+ } else {
+   ElMessage.error("获取埋点数据失败：数据格式错误");
+ }
```

#### 4. WebSocket 消息处理增强

- **修改文件**: `src/views/app/Tracking.vue`
- **变更**: 支持多种消息格式

```diff
- ws.onmessage = (event) => {
-   try {
-     const message = JSON.parse(event.data);
-     if (message.type === "tracking_event" && message.data) {
-       const newEvent = message.data as TrackingEvent;
-       trackingList.value.unshift(newEvent);
-       ...
-     }
-   } catch (error) {
-     console.error("处理 WebSocket 消息失败:", error);
-   }
- };

+ ws.onmessage = (event) => {
+   try {
+     const message = JSON.parse(event.data);
+     let newEvent: TrackingEvent | null = null;
+
+     // 支持三种格式
+     if (message.data && typeof message.data === 'object') {
+       newEvent = message.data as TrackingEvent;
+     } else if (message.type === "tracking_event" && message.data) {
+       newEvent = message.data as TrackingEvent;
+     } else if (typeof message === 'object' && 'id' in message && 'userId' in message) {
+       newEvent = message as TrackingEvent;
+     }
+
+     if (newEvent && newEvent.id) {
+       trackingList.value.unshift(newEvent);
+       if (trackingList.value.length > queryParams.pageSize) {
+         trackingList.value.pop();
+       }
+       total.value += 1;
+     }
+   } catch (error) {
+     console.error("处理 WebSocket 消息失败:", error);
+   }
+ };
```

### 📝 API 响应结构变化

#### 初始化请求响应

```json
{
  "data": {
    "list": [...],
    "total": 2,
    "pageNum": 1,
    "pageSize": 10,
    "pages": 1
  },
  "success": true,
  "status": 200,
  "message": "操作成功"
}
```

#### WebSocket 消息格式

支持以下三种格式：

1. 直接的 TrackingEvent 对象
2. `{ data: TrackingEvent }`
3. `{ type: "tracking_event", data: TrackingEvent }`

### 🔄 功能验证清单

- [x] API 端点配置正确
- [x] WebSocket 连接地址正确
- [x] 响应数据结构处理正确
- [x] 消息格式兼容性强
- [x] 错误处理完善
- [x] 用户提示清晰

### 📦 相关文件

- `src/api/tracking.ts` - API 接口定义
- `src/views/app/Tracking.vue` - 埋点管理页面
- `src/types/tracking.ts` - 类型定义
- `src/router/index.ts` - 路由配置

### 🎯 影响范围

- 埋点数据管理页面 (`/app/tracking`)
- 实时数据推送功能
- 查询过滤功能

### ✅ 向后兼容性

- 该更新不影响其他页面
- 新的消息格式处理兼容旧格式
- API 响应结构变化仅影响埋点模块

### 📖 参考文档

- `EVENTS_API_UPDATE.md` - 详细的 API 更新说明
- `EVENTS_QUICK_START.md` - 快速参考指南
