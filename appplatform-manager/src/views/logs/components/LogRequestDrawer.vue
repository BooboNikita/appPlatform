<template>
  <el-drawer
    :model-value="modelValue"
    @update:model-value="$emit('update:modelValue', $event)"
    title="日志请求列表"
    size="60%"
    destroy-on-close
  >
    <div style="padding: 16px">
      <el-form :inline="true" :model="filters" class="drawer-filter-form">
        <el-form-item>
          <el-input
            v-model="filters.username"
            placeholder="用户名"
            clearable
            style="width: 150px"
          />
        </el-form-item>
        <el-form-item>
          <el-select
            v-model="filters.status"
            placeholder="状态"
            clearable
            style="width: 120px"
          >
            <el-option label="待上传" :value="0" />
            <el-option label="已上传" :value="1" />
            <el-option label="超时" :value="2" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-date-picker
            v-model="dateRange"
            type="daterange"
            start-placeholder="开始"
            end-placeholder="结束"
            value-format="YYYY-MM-DD"
            style="width: 240px"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="fetch">查询</el-button>
          <el-button @click="reset">重置</el-button>
        </el-form-item>
      </el-form>

      <el-table :data="list" v-loading="loading" stripe>
        <el-table-column prop="username" label="用户名" width="auto" />
        <el-table-column prop="status" label="状态" width="auto">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)">
              {{ getStatusLabel(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="requestTime" label="请求时间" width="auto">
          <template #default="{ row }">{{
            formatDate(row.requestTime, 8)
          }}</template>
        </el-table-column>
        <el-table-column prop="expireTime" label="过期时间" width="auto">
          <template #default="{ row }">{{
            formatDate(row.expireTime, 8)
          }}</template>
        </el-table-column>
        <el-table-column label="操作" width="100" fixed="right">
          <template #default="{ row }">
            <el-popconfirm
              title="确定要删除这条日志请求吗？"
              @confirm="handleDelete(row.id)"
            >
              <template #reference>
                <el-button type="danger" size="small" link>删除</el-button>
              </template>
            </el-popconfirm>
          </template>
        </el-table-column>
      </el-table>
    </div>
  </el-drawer>
</template>

<script lang="ts" setup>
import { ref, reactive, watch } from "vue";
import { ElMessage } from "element-plus";
import {
  getLogRequestList,
  deleteLogRequest,
  type LogRequest,
} from "@/api/logs";
import { formatDate } from "@/utils/index";

const props = defineProps<{
  modelValue: boolean;
}>();

const emit = defineEmits<{
  (e: "update:modelValue", value: boolean): void;
}>();

const loading = ref(false);
const list = ref<LogRequest[]>([]);
const dateRange = ref<[string, string] | null>(null);

const filters = reactive({
  username: "",
  status: undefined as number | undefined,
});

const fetch = async () => {
  try {
    loading.value = true;
    const params: any = { ...filters };
    if (dateRange.value && dateRange.value.length === 2) {
      params.startDate = dateRange.value[0];
      params.endDate = dateRange.value[1];
    }
    const { data } = await getLogRequestList(params);
    list.value = data || [];
  } catch (err) {
    console.error(err);
    ElMessage.error("获取日志请求列表失败");
  } finally {
    loading.value = false;
  }
};

const reset = () => {
  filters.username = "";
  filters.status = undefined;
  dateRange.value = null;
  fetch();
};

const handleDelete = async (id: number) => {
  try {
    await deleteLogRequest(id);
    ElMessage.success("删除成功");
    fetch(); // 重新加载列表
  } catch (err) {
    console.error(err);
  }
};

const getStatusType = (
  status: number,
): "info" | "success" | "danger" | "primary" => {
  switch (status) {
    case 0:
      return "info";
    case 1:
      return "success";
    case 2:
      return "danger";
    default:
      return "info";
  }
};

const getStatusLabel = (status: number) => {
  switch (status) {
    case 0:
      return "待上传";
    case 1:
      return "已上传";
    case 2:
      return "超时";
    default:
      return "未知";
  }
};

watch(
  () => props.modelValue,
  (val) => {
    if (val) {
      fetch();
    }
  },
);
</script>

<style scoped>
.drawer-filter-form {
  margin-bottom: 12px;
}
</style>
