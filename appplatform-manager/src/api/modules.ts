import request from "@/utils/request";

const prefix = "/api-modules";

export interface ModuleItem {
  id?: number;
  title: string;
  iconUrl: string;
  targetUrl: string;
  color: string;
  port: number;
  route: "under_development" | "inner" | "webview";
  sortOrder: number;
  isActive: boolean;
  hideForTest: boolean;
  createAt?: string;
  updateAt?: string;
}

export interface ModuleListResponse {
  list: ModuleItem[];
  total: number;
}

// 获取所有模块
export const getModulesList = () => {
  return request.get(`${prefix}/all`);
};

// 获取模块详情
export const getModuleById = (id: number) => {
  return request.get(`${prefix}/${id}`);
};

// 创建模块
export const createModule = (data: ModuleItem) => {
  return request.post(`${prefix}/create`, data);
};

// 更新模块
export const updateModule = (id: number, data: ModuleItem) => {
  return request.put(`${prefix}/update/${id}`, data);
};

// 删除模块
export const deleteModule = (id: number) => {
  return request.delete(`${prefix}/${id}`);
};
