<template>
  <div class="tracking-list">
    <div class="filter-container">
      <el-form :inline="true" :model="queryParams" class="demo-form-inline">
        <el-form-item label="用户ID">
          <el-input
            v-model="queryParams.userId"
            placeholder="用户ID"
            clearable
            @keyup.enter="handleQuery"
          />
        </el-form-item>
        <el-form-item label="用户名">
          <el-input
            v-model="queryParams.userName"
            placeholder="用户名"
            clearable
            @keyup.enter="handleQuery"
          />
        </el-form-item>
        <el-form-item label="页面URL">
          <el-input
            v-model="queryParams.pageUrl"
            placeholder="页面URL"
            clearable
            @keyup.enter="handleQuery"
          />
        </el-form-item>
        <el-form-item label="事件类型">
          <el-select
            v-model="queryParams.eventType"
            placeholder="请选择"
            clearable
            style="width: 150px"
            @change="handleQuery"
          >
            <el-option label="click" value="click" />
            <el-option label="view" value="view" />
            <el-option label="exposure" value="exposure" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleQuery">查询</el-button>
          <el-button @click="resetQuery">重置</el-button>
        </el-form-item>
      </el-form>
    </div>

    <div class="action-container">
      <el-button
        :type="wsConnected ? 'success' : 'danger'"
        @click="toggleWebSocket"
      >
        <el-icon v-if="!wsConnected"><CircleClose /></el-icon>
        <el-icon v-else><CircleCheck /></el-icon>
        {{ wsConnected ? "实时刷新打开" : "实时刷新关闭" }}
      </el-button>
      <el-button @click="handleRefresh">
        <el-icon><Refresh /></el-icon>
        刷新
      </el-button>
    </div>

    <el-table
      v-loading="loading"
      :data="trackingList"
      border
      :style="{ width: '100%' }"
    >
      <el-table-column prop="id" label="ID" width="60" />
      <el-table-column prop="userId" label="用户ID" min-width="120" />
      <el-table-column prop="userName" label="用户名" min-width="100" />
      <el-table-column prop="sessionId" label="会话ID" min-width="140" />
      <el-table-column prop="pageUrl" label="页面URL" min-width="150">
        <template #default="{ row }">
          <el-text truncated>{{ row.pageUrl }}</el-text>
        </template>
      </el-table-column>
      <el-table-column label="设备信息" min-width="140">
        <template #default="{ row }">
          <el-button type="primary" size="small" @click="showDeviceDetail(row)">
            查看
          </el-button>
        </template>
      </el-table-column>
      <el-table-column label="事件信息" min-width="140">
        <template #default="{ row }">
          <div class="event-cell">
            <div>类型: {{ row.eventInfo.eventType }}</div>
            <div>ID: {{ row.eventInfo.eventId }}</div>
          </div>
        </template>
      </el-table-column>
      <el-table-column prop="event.eventTime" label="事件时间" width="180">
        <template #default="{ row }">
          {{ formatTimestamp(row.eventInfo.eventTime) }}
        </template>
      </el-table-column>
      <el-table-column prop="event.recvTime" label="接收时间" width="180">
        <template #default="{ row }">
          {{ formatTimestamp(row.eventInfo.recvTime) }}
        </template>
      </el-table-column>
      <el-table-column label="操作" width="100" fixed="right">
        <template #default="{ row }">
          <el-button type="primary" size="small" @click="showDetail(row)">
            详情
          </el-button>
        </template>
      </el-table-column>
    </el-table>

    <div class="pagination-container">
      <el-pagination
        v-model:current-page="queryParams.pageNum"
        v-model:page-size="queryParams.pageSize"
        :page-sizes="[10, 20, 30, 50]"
        layout="total, sizes, prev, pager, next, jumper"
        :total="total"
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
      />
    </div>

    <!-- 详情抽屉 -->
    <el-drawer v-model="detailVisible" title="埋点数据详情" size="50%">
      <div v-if="selectedItem" class="detail-content">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="ID">
            {{ selectedItem.id }}
          </el-descriptions-item>
          <el-descriptions-item label="用户ID">
            {{ selectedItem.userId }}
          </el-descriptions-item>
          <el-descriptions-item label="用户名">
            {{ selectedItem.userName }}
          </el-descriptions-item>
          <el-descriptions-item label="会话ID">
            {{ selectedItem.sessionId }}
          </el-descriptions-item>
          <el-descriptions-item label="页面URL" :span="2">
            {{ selectedItem.pageUrl }}
          </el-descriptions-item>
          <el-descriptions-item label="Referrer" :span="2">
            {{ selectedItem.referrer }}
          </el-descriptions-item>
          <el-descriptions-item label="状态">
            <el-tag :type="selectedItem.status === 1 ? 'success' : 'danger'">
              {{ selectedItem.status === 0 ? "正常" : "测试" }}
            </el-tag>
          </el-descriptions-item>
        </el-descriptions>

        <el-divider>应用信息</el-divider>
        <el-descriptions :column="2" border>
          <el-descriptions-item label="版本">
            {{ selectedItem.app.version }}
          </el-descriptions-item>
          <el-descriptions-item label="构建号">
            {{ selectedItem.app.buildNumber }}
          </el-descriptions-item>
        </el-descriptions>

        <el-divider>设备信息</el-divider>
        <el-descriptions :column="2" border>
          <el-descriptions-item label="设备ID">
            {{ selectedItem.device.deviceId }}
          </el-descriptions-item>
          <el-descriptions-item label="设备型号">
            {{ selectedItem.device.model }}
          </el-descriptions-item>
          <el-descriptions-item label="品牌">
            {{ selectedItem.device.brand }}
          </el-descriptions-item>
          <el-descriptions-item label="IP地址">
            {{ selectedItem.device.ip }}
          </el-descriptions-item>
          <el-descriptions-item label="操作系统">
            {{ selectedItem.device.os }}
          </el-descriptions-item>
          <el-descriptions-item label="系统版本">
            {{ selectedItem.device.osVersion }}
          </el-descriptions-item>
          <el-descriptions-item label="网络类型">
            {{ selectedItem.device.networkType }}
          </el-descriptions-item>
          <el-descriptions-item label="屏幕分辨率">
            {{ selectedItem.device.screenResolution }}
          </el-descriptions-item>
        </el-descriptions>

        <el-divider>事件信息</el-divider>
        <el-descriptions :column="2" border>
          <el-descriptions-item label="事件ID" :span="2">
            {{ selectedItem.eventInfo.eventId }}
          </el-descriptions-item>
          <el-descriptions-item label="事件类型">
            {{ selectedItem.eventInfo.eventType }}
          </el-descriptions-item>
          <el-descriptions-item label="事件时间">
            {{ formatTimestamp(selectedItem.eventInfo.eventTime) }}
          </el-descriptions-item>
          <el-descriptions-item label="接收时间" :span="2">
            {{ formatTimestamp(selectedItem.eventInfo.recvTime) }}
          </el-descriptions-item>
        </el-descriptions>
      </div>
    </el-drawer>

    <!-- 设备信息抽屉 -->
    <el-drawer v-model="deviceVisible" title="设备信息" size="40%">
      <div v-if="selectedItem" class="device-content">
        <el-descriptions :column="1" border>
          <el-descriptions-item label="设备ID">
            {{ selectedItem.device.deviceId }}
          </el-descriptions-item>
          <el-descriptions-item label="设备型号">
            {{ selectedItem.device.model }}
          </el-descriptions-item>
          <el-descriptions-item label="品牌">
            {{ selectedItem.device.brand }}
          </el-descriptions-item>
          <el-descriptions-item label="IP地址">
            {{ selectedItem.device.ip }}
          </el-descriptions-item>
          <el-descriptions-item label="操作系统">
            {{ selectedItem.device.os }}
          </el-descriptions-item>
          <el-descriptions-item label="系统版本">
            {{ selectedItem.device.osVersion }}
          </el-descriptions-item>
          <el-descriptions-item label="网络类型">
            {{ selectedItem.device.networkType }}
          </el-descriptions-item>
          <el-descriptions-item label="屏幕分辨率">
            {{ selectedItem.device.screenResolution }}
          </el-descriptions-item>
        </el-descriptions>
      </div>
    </el-drawer>
  </div>
</template>

<script lang="ts" setup>
import { ref, reactive, onMounted, onUnmounted } from "vue";
import { ElMessage } from "element-plus";
import { Refresh, CircleClose, CircleCheck } from "@element-plus/icons-vue";
import { getAppEventList, getAppEventWebSocketUrl } from "@/api/app_event";
import { TrackingEvent } from "@/types/tracking";
import { useUserStore } from "@/stores/user";

const loading = ref(false);
const trackingList = ref<TrackingEvent[]>([]);
const total = ref(0);
const detailVisible = ref(false);
const deviceVisible = ref(false);
const selectedItem = ref<TrackingEvent | null>(null);
const wsConnected = ref(false);
let ws: WebSocket | null = null;
let wsReconnectTimer: ReturnType<typeof setTimeout> | null = null;

const queryParams = reactive({
  pageNum: 1,
  pageSize: 10,
  userId: "",
  userName: "",
  pageUrl: "",
  eventType: "",
});

/**
 * 创建带有 Authorization token 的 WebSocket 连接
 * 通过 URL 参数传递 token
 */
const createWebSocketWithAuth = (url: string, token: string): WebSocket => {
  // ✅ 检查 URL 是否已经包含参数
  // 如果已有参数，使用 & 连接；否则使用 ?
  const separator = url.includes("?") ? "&" : "?";
  const wsUrlWithToken = `${url}${separator}token=${encodeURIComponent(token)}`;
  const socket = new WebSocket(wsUrlWithToken);
  return socket;
};

/**
 * 格式化时间戳
 */
const formatTimestamp = (timestamp: number): string => {
  if (!timestamp) return "-";
  const date = new Date(timestamp);
  return date.toLocaleString();
};

/**
 * 获取埋点数据列表
 */
const fetchTrackingList = async () => {
  try {
    loading.value = true;
    const response = await getAppEventList(queryParams);
    console.log("埋点数据列表:", response);
    if (response.data) {
      const pageResult = response.data;
      trackingList.value = pageResult.list;
      total.value = pageResult.total;
    } else {
      ElMessage.error("获取埋点数据失败：数据格式错误");
    }
  } catch (error) {
    console.error("获取埋点数据失败:", error);
    ElMessage.error("获取埋点数据失败");
  } finally {
    loading.value = false;
  }
};

/**
 * 查询
 */
const handleQuery = () => {
  queryParams.pageNum = 1;
  fetchTrackingList();
};

/**
 * 重置查询
 */
const resetQuery = () => {
  queryParams.pageNum = 1;
  queryParams.userId = "";
  queryParams.userName = "";
  queryParams.pageUrl = "";
  queryParams.eventType = "";
  fetchTrackingList();
};

/**
 * 分页处理
 */
const handleSizeChange = (val: number) => {
  queryParams.pageSize = val;
  fetchTrackingList();
};

const handleCurrentChange = (val: number) => {
  queryParams.pageNum = val;
  fetchTrackingList();
};

/**
 * 刷新数据
 */
const handleRefresh = () => {
  fetchTrackingList();
};

/**
 * 显示详情
 */
const showDetail = (row: TrackingEvent) => {
  selectedItem.value = row;
  detailVisible.value = true;
};

/**
 * 显示设备信息
 */
const showDeviceDetail = (row: TrackingEvent) => {
  selectedItem.value = row;
  deviceVisible.value = true;
};

/**
 * 初始化 WebSocket 连接
 */
const initWebSocket = () => {
  try {
    const userStore = useUserStore();
    if (!userStore.token) {
      ElMessage.warning("请先登录");
      return;
    }

    // 获取 WebSocket URL
    const wsUrl = getAppEventWebSocketUrl();
    console.log("WebSocket URL:", wsUrl);

    // 使用带有 Authorization token 的 WebSocket 创建函数
    ws = createWebSocketWithAuth(wsUrl, userStore.token);

    ws.onopen = () => {
      console.log("WebSocket 已连接，Authorization token 已发送");
      wsConnected.value = true;
      ElMessage.success("实时刷新打开");
      // 清除重连计时器
      if (wsReconnectTimer) {
        clearTimeout(wsReconnectTimer);
        wsReconnectTimer = null;
      }
    };

    ws.onmessage = (event) => {
      try {
        const message = JSON.parse(event.data);
        console.log("收到 WebSocket 消息:", message);

        // 处理新的埋点数据推送
        // 后端通过 @SendTo("/topic/events") 推送的数据格式
        let newEvent: TrackingEvent | null = null;
        if (message.content && typeof message.content === "object") {
          newEvent = message.content.data;
        }

        if (newEvent && newEvent.id) {
          // 添加新数据到列表顶端
          trackingList.value.unshift(newEvent);

          // 如果列表超过当前页码大小，删除最后一条
          if (trackingList.value.length > queryParams.pageSize) {
            trackingList.value.pop();
          }

          total.value += 1;
          ElMessage.success("收到新的埋点数据");
        }
      } catch (error) {
        console.error("处理 WebSocket 消息失败:", error);
      }
    };

    ws.onerror = (error) => {
      console.error("WebSocket 错误:", error);
      wsConnected.value = false;
      ElMessage.error("实时刷新连接出错");
    };

    ws.onclose = () => {
      console.log("WebSocket 连接已关闭");
      wsConnected.value = false;

      // 自动重连（最多重连 5 次）
      if (!wsReconnectTimer) {
        wsReconnectTimer = setTimeout(() => {
          console.log("尝试重新连接 WebSocket...");
          initWebSocket();
        }, 3000);
      }
    };
  } catch (error) {
    console.error("初始化 WebSocket 失败:", error);
    ElMessage.error("初始化实时刷新失败");
    wsConnected.value = false;
  }
};

/**
 * 切换 WebSocket 连接状态
 */
const toggleWebSocket = () => {
  if (wsConnected.value) {
    closeWebSocket();
  } else {
    initWebSocket();
  }
};

/**
 * 关闭 WebSocket 连接
 */
const closeWebSocket = () => {
  if (ws) {
    ws.close();
    ws = null;
  }
  if (wsReconnectTimer) {
    clearTimeout(wsReconnectTimer);
    wsReconnectTimer = null;
  }
  wsConnected.value = false;
  ElMessage.info("实时刷新已关闭");
};

onMounted(() => {
  fetchTrackingList();
});

onUnmounted(() => {
  closeWebSocket();
});
</script>

<style lang="scss" scoped>
.tracking-list {
  background: #fff;
  padding: 20px;
  border-radius: 4px;
  box-shadow: 0 1px 4px rgba(0, 21, 41, 0.08);

  .filter-container {
    margin-bottom: 20px;
  }

  .action-container {
    margin-bottom: 20px;
    display: flex;
    gap: 10px;
  }

  .pagination-container {
    margin-top: 20px;
    text-align: right;
  }

  .event-cell {
    font-size: 12px;
    line-height: 1.5;

    div {
      white-space: nowrap;
      overflow: hidden;
      text-overflow: ellipsis;
    }
  }

  .detail-content,
  .device-content {
    padding: 10px;
  }

  :deep(.el-descriptions__item) {
    word-break: break-all;
  }
}
</style>
