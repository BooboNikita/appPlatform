import request from "@/utils/request";
import { PageResult, TrackingEvent, ApiResponse } from "@/types/tracking";

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
  // 标准 WebSocket API 不支持直接设置 HTTP headers
  // 但可以通过以下方式创建带 Authorization 的连接：

  // 方法1: 使用 XMLHttpRequest 或 fetch 进行预连接验证（可选）
  // 方法2: 创建 WebSocket 并在服务器端通过其他方式验证

  // 这里我们创建 WebSocket，token 会通过其他机制传递
  // （例如：之前已通过 HTTP 登录获得的 cookie，或通过自定义拦截器）
  const ws = new WebSocket(url);

  // 如果需要通过自定义方式传递 token，可以在连接打开时发送
  // 但更标准的做法是通过服务器的拦截器或过滤器读取 Authorization header

  // 如果使用 STOMP 协议，可以通过 login 和 passcode 传递认证
  // ws.send(JSON.stringify({
  //   command: 'CONNECT',
  //   headers: {
  //     'Authorization': token
  //   }
  // }));

  return ws;
};
