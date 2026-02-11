import request from "@/utils/request";

const prefix = "/api/store-link-config";

export interface StoreLinkConfig {
  id: number;
  deviceBrand: string;
  linkTemplate: string;
  enabled: number; // 1 for enabled, 0 for disabled
  sortOrder: number;
  remark: string;
  createTime?: string;
  updateTime?: string;
}

export interface StoreLinkConfigDto {
  deviceBrand: string;
  linkTemplate: string;
  enabled: number;
  sortOrder: number;
  remark?: string;
}

// 获取所有配置
export const getAllConfigs = () => {
  return request.get<StoreLinkConfig[]>(`${prefix}`);
};

// 根据ID查询配置
export const getConfigById = (id: number) => {
  return request.get<StoreLinkConfig>(`${prefix}/${id}`);
};

// 创建配置
export const createConfig = (data: StoreLinkConfigDto) => {
  return request.post<StoreLinkConfig>(`${prefix}`, data);
};

// 更新配置
export const updateConfig = (id: number, data: StoreLinkConfigDto) => {
  return request.put<StoreLinkConfig>(`${prefix}/${id}`, data);
};

// 删除配置
export const deleteConfig = (id: number) => {
  return request.delete<boolean>(`${prefix}/${id}`);
};

// 启用/禁用配置
export const updateConfigEnabled = (id: number, enabled: number) => {
  return request.put<boolean>(`${prefix}/${id}/enabled`, null, {
    params: { enabled },
  });
};

const appPrefix = "/api-app";

// 获取应用更新总开关状态
export const getAppUpdateStatus = () => {
  return request.get<{ appUpdateTotalEnabled: boolean }>(
    `${appPrefix}/update-total/status`,
  );
};

// 设置应用更新总开关状态
export const setAppUpdateStatus = (enabled: boolean) => {
  return request.post<string>(`${appPrefix}/update-total/set-status`, null, {
    params: { enabled },
  });
};
