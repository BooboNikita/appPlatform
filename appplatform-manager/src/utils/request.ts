import axios, {
  type AxiosInstance,
  type AxiosRequestConfig,
  type AxiosResponse,
} from "axios";
import { ElMessage } from "element-plus";
import { useUserStore } from "@/stores/user";

// 创建axios实例
const service: AxiosInstance = axios.create({
  baseURL: import.meta.env.VITE_APP_API_BASE_URL,
  timeout: 30000, // 请求超时时间
  withCredentials: true, // 允许携带cookie
});

// 请求拦截器
service.interceptors.request.use(
  (config: any) => {
    const userStore = useUserStore();
    // 如果用户已登录，添加token到请求头
    if (userStore.token) {
      config.headers = config.headers || {};
      config.headers["Authorization"] = `Bearer ${userStore.token}`;
    }
    return config;
  },
  (error: any) => {
    console.error("Request Error:", error);
    return Promise.reject(error);
  }
);

// 响应拦截器
service.interceptors.response.use(
  (response: AxiosResponse) => {
    const res = response.data;

    // 如果返回的是文件流，直接返回
    if (response.config.responseType === "blob") {
      return response;
    }

    // 如果返回的是数组或不是对象结构，直接返回（兼容服务端直接返回数据的情况）
    if (Array.isArray(res) || typeof res !== "object" || res === null) {
      return { data: res, code: 200, message: "success" };
    }

    // 根据业务状态码处理
    if (res.code !== 200) {
      ElMessage.error(res.msg || res.message || "系统错误");
      // 创建一个包含完整响应数据的错误对象
      const error = new Error(res.msg || res.message || "Error");
      (error as any).response = res;
      (error as any).code = res.code;
      (error as any).data = res.data;
      return Promise.reject(error);
    }

    return res;
  },
  (error: any) => {
    console.error("Response Error:", error);

    // 处理 HTTP 状态码
    if (error.response) {
      const { status, data } = error.response;
      const message = data?.message || error.message;

      switch (status) {
        case 400:
          ElMessage.error(`请求参数错误: ${message}`);
          break;
        case 401:
          // 未授权，跳转到登录页
          useUserStore().logout();
          window.location.href = "/login";
          break;
        case 403:
          ElMessage.error("没有权限访问该资源");
          break;
        case 404:
          ElMessage.error("请求的资源不存在");
          break;
        case 500:
          ElMessage.error(`服务器错误: ${message}`);
          break;
        default:
          ElMessage.error(`请求失败: ${message}`);
      }
    } else if (error.message.includes("timeout")) {
      ElMessage.error("请求超时，请检查网络连接");
    } else if (error.message === "Network Error") {
      ElMessage.error("网络连接失败，请检查网络");
    } else {
      ElMessage.error(error.message || "请求失败");
    }

    return Promise.reject(error);
  }
);

export default service;
