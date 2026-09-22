<template>
  <div class="gray-user-panel">
    <el-tabs v-model="activeTab" @tab-change="fetchList">
      <el-tab-pane label="白名单（一定下发）" name="WHITE" />
      <el-tab-pane label="黑名单（一定不下发）" name="BLACK" />
    </el-tabs>

    <div class="toolbar">
      <el-input
        v-model="keyword"
        placeholder="搜索用户名"
        clearable
        style="width: 200px"
        @keyup.enter="fetchList"
        @clear="fetchList"
      />
      <el-button type="primary" @click="openAdd">添加用户</el-button>
      <el-button
        type="danger"
        plain
        :disabled="userList.length === 0"
        @click="handleClear"
        >清空名单</el-button
      >
    </div>

    <el-table
      v-loading="loading"
      :data="userList"
      stripe
      style="width: 100%"
      max-height="300"
    >
      <el-table-column
        prop="username"
        label="用户名"
        min-width="140"
        show-overflow-tooltip
      />
      <el-table-column
        label="昵称"
        prop="nickname"
        min-width="120"
        show-overflow-tooltip
      >
        <template #default="{ row }">{{ row.nickname || "-" }}</template>
      </el-table-column>
      <el-table-column
        prop="operator"
        label="操作人"
        width="110"
        show-overflow-tooltip
      />
      <el-table-column label="添加时间" width="150">
        <template #default="{ row }">{{
          formatDate(row.createTime, 8)
        }}</template>
      </el-table-column>
      <el-table-column label="操作" width="70">
        <template #default="{ row }">
          <el-button link type="danger" @click="handleDelete(row)"
            >删除</el-button
          >
        </template>
      </el-table-column>
      <template #empty>暂无用户</template>
    </el-table>

    <div class="tip">
      优先级：黑名单 &gt; 白名单 &gt;
      百分比灰度；开启灰度后，白名单用户一定下发，黑名单用户一定不下发，其余用户按百分比命中。
    </div>

    <el-dialog
      v-model="addVisible"
      title="添加用户"
      width="480px"
      append-to-body
    >
      <el-input
        v-model="addText"
        type="textarea"
        :rows="6"
        placeholder="每行一个用户名或昵称，也支持逗号/分号分隔（自动去重，不存在的用户会跳过）"
      />
      <template #footer>
        <el-button @click="addVisible = false">取消</el-button>
        <el-button type="primary" :loading="adding" @click="handleAdd"
          >确定</el-button
        >
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, watch } from "vue";
import { ElMessage, ElMessageBox } from "element-plus";
import {
  addGrayUsers,
  clearGrayUsers,
  deleteGrayUser,
  getGrayUsers,
  DynamicConfigItemGrayUser,
} from "@/api/dynamicConfig";
import { formatDate } from "@/utils/index";

const props = defineProps<{
  itemId: number | null;
}>();

const emit = defineEmits<{
  (e: "changed"): void;
}>();

const activeTab = ref("WHITE");
const keyword = ref("");
const userList = ref<DynamicConfigItemGrayUser[]>([]);
const loading = ref(false);
const addVisible = ref(false);
const addText = ref("");
const adding = ref(false);

const fetchList = async () => {
  if (!props.itemId) return;
  loading.value = true;
  try {
    const res: any = await getGrayUsers(
      props.itemId,
      activeTab.value,
      keyword.value || undefined,
    );
    userList.value = res?.data || [];
  } finally {
    loading.value = false;
  }
};

watch(
  () => props.itemId,
  () => fetchList(),
  { immediate: true }, // 每次挂载（含抽屉二次打开重挂载）都要拉取名单
);

const openAdd = () => {
  addText.value = "";
  addVisible.value = true;
};

const handleAdd = async () => {
  if (!props.itemId) return;
  if (!addText.value.trim()) {
    ElMessage.warning("请输入用户名");
    return;
  }
  adding.value = true;
  try {
    const res: any = await addGrayUsers(
      props.itemId,
      activeTab.value,
      addText.value.trim(),
    );
    const added = res?.data ?? 0;
    ElMessage.success(`新增 ${added} 个用户（重复/不存在的用户已自动跳过）`);
    addVisible.value = false;
    emit("changed");
    await fetchList(); // 确保列表刷新完成再关闭
  } finally {
    adding.value = false;
  }
};

const handleDelete = async (row: DynamicConfigItemGrayUser) => {
  await ElMessageBox.confirm(`确定删除用户「${row.username}」吗？`, "提示", {
    type: "warning",
  });
  await deleteGrayUser(row.id);
  ElMessage.success("删除成功");
  emit("changed");
  fetchList();
};

const handleClear = async () => {
  if (!props.itemId) return;
  const label = activeTab.value === "WHITE" ? "白名单" : "黑名单";
  await ElMessageBox.confirm(`确定清空该配置项的${label}吗？`, "提示", {
    type: "warning",
  });
  await clearGrayUsers(props.itemId, activeTab.value);
  ElMessage.success("清空成功");
  emit("changed");
  fetchList();
};

defineExpose({ fetchList });
</script>

<style scoped>
.toolbar {
  display: flex;
  gap: 10px;
  margin-bottom: 12px;
  align-items: center;
}
.tip {
  margin-top: 12px;
  font-size: 12px;
  color: var(--el-text-color-secondary);
}
</style>
