import request from "@/utils/request";

// 动态配置元数据
export interface DynamicConfig {
  id: number;
  versionRange: string;
  env: string;
  remark?: string;
  createTime: string;
  updateTime: string;
}

// 整包历史版本
export interface DynamicConfigHistory {
  id: number;
  configId: number;
  versionRange: string;
  env: string;
  remark?: string;
  snapshotJson?: string;
  operationType: string;
  operator: string;
  createTime: string;
}

// 配置项（扁平列表，含名单数量聚合字段）
export interface DynamicConfigItem {
  id: number;
  configId: number;
  versionRange: string;
  env: string;
  domain: string;
  itemKey: string;
  itemValue: string;
  valueType: string;
  enabled: number;
  grayEnabled: number;
  grayPercent: number;
  remark?: string;
  whiteCount?: number;
  blackCount?: number;
  createTime: string;
  updateTime: string;
}

// 灰度名单用户
export interface DynamicConfigItemGrayUser {
  id: number;
  itemId: number;
  listType: string;
  username: string;
  nickname?: string;
  operator: string;
  createTime: string;
  updateTime: string;
}

// 配置项级历史（记录变更后状态，含名单快照）
export interface DynamicConfigItemHistory {
  id: number;
  configId: number;
  domain: string;
  itemKey: string;
  versionRange: string;
  env: string;
  itemValue: string;
  valueType: string;
  enabled: number;
  grayEnabled: number;
  grayPercent: number;
  grayUsersJson?: string;
  remark?: string;
  operationType: string;
  operator: string;
  createTime: string;
}

const prefix = "/api-dynamic-config";

// ==================== 配置元数据 ====================

// 获取所有配置列表
export const getDynamicConfigList = () =>
  request.get<DynamicConfig[]>(`${prefix}/list`);

// 删除整份配置（含全部配置项与名单）
export const deleteDynamicConfig = (id: number) =>
  request.delete(`${prefix}/${id}`);

// ==================== 配置项 ====================

// 配置项扁平列表
export const getDynamicConfigItems = (params: {
  env?: string;
  versionRange?: string;
  domain?: string;
  keyword?: string;
}) => request.get<DynamicConfigItem[]>(`${prefix}/items`, { params });

// 单独新增配置项（同配置下域+配置项不可重复）
export const addDynamicConfigItem = (
  data: Partial<DynamicConfigItem>,
  operator?: string,
) =>
  request.post<DynamicConfigItem>(`${prefix}/item`, data, {
    params: { operator },
  });

// 更新配置项（值/启用/灰度开关/灰度百分比/备注，不含名单）
export const updateDynamicConfigItem = (
  id: number,
  data: Partial<DynamicConfigItem>,
  operator?: string,
) =>
  request.put<DynamicConfigItem>(`${prefix}/item/${id}`, data, {
    params: { operator },
  });

// 删除配置项（级联删除名单）
export const deleteDynamicConfigItem = (id: number, operator?: string) =>
  request.delete(`${prefix}/item/${id}`, { params: { operator } });

// ==================== 灰度名单 ====================

// 查询某配置项的名单（listType: WHITE/BLACK）
export const getGrayUsers = (
  itemId: number,
  listType: string,
  keyword?: string,
) =>
  request.get<DynamicConfigItemGrayUser[]>(
    `${prefix}/item/${itemId}/gray-user`,
    {
      params: { listType, keyword },
    },
  );

// 批量新增名单（usernames 支持逗号/分号/换行分隔，幂等，返回实际新增条数）
export const addGrayUsers = (
  itemId: number,
  listType: string,
  usernames: string,
  operator?: string,
) =>
  request.post<number>(`${prefix}/item/${itemId}/gray-user`, null, {
    params: { listType, usernames, operator },
  });

// 删除单条名单
export const deleteGrayUser = (id: number, operator?: string) =>
  request.delete(`${prefix}/gray-user/${id}`, { params: { operator } });

// 清空某配置项指定类型的名单
export const clearGrayUsers = (
  itemId: number,
  listType: string,
  operator?: string,
) =>
  request.delete(`${prefix}/item/${itemId}/gray-user`, {
    params: { listType, operator },
  });

// 反查某用户命中的配置项 ID 列表
export const findItemIdsByUsername = (username: string) =>
  request.get<number[]>(`${prefix}/gray-user/by-username`, {
    params: { username },
  });

// ==================== 配置项级历史与单项回溯 ====================

export const getItemHistory = (itemId: number) =>
  request.get<DynamicConfigItemHistory[]>(`${prefix}/item/${itemId}/history`);

export const getItemHistoryById = (historyId: number) =>
  request.get<DynamicConfigItemHistory>(`${prefix}/item-history/${historyId}`);

// 单项回溯：仅影响该配置项
export const revertItemToHistory = (
  itemId: number,
  historyId: number,
  operator?: string,
) =>
  request.post<DynamicConfigItem>(
    `${prefix}/item/${itemId}/revert/${historyId}`,
    null,
    {
      params: { operator },
    },
  );

// ==================== 上传 / 预览 / 整包历史 ====================

// 上传整包 JSON（服务端拆解入库）
export const uploadDynamicConfig = (data: FormData) =>
  request.post<DynamicConfig>(`${prefix}/upload`, data, {
    headers: { "Content-Type": "multipart/form-data" },
  });

// 下发预览（走客户端 match 接口，灰度过滤后结果）
export const previewDynamicConfig = (
  version: string,
  env: string,
  username?: string,
) =>
  request.get<any>(`${prefix}/match`, { params: { version, env, username } });

// 获取配置内容（管理端全量）
export const getDynamicConfigContent = (id: number, env?: string) =>
  request.get<any>(`${prefix}/${id}/content`, { params: { env } });

// 整包历史
export const getConfigHistory = (configId: number) =>
  request.get<DynamicConfigHistory[]>(`${prefix}/${configId}/history`);

export const getAllHistory = (env?: string, versionRange?: string) =>
  request.get<DynamicConfigHistory[]>(`${prefix}/history/all`, {
    params: { env, versionRange },
  });

export const getHistoryById = (historyId: number) =>
  request.get<DynamicConfigHistory>(`${prefix}/history/${historyId}`);

export const getHistoryContent = (historyId: number) =>
  request.get<any>(`${prefix}/history/${historyId}/content`);

export const revertToHistory = (
  configId: number,
  historyId: number,
  operator?: string,
) =>
  request.post<DynamicConfig>(
    `${prefix}/${configId}/revert/${historyId}`,
    null,
    {
      params: { operator },
    },
  );
