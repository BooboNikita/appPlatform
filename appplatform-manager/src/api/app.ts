import request from "@/utils/request";
import type { AxiosProgressEvent } from "axios";
import { id } from "element-plus/es/locale";

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

// 获取应用列表
export const getAppList = (params: PageParams) => {
  return request.get<PageResult<AppInfo>>("/apps", { params });
};

export const getAppById = (id: number) => {
  return request.get<AppInfo>(`/app/${id}`);
};

// 上传应用
export const uploadApp = (
  data: FormData,
  onUploadProgress?: (progressEvent: AxiosProgressEvent) => void
) => {
  return request.post("/upload", data, {
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
  return request.put(`/update/${id}`, data, {
    headers: {
      "Content-Type": "multipart/form-data",
    },
    onUploadProgress,
  });
};

// 删除应用
export const deleteApp = (id: number) => {
  return request.delete(`/delete/${id}`);
};

// 下载应用
export const downloadApp = (id: number, fileName?: string) => {
  return request
    .get(`/download/${id}`, {
      responseType: "blob",
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
    });
};
