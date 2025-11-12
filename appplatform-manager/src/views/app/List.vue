<template>
  <div class="app-list">
    <div class="filter-container">
      <el-form :inline="true" :model="queryParams" class="demo-form-inline">
        <el-form-item label="应用名称">
          <el-input
            v-model="queryParams.appName"
            placeholder="应用名称"
            clearable
            @keyup.enter="handleQuery"
          />
        </el-form-item>
        <el-form-item label="版本号">
          <el-input
            v-model="queryParams.version"
            placeholder="版本号"
            clearable
            @keyup.enter="handleQuery"
          />
        </el-form-item>
        <el-form-item label="测试版">
          <el-select
            v-model="queryParams.isBeta"
            placeholder="请选择"
            clearable
            style="width: 150px"
            @change="handleQuery"
          >
            <el-option label="是" :value="true" />
            <el-option label="否" :value="false" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleQuery">查询</el-button>
          <el-button @click="resetQuery">重置</el-button>
        </el-form-item>
      </el-form>
    </div>

    <div class="action-container">
      <el-button type="primary" @click="handleCreate">
        <el-icon><Plus /></el-icon>
        新增应用
      </el-button>
    </div>

    <el-table v-loading="loading" :data="appList" border style="width: 100%">
      <el-table-column prop="appName" label="应用名称" min-width="120" />
      <!-- <el-table-column prop="packageName" label="包名" min-width="180" /> -->
      <el-table-column prop="version" label="版本号" width="120" />
      <el-table-column prop="buildNumber" label="构建号" width="100" />
      <el-table-column prop="size" label="大小" width="100" />
      <el-table-column prop="downloadTimes" label="下载次数" width="100" />
      <el-table-column prop="isBeta" label="测试版" width="80">
        <template #default="{ row }">
          <el-tag :type="row.isBeta ? 'warning' : 'success'">
            {{ row.isBeta ? "是" : "否" }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="createTime" label="上传时间" width="180">
        <template #default="{ row }">
          {{ formatDate(row.createTime) }}
        </template>
      </el-table-column>
      <el-table-column label="操作" width="250" fixed="right">
        <template #default="{ row }">
          <el-button type="primary" size="small" @click="handleDownload(row)">
            下载
          </el-button>
          <el-button type="warning" size="small" @click="handleEdit(row)">
            修改
          </el-button>
          <el-button type="danger" size="small" @click="handleDelete(row)">
            删除
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
  </div>
</template>

<script lang="ts" setup>
import { ref, reactive, onMounted } from "vue";
import { useRouter } from "vue-router";
import { ElMessage, ElMessageBox } from "element-plus";
import { Plus } from "@element-plus/icons-vue";
import { getAppList, deleteApp, downloadApp } from "@/api/app";
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
}

const router = useRouter();
const loading = ref(false);
const appList = ref<AppInfo[]>([]);
const total = ref(0);

const queryParams = reactive({
  pageNum: 1,
  pageSize: 10,
  appName: "",
  version: "",
  isBeta: undefined,
});

// 获取应用列表
const fetchAppList = async () => {
  try {
    loading.value = true;
    const { data, code } = await getAppList(queryParams);
    console.log("应用列表数据:", data, code);
    appList.value = data.list;
    total.value = data.total;
  } catch (error) {
    console.error("获取应用列表失败:", error?.toString());
  } finally {
    loading.value = false;
  }
};

// 查询
const handleQuery = () => {
  queryParams.pageNum = 1;
  fetchAppList();
};

// 重置查询
const resetQuery = () => {
  queryParams.pageNum = 1;
  queryParams.appName = "";
  queryParams.version = "";
  queryParams.isBeta = undefined;
  fetchAppList();
};

// 分页
const handleSizeChange = (val: number) => {
  queryParams.pageSize = val;
  fetchAppList();
};

const handleCurrentChange = (val: number) => {
  queryParams.pageNum = val;
  fetchAppList();
};

// 新增应用
const handleCreate = () => {
  router.push("/app/upload");
};

// 编辑应用
const handleEdit = (row: AppInfo) => {
  // 将应用信息通过路由参数或 state 传递给上传页面
  router.push({
    path: "/app/upload",
    query: { id: row.id },
  });
};

// 下载应用
const handleDownload = async (row: AppInfo) => {
  try {
    await downloadApp(row.id, `${row.appName}_v${row.version}.apk`);
    // 更新下载次数
    const app = appList.value.find((item) => item.id === row.id);
    if (app) {
      app.downloadTimes += 1;
    }
  } catch (error) {
    console.error("下载失败:", error);
  }
};

// 删除应用
const handleDelete = (row: AppInfo) => {
  ElMessageBox.confirm(`确定要删除应用 "${row.appName}" 吗？`, "提示", {
    confirmButtonText: "确定",
    cancelButtonText: "取消",
    type: "warning",
  })
    .then(async () => {
      try {
        await deleteApp(row.id);
        ElMessage.success("删除成功");
        fetchAppList();
      } catch (error) {
        console.error("删除失败:", error);
      }
    })
    .catch(() => {});
};

onMounted(() => {
  fetchAppList();
});
</script>

<style lang="scss" scoped>
.app-list {
  background: #fff;
  padding: 20px;
  border-radius: 4px;
  box-shadow: 0 1px 4px rgba(0, 21, 41, 0.08);

  .filter-container {
    margin-bottom: 20px;
  }

  .action-container {
    margin-bottom: 20px;
  }

  .pagination-container {
    margin-top: 20px;
    text-align: right;
  }
}
</style>
