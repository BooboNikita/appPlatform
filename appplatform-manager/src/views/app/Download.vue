<template>
  <div class="client-download">
    <div class="container">
      <h1 class="page-title">应用下载</h1>

      <!-- Tab 切换 -->
      <el-tabs v-model="activeTab" @tab-change="handleTabChange">
        <el-tab-pane label="正式版" name="release">
          <div v-if="latestApp" class="latest-section">
            <div class="latest-card">
              <div class="app-logo">
                <img src="@/assets/logo.png" alt="App Logo" />
              </div>
              <div class="app-info">
                <div class="info-main">
                  <div class="info-left">
                    <h2 class="app-title">{{ latestApp.appName }}</h2>
                    <div class="app-details">
                      <span class="version">v{{ latestApp.version }}</span>
                      <span class="time">{{
                        formatDate(latestApp.createTime)
                      }}</span>
                    </div>
                  </div>
                  <div v-if="latestApp.features" class="app-features">
                    <p>{{ latestApp.features }}</p>
                  </div>
                </div>
                <el-button
                  type="primary"
                  size="large"
                  @click="handleDownload(latestApp)"
                >
                  <el-icon><Download /></el-icon>
                  下载最新版本
                </el-button>
              </div>
            </div>
          </div>

          <!-- 历史版本列表 -->
          <div v-if="appList.length > 0" class="history-section">
            <h3 class="section-title">历史版本</h3>
            <el-table :data="appList" stripe style="width: 100%">
              <el-table-column label="文件名" min-width="200">
                <template #default="{ row }">
                  <el-link type="primary" @click="handleDownload(row)">
                    {{ row.appName }}_v{{ row.version }}.apk
                  </el-link>
                </template>
              </el-table-column>
              <el-table-column label="版本号" width="100">
                <template #default="{ row }"> v{{ row.version }} </template>
              </el-table-column>
              <el-table-column label="文件时间" width="180">
                <template #default="{ row }">
                  {{ formatDate(row.createTime) }}
                </template>
              </el-table-column>
            </el-table>
          </div>
          <el-empty v-else description="暂无应用版本" />
        </el-tab-pane>

        <el-tab-pane label="Beta" name="beta">
          <div v-if="latestBetaApp" class="latest-section">
            <div class="latest-card">
              <div class="app-logo">
                <img src="@/assets/logo.png" alt="App Logo" />
              </div>
              <div class="app-info">
                <div class="info-main">
                  <div class="info-left">
                    <h2 class="app-title">
                      {{ latestBetaApp.appName }} (Beta)
                    </h2>
                    <div class="app-details">
                      <span class="version">v{{ latestBetaApp.version }}</span>
                      <span class="time">{{
                        formatDate(latestBetaApp.createTime)
                      }}</span>
                    </div>
                  </div>
                  <div v-if="latestBetaApp.features" class="app-features">
                    <p>{{ latestBetaApp.features }}</p>
                  </div>
                </div>
                <el-button
                  type="warning"
                  size="large"
                  @click="handleDownload(latestBetaApp)"
                >
                  <el-icon><Download /></el-icon>
                  下载Beta版本
                </el-button>
              </div>
            </div>
          </div>

          <!-- Beta 历史版本列表 -->
          <div v-if="betaAppList.length > 0" class="history-section">
            <h3 class="section-title">历史版本</h3>
            <el-table :data="betaAppList" stripe style="width: 100%">
              <el-table-column label="文件名" min-width="200">
                <template #default="{ row }">
                  <el-link type="primary" @click="handleDownload(row)">
                    {{ row.appName }}_v{{ row.version }}.apk
                  </el-link>
                </template>
              </el-table-column>
              <el-table-column label="版本号" width="100">
                <template #default="{ row }"> v{{ row.version }} </template>
              </el-table-column>
              <el-table-column label="文件时间" width="180">
                <template #default="{ row }">
                  {{ formatDate(row.createTime) }}
                </template>
              </el-table-column>
            </el-table>
          </div>
          <el-empty v-else description="暂无Beta版本" />
        </el-tab-pane>
      </el-tabs>
    </div>
  </div>
</template>

<script lang="ts" setup>
import { ref, computed, onMounted } from "vue";
import { ElMessage } from "element-plus";
import { Download } from "@element-plus/icons-vue";
import { getAppList, downloadApp } from "@/api/app";
import { formatDate } from "@/utils/index";

interface AppInfo {
  id: number;
  appName: string;
  packageName: string;
  version: string;
  buildNumber: string;
  size: string;
  downloadTimes: number;
  isBeta: boolean;
  createTime: string;
  features?: string;
}

const activeTab = ref("release");
const appList = ref<AppInfo[]>([]);
const betaAppList = ref<AppInfo[]>([]);
const loading = ref(false);

// 获取最新正式版应用
const latestApp = computed(() => {
  return appList.value.length > 0 ? appList.value[0] : null;
});

// 获取最新Beta版应用
const latestBetaApp = computed(() => {
  return betaAppList.value.length > 0 ? betaAppList.value[0] : null;
});

// 获取应用列表
const fetchAppList = async () => {
  try {
    loading.value = true;
    const { data } = await getAppList({ pageNum: 1, pageSize: 100 });

    // 分离正式版和Beta版，按时间排序（最新的在前）
    const allApps = data.list || [];
    const released = allApps
      .filter((app: AppInfo) => !app.isBeta)
      .sort(
        (a: AppInfo, b: AppInfo) =>
          new Date(b.createTime).getTime() - new Date(a.createTime).getTime()
      );
    const beta = allApps
      .filter((app: AppInfo) => app.isBeta)
      .sort(
        (a: AppInfo, b: AppInfo) =>
          new Date(b.createTime).getTime() - new Date(a.createTime).getTime()
      );

    appList.value = released;
    betaAppList.value = beta;
  } catch (error) {
    console.error("获取应用列表失败:", error);
    ElMessage.error("加载应用列表失败");
  } finally {
    loading.value = false;
  }
};

// 下载应用
const handleDownload = async (row: AppInfo) => {
  try {
    await downloadApp(row.id, `${row.appName}_v${row.version}.apk`);
    ElMessage.success("下载成功");
  } catch (error) {
    console.error("下载失败:", error);
    ElMessage.error("下载失败");
  }
};

// Tab 切换
const handleTabChange = () => {
  // 切换 Tab 时可以添加额外逻辑
};

onMounted(() => {
  fetchAppList();
});
</script>

<style lang="scss" scoped>
.client-download {
  min-height: 100vh;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  padding: 40px 20px;

  .container {
    max-width: 1000px;
    margin: 0 auto;

    .page-title {
      color: #fff;
      text-align: center;
      font-size: 32px;
      margin-bottom: 40px;
      font-weight: bold;
      text-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
    }
  }

  :deep(.el-tabs) {
    background: #fff;
    border-radius: 8px;
    padding: 20px;
    box-shadow: 0 4px 20px rgba(0, 0, 0, 0.1);

    .el-tabs__nav-wrap {
      margin-bottom: 20px;
    }

    .el-tabs__content {
      padding: 0;
    }
  }

  .latest-section {
    margin-bottom: 40px;

    .latest-card {
      display: flex;
      align-items: center;
      gap: 30px;
      padding: 30px;
      background: linear-gradient(135deg, #f5f7fa 0%, #c3cfe2 100%);
      border-radius: 8px;
      box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);

      .app-logo {
        flex-shrink: 0;
        width: 100px;
        height: 100px;
        border-radius: 12px;
        background: #fff;
        display: flex;
        align-items: center;
        justify-content: center;
        box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
        overflow: hidden;

        img {
          width: 100%;
          height: 100%;
          object-fit: cover;
        }
      }

      .app-info {
        flex: 1;
        display: flex;
        flex-direction: column;
        justify-content: space-between;

        .info-main {
          display: flex;
          gap: 30px;
          margin-bottom: 20px;
          flex: 1;

          .info-left {
            flex: 0 0 auto;

            .app-title {
              font-size: 24px;
              font-weight: bold;
              color: #333;
              margin-bottom: 12px;
            }

            .app-details {
              display: flex;
              gap: 20px;
              font-size: 14px;
              color: #666;

              .version {
                font-weight: 600;
                color: #667eea;
              }

              .time {
                color: #999;
              }
            }
          }

          .app-features {
            flex: 1;
            padding: 12px 16px;
            background: rgba(255, 255, 255, 0.5);
            border-radius: 4px;
            font-size: 14px;
            color: #555;

            strong {
              display: block;
              color: #333;
              font-weight: 600;
              margin-bottom: 8px;
            }

            p {
              margin: 0;
              line-height: 1.5;
              white-space: pre-wrap;
              word-break: break-word;
            }
          }
        }

        :deep(.el-button) {
          align-self: flex-start;
          font-size: 16px;
          padding: 12px 30px;
          min-width: 160px;
        }
      }

      @media (max-width: 768px) {
        flex-direction: column;
        gap: 20px;

        .app-logo {
          width: 80px;
          height: 80px;
        }

        .app-info {
          text-align: center;

          .info-main {
            flex-direction: column;
            gap: 16px;

            .info-left {
              .app-title {
                font-size: 20px;
              }

              .app-details {
                flex-direction: column;
                gap: 8px;
              }
            }

            .app-features {
              padding: 12px 16px;

              strong {
                margin-bottom: 8px;
              }
            }
          }

          :deep(.el-button) {
            align-self: center;
            width: 100%;
            max-width: 200px;
          }
        }
      }
    }
  }

  .history-section {
    margin-top: 40px;

    .section-title {
      font-size: 18px;
      font-weight: bold;
      color: #333;
      margin-bottom: 20px;
      border-left: 4px solid #667eea;
      padding-left: 12px;
    }

    :deep(.el-table) {
      border-radius: 8px;
      overflow: hidden;
      box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);

      .el-table__header-wrapper {
        background: #f5f7fa;
      }

      th {
        background: #f5f7fa !important;
        color: #333;
        font-weight: 600;
      }

      td {
        padding: 16px;
      }

      tbody tr:hover {
        background: #f9f9f9;
      }
    }
  }

  :deep(.el-empty) {
    padding: 40px 0;
  }
}
</style>
