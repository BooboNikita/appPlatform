import { defineStore } from "pinia";
import { ref } from "vue";
import { useRouter } from "vue-router";
import {
  login as loginApi,
  logout as logoutApi,
  getInfo,
  register as registerApi,
} from "@/api/user";
import type { LoginForm } from "@/types/user";

export const useUserStore = defineStore("user", () => {
  const router = useRouter();
  const token = ref(localStorage.getItem("token") || "");
  const userInfo = ref<any>(null);

  // 登录
  const login = async (loginForm: LoginForm) => {
    try {
      const { data } = await loginApi(loginForm);
      token.value = data.token;
      localStorage.setItem("token", data.token);
      await getUserInfo();
      router.push("/");
      return true;
    } catch (error) {
      return Promise.reject(error);
    }
  };

  const register = async (registerForm: LoginForm) => {
    try {
      const { data } = await registerApi({
        username: registerForm.username,
        password: registerForm.password,
      });
      return true;
    } catch (error) {
      return Promise.reject(error);
    }
  };

  // 获取用户信息
  const getUserInfo = async () => {
    try {
      const { data } = await getInfo();
      userInfo.value = data;
      return data;
    } catch (error) {
      return Promise.reject(error);
    }
  };

  // 登出
  const logout = async () => {
    try {
      // await logoutApi();
      resetToken();
      // router.push("/login");
    } catch (error) {
      console.error("Logout error:", error);
    }
  };

  // 重置token
  const resetToken = () => {
    token.value = "";
    userInfo.value = null;
    localStorage.removeItem("token");
  };

  return {
    token,
    userInfo,
    login,
    logout,
    getUserInfo,
    resetToken,
    register,
  };
});
