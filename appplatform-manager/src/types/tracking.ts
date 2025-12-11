// 埋点数据类型定义

// 应用信息
export interface AppInfo {
  version: string;
  buildNumber: string;
}

// 设备信息
export interface DeviceInfo {
  deviceId: string;
  model: string;
  brand: string;
  ip: string;
  os: string;
  osVersion: string;
  networkType: string;
  screenResolution: string;
}

// 事件信息
export interface EventInfo {
  eventId: string;
  eventType: string;
  eventTime: number;
  recvTime: number;
}

// 埋点事件数据
export interface TrackingEvent {
  id: number;
  userId: string;
  userName: string;
  sessionId: string;
  pageUrl: string;
  referrer: string;
  status: number;
  app: AppInfo;
  device: DeviceInfo;
  eventInfo: EventInfo;
  extra: string;
}

// 分页结果
export interface PageResult<T> {
  list: T[];
  total: number;
  pageNum: number;
  pageSize: number;
  pages: number;
}

// API 响应
export interface ApiResponse<T> {
  data: T;
  success: boolean;
  status: number;
  message: string;
}

// WebSocket 消息类型
export interface WebSocketMessage {
  type: string;
  data: TrackingEvent;
  timestamp: number;
}

export interface EventStatus {
  eventTrack: boolean;
}
