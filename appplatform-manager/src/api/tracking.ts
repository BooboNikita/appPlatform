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
export const getTrackingList = (params: PageParams) => {
  return request.get<ApiResponse<PageResult<TrackingEvent>>>(
    `${prefix}/recent`,
    { params }
  );
};

/**
 * 获取埋点数据详情
 * @param id 埋点数据ID
 */
export const getTrackingDetail = (id: number) => {
  return request.get<TrackingEvent>(`${prefix}/${id}`);
};

/**
 * 获取 WebSocket 连接地址
 */
export const getWebSocketUrl = () => {
  const baseUrl = import.meta.env.VITE_APP_API_BASE_URL || "";
  const wsProtocol = location.protocol === "https:" ? "wss:" : "ws:";
  const url = `${wsProtocol}//${location.host}${baseUrl}/topic/events`;
  return url;
};
