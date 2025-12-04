<template>
  <div class="logs-page">
    <div class="filter-bar">
      <el-form :inline="true" :model="filters" class="filter-form">
        <el-form-item>
          <el-input v-model="filters.appName" placeholder="应用名" />
        </el-form-item>
        <el-form-item>
          <el-input v-model="filters.username" placeholder="用户名" />
        </el-form-item>
        <el-form-item>
          <el-date-picker
            v-model="dateRange"
            type="daterange"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            value-format="YYYY-MM-DD"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="search">查询</el-button>
          <el-button @click="reset">重置</el-button>
        </el-form-item>
      </el-form>
    </div>

    <el-table :data="logs" style="width: 100%" v-loading="loading" stripe>
      <el-table-column prop="appName" label="应用" width="auto" />
      <el-table-column prop="username" label="用户名" width="auto" />
      <el-table-column prop="nickname" label="昵称" width="auto" />
      <el-table-column prop="version" label="版本" width="auto" />
      <el-table-column prop="uploadtime" label="时间" width="auto">
        <template #default="{ row }">{{ formatDate(row.uploadtime) }}</template>
      </el-table-column>
      <el-table-column prop="imageUrls" label="图片" width="auto">
        <template #default="{ row }">
          <div v-if="row.imageUrls && row.imageUrls.length">
            <el-image
              v-for="(img, idx) in splitImageUrls(row.imageUrls)"
              :key="idx"
              :src="img"
              fit="cover"
              :preview-src-list="splitImageUrls(row.imageUrls)"
              style="width: 50px; height: 50px; margin-right: 8px"
              ref="imageRef"
              :preview-teleported="true"
            />
          </div>
          <div v-else>无</div>
        </template>
      </el-table-column>
      <el-table-column prop="problem" label="问题" width="auto" />

      <el-table-column label="操作" width="auto" fixed="right">
        <template #default="{ row }">
          <el-button type="primary" size="small" @click="toDetail(row.id)"
            >详情</el-button
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
import { ref, reactive, onMounted, watch } from "vue";
import { useRouter } from "vue-router";
import { ElMessage } from "element-plus";
import { getLogsList, LogItem } from "@/api/logs";
import { formatDate } from "@/utils/index";

const router = useRouter();
const loading = ref(false);
const logs = ref<LogItem[]>([]);
const total = ref(0);
const pageNum = ref(1);
const pageSize = ref(10);

const filters = reactive({ appName: "", username: "" });
const dateRange = ref<[string, string] | null>(null);

const fetch = async () => {
  try {
    loading.value = true;
    const params: any = { pageNum: pageNum.value, pageSize: pageSize.value };
    if (dateRange.value && dateRange.value.length === 2) {
      params.startDate = dateRange.value[0];
      params.endDate = dateRange.value[1];
    }
    if (filters.appName) params.appName = filters.appName;
    if (filters.username) params.username = filters.username;

    const { data } = await getLogsList(params);
    logs.value = data.list || [];
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
  filters.appName = "";
  filters.username = "";
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
  router.push({ path: `/logs/detail/${id}` });
};

const splitImageUrls = (imageUrls: string) => {
  if (!imageUrls) return [];
  return imageUrls.split(",").map((url) => url.trim());
};

onMounted(fetch);

// expose helper
const formatDateRef = formatDate;
</script>

<style scoped lang="scss">
.logs-page {
  padding: 16px;
}
.filter-bar {
  margin-bottom: 12px;
}
.pagination {
  margin-top: 12px;
  display: flex;
  justify-content: flex-end;
}
</style>
