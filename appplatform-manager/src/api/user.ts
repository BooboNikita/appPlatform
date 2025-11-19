import request from "@/utils/request";
import type { LoginForm } from "@/types/user";

// 用户信息接口
export interface UserInfo {
  id: number;
  username: string;
  email: string;
  [key: string]: any;
}

// 登录接口
export const login = (data: LoginForm) => {
  return request.post<{ token: string }>("/auth/login", data);
};

// 登出接口
export const logout = () => {
  return request.post("/logout");
};

// 获取用户信息接口
export const getInfo = () => {
  return request.get<UserInfo>("/auth/userinfo");
};

export const register = (data: LoginForm) => {
  return request.post("/auth/register", data);
};
