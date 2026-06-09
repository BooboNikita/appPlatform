<template>
  <div class="login-container">
    <div class="login-box">
      <div class="login-left">
        <div class="welcome">欢迎使用</div>
        <div class="title">应用管理平台</div>
        <div class="subtitle">Application Management Platform</div>
      </div>
      <div class="login-form">
        <div class="form-title">用户登录</div>
        <el-form
          ref="loginFormRef"
          :model="loginForm"
          :rules="loginRules"
          class="login-form"
          auto-complete="on"
          label-position="left"
        >
          <el-form-item prop="username">
            <el-input
              v-model="loginForm.username"
              placeholder="请输入用户名"
              type="text"
              tabindex="1"
              auto-complete="on"
            >
              <template #prefix>
                <el-icon><User /></el-icon>
              </template>
            </el-input>
          </el-form-item>

          <el-form-item prop="password">
            <el-input
              v-model="loginForm.password"
              type="password"
              placeholder="请输入密码"
              tabindex="2"
              auto-complete="on"
              show-password
              @keyup.enter="handleLogin"
            >
              <template #prefix>
                <el-icon><Lock /></el-icon>
              </template>
            </el-input>
          </el-form-item>

          <!-- <el-form-item prop="code">
            <div class="code-container">
              <el-input
                v-model="loginForm.code"
                placeholder="验证码"
                style="width: 60%"
                @keyup.enter="handleLogin"
              >
                <template #prefix>
                  <el-icon><Key /></el-icon>
                </template>
              </el-input>
              <img
                :src="captchaUrl"
                class="captcha"
                alt="验证码"
                @click="refreshCaptcha"
              />
            </div>
          </el-form-item> -->

          <el-checkbox v-model="loginForm.rememberMe" class="remember-me">
            记住我
          </el-checkbox>

          <el-button
            :loading="loading"
            type="primary"
            style="width: 100%; margin-top: 20px"
            @click.prevent="handleLogin"
          >
            登 录
          </el-button>
          <div class="register-container">
            <el-button type="text" @click="showRegisterForm = true"
              >注册</el-button
            >
          </div>
          <el-dialog
            v-model="showRegisterForm"
            title="用户注册"
            width="30%"
            :before-close="handleCloseRegisterForm"
          >
            <el-form
              :model="registerForm"
              :rules="registerRules"
              ref="registerFormRef"
            >
              <el-form-item label="用户名" prop="username">
                <el-input
                  v-model="registerForm.username"
                  autocomplete="off"
                ></el-input>
              </el-form-item>
              <el-form-item label="密码" prop="password">
                <el-input
                  type="password"
                  v-model="registerForm.password"
                  autocomplete="off"
                ></el-input>
              </el-form-item>
              <el-form-item label="确认密码" prop="password">
                <el-input
                  type="password"
                  v-model="registerForm.confirmPassword"
                  autocomplete="off"
                ></el-input>
              </el-form-item>
            </el-form>
            <template #footer>
              <span class="dialog-footer">
                <el-button @click="handleCloseRegisterForm">取消</el-button>
                <el-button type="primary" @click="handleRegister"
                  >注册</el-button
                >
              </span>
            </template>
          </el-dialog>
        </el-form>
      </div>
    </div>
  </div>
</template>

<script lang="ts" setup>
import { ref, reactive, onMounted } from "vue";
import { useRouter } from "vue-router";
import { ElMessage } from "element-plus";
import { User, Lock, Key } from "@element-plus/icons-vue";
import { useUserStore } from "@/stores/user";
import { tr } from "element-plus/es/locale";

const router = useRouter();
const userStore = useUserStore();

const loginForm = reactive({
  username: "",
  password: "",
  code: "1234",
  rememberMe: false,
});

const loginRules = {
  username: [{ required: true, message: "请输入用户名", trigger: "blur" }],
  password: [{ required: true, message: "请输入密码", trigger: "blur" }],
  code: [{ required: true, message: "请输入验证码", trigger: "blur" }],
};

const registerForm = reactive({
  username: "",
  password: "",
  confirmPassword: "",
});

const registerRules = {
  username: [{ required: true, message: "请输入用户名", trigger: "blur" }],
  password: [{ required: true, message: "请输入密码", trigger: "blur" }],
};

const loading = ref(false);
const showRegisterForm = ref(false);
const captchaUrl = ref("");
const loginFormRef = ref();

// 获取验证码
const refreshCaptcha = () => {
  // 这里替换为实际的验证码接口
  captchaUrl.value = `https://picsum.photos/120/40?t=${Date.now()}`;
};

// 登录
const handleLogin = async () => {
  try {
    loading.value = true;
    await userStore.login(loginForm);
    if (loginForm.rememberMe) {
      localStorage.setItem("rememberedUsername", loginForm.username);
      localStorage.setItem("rememberedPassword", loginForm.password);
    } else {
      localStorage.removeItem("rememberedUsername");
      localStorage.removeItem("rememberedPassword");
    }
    router.push("/");
  } catch (error) {
    console.error("Login error:", error);
    refreshCaptcha();
  } finally {
    loading.value = false;
  }
};

const handleCloseRegisterForm = () => {
  showRegisterForm.value = false;
};

const handleRegister = async () => {
  try {
    if (registerForm.password !== registerForm.confirmPassword) {
      ElMessage.error("两次输入的密码不一致");
      return;
    }
    // 调用注册接口
    const res = await userStore.register(registerForm);
    if (res == true) {
      ElMessage.success("注册成功，请登录");
      showRegisterForm.value = false;
    } else {
      ElMessage.error("注册失败");
    }
  } catch (error) {
    console.error("Register error:", error);
  }
};

onMounted(() => {
  refreshCaptcha();
  const rememberedUsername = localStorage.getItem("rememberedUsername");
  if (rememberedUsername) {
    loginForm.username = rememberedUsername;
    loginForm.password = localStorage.getItem("rememberedPassword") || "";
    loginForm.rememberMe = true;
  }
});
</script>

<style lang="scss" scoped>
.login-container {
  min-height: 100vh;
  width: 100%;
  background-color: #2d3a4b;
  overflow: hidden;
  display: flex;
  justify-content: center;
  align-items: center;

  .login-box {
    width: 900px;
    height: 500px;
    background: #fff;
    border-radius: 8px;
    display: flex;
    overflow: hidden;
    box-shadow: 0 0 20px rgba(0, 0, 0, 0.2);

    .login-left {
      width: 50%;
      background: linear-gradient(135deg, #409eff 0%, #337ecc 100%);
      color: #fff;
      padding: 60px 40px;
      display: flex;
      flex-direction: column;
      justify-content: center;

      .welcome {
        font-size: 24px;
        margin-bottom: 10px;
      }

      .title {
        font-size: 36px;
        font-weight: bold;
        margin-bottom: 20px;
      }

      .subtitle {
        font-size: 16px;
        opacity: 0.8;
      }
    }

    .login-form {
      width: 80%;
      padding: 60px 40px;
      display: flex;
      flex-direction: column;
      justify-content: center;

      .form-title {
        font-size: 24px;
        color: #303133;
        margin-bottom: 40px;
        text-align: center;
        font-weight: bold;
      }

      .code-container {
        display: flex;
        justify-content: space-between;
        align-items: center;
        width: 100%;

        .captcha {
          width: 35%;
          height: 40px;
          cursor: pointer;
          border-radius: 4px;
          margin-left: 10px;
        }
      }

      .remember-me {
        margin: 10px 0;
      }
    }
  }
}

// 响应式布局
@media (max-width: 992px) {
  .login-container .login-box {
    width: 90%;
    flex-direction: column;
    height: auto;

    .login-left,
    .login-form {
      width: 100%;
      padding: 40px 30px;
    }

    .login-left {
      text-align: center;
    }
  }
}
</style>
