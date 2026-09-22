<template>
  <el-dialog
    :model-value="modelValue"
    :title="`配置项历史 - ${title || ''}`"
    width="900px"
    :destroy-on-close="true"
    append-to-body
    @update:model-value="emit('update:modelValue', $event)"
    @open="fetchList"
  >
    <el-table
      v-loading="loading"
      :data="historyList"
      stripe
      style="width: 100%"
      max-height="420"
    >
      <el-table-column label="时间" width="160">
        <template #default="{ row }">{{
          formatDate(row.createTime, 8)
        }}</template>
      </el-table-column>
      <el-table-column label="操作类型" width="90">
        <template #default="{ row }">
          <el-tag :type="opType(row.operationType)" size="small">{{
            row.operationType
          }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column
        prop="operator"
        label="操作人"
        width="110"
        show-overflow-tooltip
      />
      <el-table-column
        prop="itemKey"
        label="配置项"
        min-width="140"
        show-overflow-tooltip
      ></el-table-column>
      <el-table-column prop="versionRange" label="版本范围" min-width="130" />
      <el-table-column
        prop="itemValue"
        label="值"
        min-width="180"
        show-overflow-tooltip
      />
      <el-table-column label="状态" width="70">
        <template #default="{ row }">
          <el-tag :type="row.enabled === 1 ? 'success' : 'info'" size="small">
            {{ row.enabled === 1 ? "启用" : "停用" }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="灰度" width="90">
        <template #default="{ row }">
          <span v-if="row.grayEnabled === 1">{{ row.grayPercent }}%</span>
          <span v-else style="color: var(--el-text-color-secondary)">关</span>
        </template>
      </el-table-column>
      <el-table-column label="白/黑名单" width="100">
        <template #default="{ row }">
          <span>{{ grayCounts(row).white }} / {{ grayCounts(row).black }}</span>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="130" fixed="right">
        <template #default="{ row }">
          <el-button link type="primary" @click="openView(row)">查看</el-button>
          <el-button link type="warning" @click="handleRevert(row)"
            >回溯</el-button
          >
        </template>
      </el-table-column>
      <template #empty>暂无历史记录</template>
    </el-table>

    <div class="tip">
      历史记录为每次变更后的状态；回溯仅影响该配置项，不影响同一配置下的其他配置项。
    </div>

    <el-dialog
      v-model="viewVisible"
      title="历史详情"
      width="700px"
      append-to-body
    >
      <el-descriptions :column="2" border size="small">
        <el-descriptions-item label="域">{{
          viewing?.domain
        }}</el-descriptions-item>
        <el-descriptions-item label="配置项">{{
          viewing?.itemKey
        }}</el-descriptions-item>
        <el-descriptions-item label="版本范围">{{
          viewing?.versionRange
        }}</el-descriptions-item>
        <el-descriptions-item label="值类型">{{
          viewing?.valueType
        }}</el-descriptions-item>
        <el-descriptions-item label="启用">{{
          viewing?.enabled === 1 ? "是" : "否"
        }}</el-descriptions-item>
        <el-descriptions-item label="灰度">
          {{
            viewing && viewing.grayEnabled === 1
              ? `开启 · ${viewing.grayPercent}%`
              : "关闭"
          }}
        </el-descriptions-item>
        <el-descriptions-item label="白/黑名单"
          >{{ grayCounts(viewing).white }} /
          {{ grayCounts(viewing).black }}</el-descriptions-item
        >
        <el-descriptions-item label="操作人">{{
          viewing?.operator
        }}</el-descriptions-item>
        <el-descriptions-item label="时间">{{
          viewing ? formatDate(viewing.createTime, 8) : ""
        }}</el-descriptions-item>
        <el-descriptions-item label="名单明细" :span="2">
          <template v-if="grayUsersOf(viewing).length === 0">无</template>
          <el-tag
            v-for="(u, i) in grayUsersOf(viewing)"
            :key="i"
            :type="u.listType === 'WHITE' ? 'success' : 'danger'"
            size="small"
            style="margin-right: 6px"
          >
            {{ u.listType === "WHITE" ? "白" : "黑" }}:{{ u.username }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item v-if="viewing?.remark" label="备注" :span="2">{{
          viewing.remark
        }}</el-descriptions-item>
      </el-descriptions>
      <codemirror
        :model-value="viewContent"
        :style="{ height: '200px', marginTop: '12px' }"
        :extensions="extensions"
        :disabled="true"
      />
    </el-dialog>
  </el-dialog>
</template>

<script setup lang="ts">
import { ref, computed } from "vue";
import { ElMessage, ElMessageBox } from "element-plus";
import { Codemirror } from "vue-codemirror";
import { json } from "@codemirror/lang-json";
import { oneDark } from "@codemirror/theme-one-dark";
import {
  getItemHistory,
  revertItemToHistory,
  DynamicConfigItemHistory,
} from "@/api/dynamicConfig";
import { formatDate } from "@/utils/index";

const props = defineProps<{
  modelValue: boolean;
  itemId: number | null;
  title?: string;
}>();

const emit = defineEmits<{
  (e: "update:modelValue", v: boolean): void;
  (e: "reverted"): void;
}>();

const historyList = ref<DynamicConfigItemHistory[]>([]);
const loading = ref(false);
const viewVisible = ref(false);
const viewing = ref<DynamicConfigItemHistory | null>(null);

const extensions = [json(), oneDark];

const fetchList = async () => {
  if (!props.itemId) return;
  loading.value = true;
  try {
    const res: any = await getItemHistory(props.itemId);
    historyList.value = res?.data || [];
  } finally {
    loading.value = false;
  }
};

const opType = (t: string) => {
  switch (t) {
    case "CREATE":
      return "success";
    case "UPDATE":
      return "warning";
    case "DELETE":
      return "danger";
    case "REVERT":
      return "primary";
    default:
      return "info";
  }
};

const grayUsersOf = (
  row: DynamicConfigItemHistory | null,
): Array<{ listType: string; username: string }> => {
  try {
    const arr = JSON.parse(row?.grayUsersJson || "[]");
    return Array.isArray(arr) ? arr : [];
  } catch {
    return [];
  }
};

const grayCounts = (row: DynamicConfigItemHistory | null) => {
  const users = grayUsersOf(row);
  return {
    white: users.filter((u) => u.listType === "WHITE").length,
    black: users.filter((u) => u.listType === "BLACK").length,
  };
};

const viewContent = computed(() => {
  if (!viewing.value) return "";
  try {
    return JSON.stringify(JSON.parse(viewing.value.itemValue), null, 2);
  } catch {
    return viewing.value.itemValue;
  }
});

const openView = (row: DynamicConfigItemHistory) => {
  viewing.value = row;
  viewVisible.value = true;
};

const handleRevert = async (row: DynamicConfigItemHistory) => {
  if (!props.itemId) return;
  await ElMessageBox.confirm(
    `确定回溯到 ${formatDate(row.createTime, 8)} 的状态吗？仅影响该配置项，不影响同一配置下的其他配置项。`,
    "回溯确认",
    { type: "warning" },
  );
  await revertItemToHistory(props.itemId, row.id);
  ElMessage.success("回溯成功");
  emit("reverted");
  fetchList();
};
</script>

<style scoped>
.tip {
  margin-top: 12px;
  font-size: 12px;
  color: var(--el-text-color-secondary);
}
</style>
