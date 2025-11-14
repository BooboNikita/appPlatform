import request from "@/utils/request";

export interface LogItem {
  id: number;
  username: string;
  nickname?: string;
  uploadtime: string; // ISO string
  path: string; // comma separated file urls
  appname: string;
  version: string;
}

interface PageParams {
  pageNum: number;
  pageSize: number;
  startDate?: string;
  endDate?: string;
  appName?: string;
  username?: string;
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
