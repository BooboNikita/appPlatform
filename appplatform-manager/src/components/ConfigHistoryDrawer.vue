<template>
  <el-drawer
    v-model="visible"
    title="版本历史"
    size="50%"
    :destroy-on-close="true"
    @open="fetchHistory"
  >
    <el-table
      :data="historyList"
      v-loading="loading"
      stripe
      style="width: 100%"
    >
      <el-table-column prop="id" label="ID" width="60" />
      <el-table-column prop="versionRange" label="版本范围" width="120" />
      <el-table-column prop="env" label="环境" width="80">
        <template #default="{ row }">
          <el-tag
            :type="row.env === 'prod' ? 'danger' : 'warning'"
            size="small"
          >
            {{ row.env }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="remark" label="备注" show-overflow-tooltip />
      <el-table-column prop="createTime" label="创建时间" width="160">
        <template #default="{ row }">
          {{ formatDate(row.createTime, 8) }}
        </template>
      </el-table-column>
      <el-table-column label="操作" width="150" fixed="right">
        <template #default="{ row }">
          <el-button link type="primary" @click="handleView(row)"
            >查看</el-button
          >
          <el-button link type="danger" @click="handleRevertClick(row)"
            >回溯</el-button
          >
        </template>
      </el-table-column>
    </el-table>

    <!-- 查看内容对话框 -->
    <el-dialog
      v-model="viewDialogVisible"
      :title="`历史版本内容 (ID: ${viewingHistory?.id})`"
      width="70%"
      append-to-body
    >
      <div
        class="view-metadata"
        v-if="viewMetadata && Object.keys(viewMetadata).length > 0"
      >
        <el-descriptions title="配置元数据" :column="2" border size="small">
          <el-descriptions-item label="版本范围">{{
            viewMetadata.versionRange || "-"
          }}</el-descriptions-item>
          <el-descriptions-item label="环境">
            <el-tag
              :type="viewMetadata.env === 'prod' ? 'danger' : 'warning'"
              size="small"
            >
              {{ viewMetadata.env || "-" }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="备注">{{
            viewMetadata.remark || "-"
          }}</el-descriptions-item>
          <el-descriptions-item label="发布时间">{{
            viewMetadata.publishTime || "-"
          }}</el-descriptions-item>
        </el-descriptions>
      </div>
      <div class="history-content-editor">
        <div
          class="editor-label"
          style="margin: 10px 0 5px; font-weight: bold; font-size: 14px"
        >
          配置内容 (configs)
        </div>
        <codemirror
          v-model="viewContent"
          :style="{ height: '500px' }"
          :extensions="extensions"
          :disabled="true"
        />
      </div>
      <template #footer>
        <el-button @click="viewDialogVisible = false">关闭</el-button>
        <el-button type="danger" @click="handleRevertFromView"
          >从此版本回溯</el-button
        >
      </template>
    </el-dialog>

    <!-- 回溯确认对话框 -->
    <DiffConfirmDialog
      v-model="diffVisible"
      :title="diffTitle"
      :meta-changes="diffMetaChanges"
      :before-text="currentContent"
      :after-text="revertTargetContent"
      confirm-text="确认回溯"
      cancel-text="取消"
      @confirm="executeRevert"
      @cancel="diffVisible = false"
    />
  </el-drawer>
</template>

<script setup lang="ts">
import { ref, computed } from "vue";
import { ElMessage } from "element-plus";
import { Codemirror } from "vue-codemirror";
import { json } from "@codemirror/lang-json";
import { oneDark } from "@codemirror/theme-one-dark";
import {
  getConfigHistory,
  getHistoryContent,
  revertToHistory,
  type DynamicConfigHistory,
} from "@/api/dynamicConfig";
import DiffConfirmDialog from "@/components/DiffConfirmDialog.vue";
import moment from "moment";
import { formatDate } from "@/utils/index";

const props = defineProps<{
  modelValue: boolean;
  configId: number | null;
  currentContent: string;
  currentMetadata?: {
    versionRange: string;
    env: string;
    remark: string;
  };
}>();

const emit = defineEmits(["update:modelValue", "revert-success"]);

const visible = computed({
  get: () => props.modelValue,
  set: (val) => emit("update:modelValue", val),
});

const loading = ref(false);
const historyList = ref<DynamicConfigHistory[]>([]);

const viewDialogVisible = ref(false);
const viewingHistory = ref<DynamicConfigHistory | null>(null);
const viewContent = ref("");
const viewMetadata = ref<any>({});

const diffVisible = ref(false);
const diffTitle = ref("");
const diffMetaChanges = ref<string[]>([]);
const revertTargetContent = ref("");
const revertTargetId = ref<number | null>(null);

const extensions = [json(), oneDark];

const fetchHistory = async () => {
  if (!props.configId) return;
  try {
    loading.value = true;
    const res = await getConfigHistory(props.configId);
    historyList.value = (res as any).data || [];
  } catch (error) {
    console.error("获取历史记录失败", error);
    ElMessage.error("获取历史记录失败");
  } finally {
    loading.value = false;
  }
};

const fetchContent = async (historyId: number) => {
  try {
    const res = await getHistoryContent(historyId);
    return (res as any).data;
  } catch (error) {
    console.error("获取历史内容失败", error);
    ElMessage.error("获取历史内容失败");
    return null;
  }
};

const handleView = async (row: DynamicConfigHistory) => {
  const data = await fetchContent(row.id);
  if (!data) return;

  let fullJson = data;
  if (typeof data === "string") {
    try {
      fullJson = JSON.parse(data);
    } catch (e) {
      fullJson = { configs: data };
    }
  }

  if (fullJson && fullJson.configs) {
    viewContent.value = JSON.stringify(fullJson.configs, null, 2);
    viewMetadata.value = fullJson.metadata || {};
  } else {
    viewContent.value =
      typeof data === "string" ? data : JSON.stringify(data, null, 2);
    viewMetadata.value = {};
  }

  viewingHistory.value = row;
  viewDialogVisible.value = true;
};

const handleRevertClick = async (row: DynamicConfigHistory) => {
  const data = await fetchContent(row.id);
  if (!data) return;

  let fullJson = data;
  if (typeof data === "string") {
    try {
      fullJson = JSON.parse(data);
    } catch (e) {
      fullJson = { configs: data };
    }
  }

  if (fullJson && fullJson.configs) {
    revertTargetContent.value = JSON.stringify(fullJson.configs, null, 2);
  } else {
    revertTargetContent.value =
      typeof data === "string" ? data : JSON.stringify(data, null, 2);
  }

  revertTargetId.value = row.id;
  diffTitle.value = `确认回溯到版本 (ID: ${row.id})`;

  // 计算元数据变更
  diffMetaChanges.value = [];
  if (props.currentMetadata) {
    const historyMeta = fullJson?.metadata || {};
    // historyMeta 可能包含 versionRange, env, remark
    // 如果 historyMeta 为空，说明是旧数据或者解析失败，视情况显示

    // 版本范围
    const histRange = historyMeta.versionRange || row.versionRange || "";
    if (props.currentMetadata.versionRange !== histRange) {
      diffMetaChanges.value.push(
        `版本范围: ${props.currentMetadata.versionRange} -> ${histRange}`,
      );
    }
    // 环境
    const histEnv = historyMeta.env || row.env || "";
    if (props.currentMetadata.env !== histEnv) {
      diffMetaChanges.value.push(
        `环境: ${props.currentMetadata.env} -> ${histEnv}`,
      );
    }
    // 备注
    const histRemark = historyMeta.remark || row.remark || "";
    if (props.currentMetadata.remark !== histRemark) {
      diffMetaChanges.value.push(
        `备注: ${props.currentMetadata.remark || "(空)"} -> ${histRemark || "(空)"}`,
      );
    }
  }

  diffVisible.value = true;
};

const handleRevertFromView = () => {
  if (viewingHistory.value) {
    revertTargetContent.value = viewContent.value;
    revertTargetId.value = viewingHistory.value.id;
    diffTitle.value = `确认回溯到版本 (ID: ${viewingHistory.value.id})`;
    viewDialogVisible.value = false;

    // 计算元数据变更 (复用 viewMetadata)
    diffMetaChanges.value = [];
    if (props.currentMetadata) {
      const historyMeta = viewMetadata.value || {};
      // 尝试从 viewingHistory row 中获取 fallback
      const row = viewingHistory.value;

      const histRange = historyMeta.versionRange || row.versionRange || "";
      if (props.currentMetadata.versionRange !== histRange) {
        diffMetaChanges.value.push(
          `版本范围: ${props.currentMetadata.versionRange} -> ${histRange}`,
        );
      }

      const histEnv = historyMeta.env || row.env || "";
      if (props.currentMetadata.env !== histEnv) {
        diffMetaChanges.value.push(
          `环境: ${props.currentMetadata.env} -> ${histEnv}`,
        );
      }

      const histRemark = historyMeta.remark || row.remark || "";
      if (props.currentMetadata.remark !== histRemark) {
        diffMetaChanges.value.push(
          `备注: ${props.currentMetadata.remark || "(空)"} -> ${histRemark || "(空)"}`,
        );
      }
    }

    diffVisible.value = true;
  }
};

const executeRevert = async () => {
  if (!props.configId || !revertTargetId.value) return;
  try {
    await revertToHistory(props.configId, revertTargetId.value);
    ElMessage.success("回溯成功");
    diffVisible.value = false;
    visible.value = false;
    emit("revert-success");
  } catch (error) {
    console.error("回溯失败", error);
    ElMessage.error("回溯失败");
  }
};
</script>

<style scoped>
.history-content-editor {
  border: 1px solid #dcdfe6;
}
</style>
