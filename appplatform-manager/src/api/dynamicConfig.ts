import request from "@/utils/request";

export interface DynamicConfig {
  id: number;
  versionRange: string;
  fileUrl: string;
  env: string;
  remark: string;
  createTime: string;
  updateTime: string;
}

export interface DynamicConfigHistory {
  id: number;
  configId: number;
  versionRange: string;
  env: string;
  remark: string;
  content?: string;
  createTime: string;
  creator?: string;
}

const prefix = "/api-dynamic-config";

// 获取所有配置列表
export const getDynamicConfigList = () => {
  return request.get<DynamicConfig[]>(`${prefix}/list`);
};

// 获取配置文件内容
export const getDynamicConfigContent = (id: number) => {
  return request.get<any>(`${prefix}/${id}/content`);
};

// 更新动态配置（元数据或文件）
export const updateDynamicConfig = (id: number, data: FormData) => {
  return request.put<DynamicConfig>(`${prefix}/${id}`, data, {
    headers: {
      "Content-Type": "multipart/form-data",
    },
  });
};

// 上传新配置
export const uploadDynamicConfig = (data: FormData) => {
  return request.post<DynamicConfig>(`${prefix}/upload`, data, {
    headers: {
      "Content-Type": "multipart/form-data",
    },
  });
};

// 删除配置
export const deleteDynamicConfig = (id: number) => {
  return request.delete(`${prefix}/${id}`);
};

export const updateDynamicConfigContent = (
  id: number,
  content: string,
  versionRange: string,
  env: string,
  remark: string,
) => {
  const formData = new FormData();
  const blob = new Blob([content], { type: "application/json" });
  formData.append("file", blob, "config.json");
  formData.append("versionRange", versionRange);
  formData.append("env", env);
  formData.append("remark", remark);
  return updateDynamicConfig(id, formData);
};

// 获取配置历史列表
export const getConfigHistory = (id: number) => {
  return request.get<DynamicConfigHistory[]>(`${prefix}/${id}/history`);
};

// 获取所有历史列表
export const getAllHistory = () => {
  return request.get<DynamicConfigHistory[]>(`${prefix}/history/all`);
};

// 恢复到历史版本
export const revertToHistory = (configId: number, historyId: number) => {
  return request.post(`${prefix}/${configId}/revert/${historyId}`);
};

// 获取单个历史记录详情
export const getHistoryById = (historyId: number) => {
  return request.get<DynamicConfigHistory>(`${prefix}/history/${historyId}`);
};

// 获取历史记录内容
export const getHistoryContent = (historyId: number) => {
  return request.get<string>(`${prefix}/history/${historyId}/content`);
};
