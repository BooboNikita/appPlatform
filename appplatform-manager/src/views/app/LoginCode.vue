<template>
  <div class="form-qrcode-container">
    <div class="form-qrcode-card">
      <div class="form-qrcode-form">
        <el-form
          :model="form"
          ref="formRef"
          :rules="formRules"
          label-width="70px"
          autocomplete="off"
        >
          <el-form-item label="用户名" prop="username">
            <el-autocomplete
              v-model="form.username"
              :fetch-suggestions="querySearch"
              placeholder="手机号/账号"
              clearable
              class="inline-input"
              @select="handleSelect"
              style="width: 100%"
              name="new-username"
              autocomplete="new-password"
            />
          </el-form-item>
          <el-form-item label="密码" prop="password">
            <el-input
              v-model="form.password"
              type="password"
              placeholder="请输入密码"
              show-password
              clearable
              name="new-password"
              autocomplete="new-password"
            />
          </el-form-item>
          <el-form-item>
            <el-button
              type="primary"
              size="large"
              @click="handleGenerateQrcode"
              round
            >
              生成二维码
            </el-button>
          </el-form-item>
        </el-form>
      </div>

      <div class="form-qrcode-qrcode">
        <div class="qrcode-img-container" v-if="qrcodeBase64.dataUrl">
          <el-image
            :src="qrcodeBase64.dataUrl"
            fit="contain"
            class="qrcode-img"
          />
          <img
            src="@/assets/logo.png"
            fit="contain"
            class="qrcode-center-img"
          />
        </div>

        <div v-else class="qrcode-placeholder">
          <el-icon size="48" color="#dcdfe6"><Picture /></el-icon>
          <span>二维码预览</span>
        </div>
      </div>
    </div>
  </div>
</template>

<script lang="ts" setup>
import { ref, reactive, onMounted } from "vue";
import QRCode from "qrcode";
import { ElMessage } from "element-plus";
import { Picture } from "@element-plus/icons-vue";

interface SavedAccount {
  value: string;
  password?: string;
}

const formRef = ref();
const form = reactive({
  username: "",
  password: "",
});

const qrcodeBase64 = reactive({
  dataUrl: "",
});

const formRules = {
  username: [{ required: true, message: "请输入用户名", trigger: "blur" }],
  password: [{ required: true, message: "请输入密码", trigger: "blur" }],
};

const savedAccounts = ref<SavedAccount[]>([]);
const STORAGE_KEY = "login_code_history";

onMounted(() => {
  loadSavedAccounts();
});

const loadSavedAccounts = () => {
  const stored = localStorage.getItem(STORAGE_KEY);
  if (stored) {
    try {
      savedAccounts.value = JSON.parse(stored);
    } catch (e) {
      console.error("Failed to parse saved accounts", e);
      savedAccounts.value = [];
    }
  }
};

const saveAccount = () => {
  const { username, password } = form;
  if (!username || !password) return;

  // Remove existing entry for this username
  const index = savedAccounts.value.findIndex(
    (item) => item.value === username,
  );
  if (index > -1) {
    savedAccounts.value.splice(index, 1);
  }

  // Add to top
  savedAccounts.value.unshift({ value: username, password });

  // Limit history size (e.g., 20 items)
  if (savedAccounts.value.length > 20) {
    savedAccounts.value = savedAccounts.value.slice(0, 20);
  }

  localStorage.setItem(STORAGE_KEY, JSON.stringify(savedAccounts.value));
};

const querySearch = (queryString: string, cb: any) => {
  const results = queryString
    ? savedAccounts.value.filter(createFilter(queryString))
    : savedAccounts.value;
  cb(results);
};

const createFilter = (queryString: string) => {
  return (item: SavedAccount) => {
    return item.value.toLowerCase().includes(queryString.toLowerCase());
  };
};

const handleSelect = (item: Record<string, any>) => {
  const account = item as SavedAccount;
  form.username = account.value;
  if (account.password) {
    form.password = account.password;
  }
};

const handleGenerateQrcode = async () => {
  try {
    await formRef.value.validate();
    saveAccount(); // Save history on success
    const qrcodeData = { username: form.username, password: form.password };
    qrcodeBase64.dataUrl = await generateQrcode(qrcodeData);
  } catch (error) {
    ElMessage.error("生成二维码失败");
    qrcodeBase64.dataUrl = "";
  }
};

const generateQrcode = async (data: { username: string; password: string }) => {
  return await QRCode.toDataURL(JSON.stringify(data), {
    margin: 1,
    width: 200,
    color: {
      dark: "#111111",
      light: "#ffffff",
    },
  });
};
</script>

<style scoped>
.form-qrcode-container {
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: 100vh;
  background: linear-gradient(135deg, #f5f7fa 0%, #c3cfe2 100%);
}

.form-qrcode-card {
  display: flex;
  gap: 40px;
  padding: 48px;
  border-radius: 16px;
  background: #ffffff;
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.08);
}

.form-qrcode-form {
  width: 280px;
}

.form-qrcode-qrcode {
  width: 200px;
  height: 200px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.qrcode-img-container {
  display: flex;
  align-items: center;
  justify-content: center;
}

.qrcode-img {
  width: 100%;
  height: 100%;
  border-radius: 8px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
}

.qrcode-center-img {
  position: absolute;
  width: 40px;
  height: 40px;
  background-color: #ffffff;
  border-radius: 4px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
}

.qrcode-placeholder {
  flex-direction: column;
  color: #909399;
  font-size: 14px;
  gap: 8px;
}

.el-button {
  width: 100%;
}
</style>
