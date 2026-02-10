import request from "@/utils/request";

export interface PerformanceReview {
  deptId: string;
  name?: string;
  coverImage?: string;
  deadline?: string;
  [key: string]: any;
}

const prefix = "/api-performance-review";

// 获取部门封面图
export const getCoverImage = (deptId: string) => {
  return request.get<any, string>(`${prefix}/cover/${deptId}`);
};

// 获取组织名称
export const getName = (deptId: string) => {
  return request.get<any, string>(`${prefix}/name/${deptId}`);
};

// 获取部门截止时间
export const getDeadline = (deptId: string) => {
  return request.get<any, string>(`${prefix}/deadline/${deptId}`);
};

// 获取部门完整配置
export const getDeptConfig = (deptId: string) => {
  return request.get<any, PerformanceReview>(`${prefix}/config/${deptId}`);
};

// 设置部门封面图
export const setCoverImage = (
  deptId: string,
  coverImage: string,
  operator: string = "admin",
) => {
  const formData = new FormData();
  formData.append("coverImage", coverImage);
  formData.append("operator", operator);
  return request.post<any, string>(`${prefix}/cover/${deptId}`, formData);
};

// 设置组织名称
export const setName = (
  deptId: string,
  name: string,
  operator: string = "admin",
) => {
  const formData = new FormData();
  formData.append("name", name);
  formData.append("operator", operator);
  return request.post<any, string>(`${prefix}/name/${deptId}`, formData);
};

// 设置部门截止时间
export const setDeadline = (
  deptId: string,
  deadline: string,
  operator: string = "admin",
) => {
  const formData = new FormData();
  formData.append("deadline", deadline);
  formData.append("operator", operator);
  return request.post<any, string>(`${prefix}/deadline/${deptId}`, formData);
};

// 更新部门完整配置
export const updateDeptConfig = (deptId: string, data: PerformanceReview) => {
  return request.put<any, PerformanceReview>(
    `${prefix}/config/${deptId}`,
    data,
  );
};

// 删除部门配置
export const deleteDeptConfig = (deptId: string) => {
  return request.delete<any, void>(`${prefix}/config/${deptId}`);
};

// 获取所有部门配置
export const getAllConfigs = () => {
  return request.get<any, PerformanceReview[]>(`${prefix}/list`);
};

// 批量获取部门配置
export const getBatchConfigs = (deptIds: string[]) => {
  return request.post<any, Record<string, PerformanceReview>>(
    `${prefix}/batch`,
    deptIds,
  );
};

// 检查部门配置是否存在
export const existsByDeptId = (deptId: string) => {
  return request.get<any, boolean>(`${prefix}/exists/${deptId}`);
};
