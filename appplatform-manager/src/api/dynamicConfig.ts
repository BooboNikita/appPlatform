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

const prefix = "/api-dynamic-config";

// 获取所有配置列表
export const getDynamicConfigList = () => {
  return request.get<DynamicConfig[]>(`${prefix}/list`);
};

// 获取配置文件内容
export const getDynamicConfigContent = (id: number) => {
  return request.get<string>(`${prefix}/${id}/content`);
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

// 更新配置内容（直接提交 JSON 字符串，后端需要支持这个操作，或者前端构造 FormData）
// 根据 Controller，updateConfig 接收 MultipartFile。
// 如果用户在右侧编辑框修改了内容，我们需要将其包装成一个文件上传，或者后端需要一个接收字符串的接口。
// 当前 Controller 只有:
// @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
// public Result<DynamicConfig> updateConfig(...)
// 其中 MultipartFile file 是可选的。
// 如果只修改内容，我们需要把字符串转成 Blob 然后放入 FormData。

export const updateDynamicConfigContent = (
  id: number,
  content: string,
  versionRange: string,
  env: string,
  remark: string
) => {
  const formData = new FormData();
  const blob = new Blob([content], { type: "application/json" });
  formData.append("file", blob, "config.json");
  formData.append("versionRange", versionRange);
  formData.append("env", env);
  formData.append("remark", remark);
  return updateDynamicConfig(id, formData);
};
