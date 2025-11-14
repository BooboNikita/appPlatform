import request from "@/utils/request";
import axios, { type AxiosProgressEvent, type CancelTokenSource } from "axios";
import { progressProps } from "element-plus";
import { id } from "element-plus/es/locale";

// 存储所有下载的取消令牌
const downloadCancelTokens = new Map<number, CancelTokenSource>();

// 应用信息接口
export interface AppInfo {
  id: number;
  appName: string;
  packageName: string;
  version: string;
  buildNumber: string;
  size: string;
  downloadTimes: number;
  isBeta: boolean;
  createTime: string;
  features?: string;
}

// 分页参数
interface PageParams {
  pageNum: number;
  pageSize: number;
  [key: string]: any;
}

// 分页返回结果
interface PageResult<T> {
  list: T[];
  total: number;
  pageNum: number;
  pageSize: number;
}

const prefix = "/api-app";

// 获取应用列表
export const getAppList = (params: PageParams) => {
  return request.get<PageResult<AppInfo>>(`${prefix}/apps`, { params });
};

export const getAppById = (id: number) => {
  return request.get<AppInfo>(`${prefix}/app/${id}`);
};

// 上传应用
export const uploadApp = (
  data: FormData,
  onUploadProgress?: (progressEvent: AxiosProgressEvent) => void
) => {
  return request.post(`${prefix}/upload`, data, {
    headers: {
      "Content-Type": "multipart/form-data",
    },
    onUploadProgress,
  });
};

export const updateApp = (
  id: number,
  data: FormData,
  onUploadProgress?: (progressEvent: AxiosProgressEvent) => void
) => {
  return request.put(`${prefix}/update/${id}`, data, {
    headers: {
      "Content-Type": "multipart/form-data",
    },
    onUploadProgress,
  });
};

// 删除应用
export const deleteApp = (id: number) => {
  return request.delete(`${prefix}/delete/${id}`);
};

// 下载应用
export const downloadApp = (
  id: number,
  fileName?: string,
  onDownloadProgress?: (progress: number) => void
) => {
  // 创建新的取消令牌
  const source = axios.CancelToken.source();
  downloadCancelTokens.set(id, source);

  return request
    .get(`${prefix}/download/${id}`, {
      responseType: "blob",
      timeout: 0,
      cancelToken: source.token,
      onDownloadProgress: (progressEvent: AxiosProgressEvent) => {
        if (onDownloadProgress) {
          console.log("下载进度事件:", progressEvent);
          const progress = Math.round(
            (progressEvent.loaded / (progressEvent.total || 1)) * 100
          );
          onDownloadProgress(progress);
        }
      },
    })
    .then((response) => {
      // 从响应头获取文件名
      let filename = fileName ?? `app-${id}.apk`; // 默认文件名

      // 创建下载链接
      const url = window.URL.createObjectURL(new Blob([response.data]));
      const link = document.createElement("a");
      link.href = url;
      link.setAttribute("download", filename);
      document.body.appendChild(link);
      link.click();
      document.body.removeChild(link);
      window.URL.revokeObjectURL(url);
    })
    .finally(() => {
      // 下载完成或取消，清理取消令牌
      downloadCancelTokens.delete(id);
    });
};

// 取消下载
export const cancelDownload = (id: number) => {
  const source = downloadCancelTokens.get(id);
  if (source) {
    source.cancel(`下载已取消`);
    downloadCancelTokens.delete(id);
  }
};
