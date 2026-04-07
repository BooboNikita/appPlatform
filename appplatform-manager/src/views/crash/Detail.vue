<template>
  <div class="crash-detail-container">
    <div class="header-bar">
      <el-button @click="goBack" icon="ArrowLeft">返回列表</el-button>
    </div>

    <div class="crash-layout" v-if="crash">
      <div class="left">
        <el-card class="stack-card">
          <div class="message-header">
            <h3>崩溃消息</h3>
            <el-button size="small" @click="copyMessage">复制</el-button>
          </div>
          <pre class="crash-message">{{ crash.message }}</pre>
        </el-card>

        <el-card class="stack-card">
          <div class="stack-header">
            <h3>调用栈</h3>
            <div class="stack-controls">
              <el-button size="small" @click="toggleWrap">
                {{ wrap ? "换行: 开" : "换行: 关" }}
              </el-button>
              <el-button size="small" @click="decreaseFont">A-</el-button>
              <el-button size="small" @click="increaseFont">A+</el-button>
              <el-button size="small" @click="copyStack">复制</el-button>
            </div>
          </div>
          <el-scrollbar class="stack-scroll">
            <pre
              class="stack-trace"
              :style="{
                fontSize: fontSize + 'px',
                overflowWrap: wrap ? 'anywhere' : 'normal',
                wordBreak: wrap ? 'break-word' : 'normal',
              }"
              >{{ crash.stackTrace }}</pre
            >
          </el-scrollbar>
        </el-card>
      </div>

      <div class="right">
        <el-card class="info-card">
          <h3>崩溃基本信息</h3>
          <el-descriptions :column="1" border>
            <el-descriptions-item label="ID">{{
              crash.id
            }}</el-descriptions-item>
            <el-descriptions-item label="崩溃ID">{{
              crash.crashId
            }}</el-descriptions-item>
            <el-descriptions-item label="应用ID">{{
              crash.appId
            }}</el-descriptions-item>
            <el-descriptions-item label="用户ID">{{
              crash.userId
            }}</el-descriptions-item>
            <el-descriptions-item label="会话ID">{{
              crash.sessionId
            }}</el-descriptions-item>
            <el-descriptions-item label="崩溃类型">
              <el-tag type="danger">{{ crash.crashType }}</el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="应用版本">{{
              crash.appVersion
            }}</el-descriptions-item>
            <el-descriptions-item label="构建号">{{
              crash.appBuildNumber
            }}</el-descriptions-item>
            <el-descriptions-item label="SDK版本">{{
              crash.sdkVersion
            }}</el-descriptions-item>
            <el-descriptions-item label="崩溃时间">{{
              formatDateTime(crash.crashTimestamp)
            }}</el-descriptions-item>
            <el-descriptions-item label="上报时间">{{
              formatDateTime(crash.reportTimestamp)
            }}</el-descriptions-item>
            <el-descriptions-item label="创建时间">{{
              formatDateTime(crash.createdAt)
            }}</el-descriptions-item>
          </el-descriptions>
        </el-card>

        <el-card class="info-card">
          <h3>设备信息</h3>
          <el-descriptions :column="1" border>
            <el-descriptions-item label="设备型号">{{
              crash.deviceModel || "-"
            }}</el-descriptions-item>
            <el-descriptions-item label="设备品牌">{{
              crash.deviceBrand || "-"
            }}</el-descriptions-item>
            <el-descriptions-item label="系统版本">{{
              crash.osVersion || "-"
            }}</el-descriptions-item>
            <el-descriptions-item label="平台">{{
              crash.platform || "-"
            }}</el-descriptions-item>
            <el-descriptions-item label="屏幕分辨率">{{
              crash.screenResolution || "-"
            }}</el-descriptions-item>
            <el-descriptions-item label="设备信息">{{
              crash.deviceInfo || "-"
            }}</el-descriptions-item>
            <el-descriptions-item label="总内存">{{
              crash.totalMemory || "-"
            }}</el-descriptions-item>
            <el-descriptions-item label="可用内存">{{
              crash.availableMemory || "-"
            }}</el-descriptions-item>
            <el-descriptions-item label="网络类型">{{
              crash.networkType || "-"
            }}</el-descriptions-item>
            <el-descriptions-item label="电池电量">{{
              crash.batteryLevel || "-"
            }}</el-descriptions-item>
          </el-descriptions>
        </el-card>

        <el-card class="info-card" v-if="customData">
          <h3>自定义数据</h3>
          <el-descriptions :column="1" border>
            <el-descriptions-item label="版本">{{
              customData.version || "-"
            }}</el-descriptions-item>
            <el-descriptions-item label="昵称">{{
              customData.nickname || "-"
            }}</el-descriptions-item>
            <el-descriptions-item label="用户名">{{
              customData.username || "-"
            }}</el-descriptions-item>
          </el-descriptions>
        </el-card>
      </div>
    </div>

    <div v-else-if="loading" class="loading">加载中...</div>
    <div v-else class="error">加载失败</div>
  </div>
</template>

<script lang="ts" setup>
import { ref, computed, onMounted } from "vue";
import { useRoute, useRouter } from "vue-router";
import { ElMessage } from "element-plus";
import { ArrowLeft } from "@element-plus/icons-vue";
import { getCrashById, type CrashReport } from "@/api/crash";

const route = useRoute();
const router = useRouter();
const id = Number(route.params.id);

const loading = ref(false);
const crash = ref<CrashReport | null>(null);

const wrap = ref(true);
const fontSize = ref(13);

const customData = computed(() => {
  if (!crash.value?.customData) return null;
  try {
    return JSON.parse(crash.value.customData);
  } catch {
    return null;
  }
});

const goBack = () => {
  router.back();
};

const formatDateTime = (dateStr: string) => {
  if (!dateStr) return "-";
  const date = new Date(dateStr);
  const year = date.getFullYear();
  const month = String(date.getMonth() + 1).padStart(2, "0");
  const day = String(date.getDate()).padStart(2, "0");
  const hours = String(date.getHours()).padStart(2, "0");
  const minutes = String(date.getMinutes()).padStart(2, "0");
  const seconds = String(date.getSeconds()).padStart(2, "0");
  return `${year}-${month}-${day} ${hours}:${minutes}:${seconds}`;
};

const toggleWrap = () => {
  wrap.value = !wrap.value;
};

const increaseFont = () => {
  if (fontSize.value < 32) fontSize.value += 1;
};

const decreaseFont = () => {
  if (fontSize.value > 8) fontSize.value -= 1;
};

const copyMessage = async () => {
  try {
    await navigator.clipboard.writeText(crash.value?.message || "");
    ElMessage.success("已复制到剪贴板");
  } catch (e) {
    ElMessage.error("复制失败");
  }
};

const copyStack = async () => {
  try {
    await navigator.clipboard.writeText(crash.value?.stackTrace || "");
    ElMessage.success("已复制到剪贴板");
  } catch (e) {
    ElMessage.error("复制失败");
  }
};

onMounted(async () => {
  try {
    loading.value = true;
    const { data } = await getCrashById(id);
    crash.value = data;
  } catch (err) {
    console.error(err);
    ElMessage.error("加载详情失败");
  } finally {
    loading.value = false;
  }
});
</script>

<style scoped lang="scss">
.crash-detail-container {
  padding: 16px;
  display: flex;
  flex-direction: column;
  gap: 12px;
}
.header-bar {
  display: flex;
  align-items: center;
}
.crash-layout {
  display: flex;
  gap: 16px;
  align-items: flex-start;
}
.left {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 12px;
}
.right {
  width: 400px;
  display: flex;
  flex-direction: column;
  gap: 12px;
}
.stack-card {
  :deep(.el-card__header) {
    padding: 12px 16px;
  }
  :deep(.el-card__body) {
    padding: 16px;
  }
}
.info-card {
  :deep(.el-card__header) {
    padding: 12px 16px;
  }
  :deep(.el-card__body) {
    padding: 16px;
  }
  h3 {
    margin: 0 0 12px 0;
    font-size: 16px;
    font-weight: 600;
  }
}
.message-header,
.stack-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
  h3 {
    margin: 0;
  }
}
.stack-controls {
  display: flex;
  gap: 8px;
}
.crash-message {
  background: #f5f5f5;
  padding: 12px;
  border-radius: 4px;
  margin: 0;
  white-space: pre-wrap;
  word-break: break-all;
}
.stack-scroll {
  height: 400px;
}
.stack-trace {
  white-space: pre-wrap;
  background: #111;
  color: #dcdcdc;
  padding: 12px;
  margin: 0;
}
.loading,
.error {
  text-align: center;
  padding: 48px;
  color: #666;
}
</style>
