<template>
  <div class="crash-page">
    <div class="filter-bar">
      <el-form :inline="true" :model="filters" class="filter-form">
        <el-form-item>
          <el-input v-model="filters.appId" placeholder="应用ID" clearable />
        </el-form-item>
        <el-form-item>
          <el-input
            v-model="filters.crashType"
            placeholder="崩溃类型"
            clearable
          />
        </el-form-item>
        <el-form-item>
          <el-input v-model="filters.userId" placeholder="用户ID" clearable />
        </el-form-item>
        <el-form-item>
          <el-date-picker
            v-model="dateRange"
            type="datetimerange"
            start-placeholder="开始时间"
            end-placeholder="结束时间"
            value-format="YYYY-MM-DD HH:mm:ss"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="search">查询</el-button>
          <el-button @click="reset">重置</el-button>
        </el-form-item>
      </el-form>
    </div>

    <el-table :data="crashList" style="width: 100%" v-loading="loading" stripe>
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="userId" label="用户ID" width="120" />
      <el-table-column label="昵称" width="100">
        <template #default="{ row }">{{ getNickname(row) }}</template>
      </el-table-column>
      <el-table-column prop="crashTimestamp" label="崩溃时间" width="180">
        <template #default="{ row }">{{
          formatDate(row.crashTimestamp)
        }}</template>
      </el-table-column>
      <el-table-column prop="crashType" label="崩溃类型" width="100" />
      <el-table-column
        prop="message"
        label="崩溃信息"
        width="200"
        show-overflow-tooltip
      />
      <el-table-column prop="appVersion" label="版本" width="80" />
      <el-table-column
        prop="deviceModel"
        label="设备型号"
        width="120"
        show-overflow-tooltip
      />

      <el-table-column label="操作" width="180" fixed="right">
        <template #default="{ row }">
          <el-button type="primary" size="small" @click="toDetail(row.id)"
            >详情</el-button
          >
          <el-button type="danger" size="small" @click="handleDelete(row.id)"
            >删除</el-button
          >
        </template>
      </el-table-column>
    </el-table>

    <div class="pagination">
      <el-pagination
        v-model:current-page="pageNum"
        v-model:page-size="pageSize"
        :page-sizes="[10, 20, 50]"
        layout="total, sizes, prev, pager, next, jumper"
        :total="total"
        @current-change="onPageChange"
        @size-change="onSizeChange"
      />
    </div>
  </div>
</template>

<script lang="ts" setup>
import { ref, reactive, onMounted } from "vue";
import { useRouter } from "vue-router";
import { ElMessage, ElMessageBox } from "element-plus";
import { getCrashList, deleteCrash, type CrashReport } from "@/api/crash";
import { formatDate } from "@/utils/index";

const router = useRouter();
const loading = ref(false);
const crashList = ref<CrashReport[]>([]);
const total = ref(0);
const pageNum = ref(1);
const pageSize = ref(10);

const filters = reactive({
  appId: "",
  crashType: "",
  userId: "",
});
const dateRange = ref<[string, string] | null>(null);

// const formatDateTime = (dateStr: string) => {
//   if (!dateStr) return "-";
//   const date = new Date(dateStr);
//   const year = date.getFullYear();
//   const month = String(date.getMonth() + 1).padStart(2, "0");
//   const day = String(date.getDate()).padStart(2, "0");
//   const hours = String(date.getHours()).padStart(2, "0");
//   const minutes = String(date.getMinutes()).padStart(2, "0");
//   const seconds = String(date.getSeconds()).padStart(2, "0");
//   return `${year}-${month}-${day} ${hours}:${minutes}:${seconds}`;
// };

const getNickname = (row: CrashReport) => {
  if (!row.customData) return "-";
  try {
    const data = JSON.parse(row.customData);
    return data.nickname || "-";
  } catch {
    return "-";
  }
};

const fetch = async () => {
  try {
    loading.value = true;
    const params: any = { pageNum: pageNum.value, pageSize: pageSize.value };
    if (dateRange.value && dateRange.value.length === 2) {
      params.startDate = dateRange.value[0];
      params.endDate = dateRange.value[1];
    }
    if (filters.appId) params.appId = filters.appId;
    if (filters.crashType) params.crashType = filters.crashType;
    if (filters.userId) params.userId = filters.userId;

    const { data } = await getCrashList(params);
    crashList.value = data.list || [];
    total.value = data.total || 0;
  } catch (err) {
    console.error(err);
    ElMessage.error("加载失败");
  } finally {
    loading.value = false;
  }
};

const search = () => {
  pageNum.value = 1;
  fetch();
};

const reset = () => {
  filters.appId = "";
  filters.crashType = "";
  filters.userId = "";
  dateRange.value = null;
  pageNum.value = 1;
  fetch();
};

const onPageChange = (v: number) => {
  pageNum.value = v;
  fetch();
};

const onSizeChange = (s: number) => {
  pageSize.value = s;
  pageNum.value = 1;
  fetch();
};

const toDetail = (id: number) => {
  router.push({ path: `/crash/detail/${id}` });
};

const handleDelete = async (id: number) => {
  try {
    await ElMessageBox.confirm("确定要删除这条崩溃报告吗？", "提示", {
      confirmButtonText: "确定",
      cancelButtonText: "取消",
      type: "warning",
    });
    await deleteCrash(id);
    ElMessage.success("删除成功");
    fetch();
  } catch (err: any) {
    if (err !== "cancel") {
      console.error(err);
      ElMessage.error("删除失败");
    }
  }
};

onMounted(fetch);
</script>

<style scoped lang="scss">
.crash-page {
  padding: 16px;
}
.filter-bar {
  margin-bottom: 12px;
}
.filter-form {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
}
.filter-form :deep(.el-form-item) {
  margin-bottom: 8px;
}
.pagination {
  margin-top: 12px;
  display: flex;
  justify-content: flex-end;
}
</style>
