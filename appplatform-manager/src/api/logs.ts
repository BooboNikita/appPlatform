import request from "@/utils/request";

export interface LogItem {
  id: number;
  username: string;
  nickname?: string;
  uploadtime: string; // ISO string
  path: string; // comma separated file urls
  appName: string; // changed from appname to appName (based on List.vue usage)
  version: string;
  imageUrls: string; // changed from imageUrl to imageUrls (based on List.vue usage)
  problem: string;
}

export interface LogRequest {
  id: number;
  username: string;
  status: number; // 0: pending, 1: uploaded, 2: timeout
  requestTime: string;
  expireTime: string;
}

interface PageParams {
  pageNum: number;
  pageSize: number;
  startDate?: string;
  endDate?: string;
  appName?: string;
  username?: string;
  hasRequest?: boolean; // new filter
  requestDate?: string; // new filter
}

interface PageResult<T> {
  list: T[];
  total: number;
  pageNum: number;
  pageSize: number;
}

const prefix = "/api-logs";

export const getLogsList = (params: PageParams) => {
  return request.get<PageResult<LogItem>>(`${prefix}/list`, { params });
};

export const getLogById = (id: number) => {
  return request.get<LogItem>(`${prefix}/${id}`);
};

// fetch single file content by path (server should return text/plain or similar)
export const getLogFileContent = (path: string) => {
  // Backend expects a POST with JSON body { filePath }
  // and returns plain text. Pass responseType:'text' in the axios config.
  return request.post(`${prefix}/file`, null, {
    params: { filePath: path },
  });
};

export const createLogRequest = (username: string, timeoutMinutes?: number) => {
  return request.post<LogRequest>(`${prefix}/request`, null, {
    params: { username, timeoutMinutes },
  });
};

export const checkLogRequest = (username: string) => {
  return request.get<boolean>(`${prefix}/request/check`, {
    params: { username },
  });
};

export const getLogRequestList = (params: {
  username?: string;
  status?: number;
  startDate?: string;
  endDate?: string;
}) => {
  return request.get<LogRequest[]>(`${prefix}/request/list`, { params });
};

export const deleteLogRequest = (id: number) => {
  return request.delete<void>(`${prefix}/request/${id}`);
};
