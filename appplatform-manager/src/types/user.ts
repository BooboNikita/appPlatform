// 登录表单接口
export interface LoginForm {
  username: string;
  password: string;
}

// 用户信息接口
export interface UserInfo {
  id: number;
  username: string;
  email: string;
  [key: string]: any;
}
