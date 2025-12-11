import request from "@/utils/request";
import {
  PageResult,
  TrackingEvent,
  ApiResponse,
  EventStatus,
} from "@/types/tracking";

// 分页参数
interface PageParams {
  pageNum: number;
  pageSize: number;
  [key: string]: any;
}

const prefix = "/api-events";

/**
 * 获取埋点数据列表
 * @param params 分页参数
 */
export const getAppEventList = (params: PageParams) => {
  return request.get<PageResult<TrackingEvent>>(`${prefix}/recent`, { params });
};

/**
 * 获取埋点数据详情
 * @param id 埋点数据ID
 */
export const getAppEventDetail = (id: number) => {
  return request.get<TrackingEvent>(`${prefix}/${id}`);
};

/**
 * 获取 WebSocket 连接地址
 */
export const getAppEventWebSocketUrl = () => {
  const baseUrl = import.meta.env.VITE_APP_API_BASE_URL || "";
  //   const wsProtocol = location.protocol === "https:" ? "wss:" : "ws:";
  const urlPre = baseUrl.startsWith("https")
    ? baseUrl.replace("https", "wss")
    : baseUrl.replace("http", "ws");
  const wsProtocol = location.protocol === "https:" ? "wss:" : "ws:";
  const url = `${urlPre}/ws`;
  return url;
};

/**
 * 创建带有 Authorization header 的 WebSocket 连接
 * @param url WebSocket URL
 * @param token 认证令牌
 * @returns WebSocket 实例
 */
export const createAppEventWebSocket = (
  url: string,
  token?: string
): WebSocket => {
  const ws = new WebSocket(url);
  return ws;
};

/**
 * 获取埋点上报状态
 */
export const getTrackingStatus = () => {
  return request.get<{ eventTrack: boolean }>(`${prefix}/tracking/status`);
};

/**
 * 设置埋点上报状态
 * @param enabled 是否启用埋点上报
 */
export const setTrackingStatus = (enabled: boolean) => {
  return request.post<void>(`${prefix}/tracking/set-status`, null, {
    params: { enabled },
  });
};
