<template>
  <div class="dynamic-config-container">
    <el-card shadow="never">
      <!-- 筛选栏 -->
      <div class="filter-bar">
        <el-select
          v-model="filters.env"
          placeholder="环境"
          clearable
          style="width: 130px"
        >
          <el-option label="生产 (prod)" value="prod" />
          <el-option label="测试 (test)" value="test" />
        </el-select>
        <el-input
          v-model="filters.versionRange"
          placeholder="版本范围"
          clearable
          style="width: 150px"
        />
        <el-input
          v-model="filters.domain"
          placeholder="所属域"
          clearable
          style="width: 130px"
        />
        <el-input
          v-model="filters.keyword"
          placeholder="关键字（域/配置项/备注）"
          clearable
          style="width: 200px"
          @keyup.enter="fetchList"
        />
        <el-button type="primary" @click="fetchList">查询</el-button>
        <el-button @click="resetFilters">重置</el-button>
      </div>

      <!-- 工具栏 -->
      <div class="toolbar">
        <el-button type="primary" @click="openUpload"
          ><el-icon><Plus /></el-icon>&nbsp;上传JSON（整包拆解）</el-button
        >
        <el-button type="warning" plain @click="openAddItem"
          ><el-icon><Plus /></el-icon>&nbsp;添加配置项</el-button
        >
        <el-button type="success" plain @click="openPreview"
          >下发JSON预览</el-button
        >
        <el-button
          type="info"
          plain
          :disabled="!selectedRow"
          @click="openHistoryDrawer"
          >整包历史</el-button
        >
        <el-button @click="fetchList">刷新</el-button>
      </div>

      <!-- 配置项列表 -->
      <el-table
        v-loading="loading"
        :data="itemList"
        stripe
        highlight-current-row
        style="width: 100%"
        @current-change="(row: DynamicConfigItem | null) => (selectedRow = row)"
      >
        <el-table-column prop="id" label="ID" width="70" />
        <el-table-column
          prop="versionRange"
          label="版本范围"
          width="120"
          show-overflow-tooltip
        />
        <el-table-column label="环境" width="80">
          <template #default="{ row }">
            <el-tag
              :type="row.env === 'prod' ? 'danger' : 'warning'"
              size="small"
              >{{ row.env }}</el-tag
            >
          </template>
        </el-table-column>
        <el-table-column
          prop="domain"
          label="所属域"
          width="120"
          show-overflow-tooltip
        />
        <el-table-column prop="itemKey" label="配置项" min-width="220" />
        <el-table-column label="当前值" min-width="200" show-overflow-tooltip>
          <template #default="{ row }">
            <span :class="valueClass(row.valueType)">{{ row.itemValue }}</span>
          </template>
        </el-table-column>
        <el-table-column label="更新时间" width="160">
          <template #default="{ row }">{{
            formatDate(row.updateTime, 8)
          }}</template>
        </el-table-column>
        <el-table-column label="操作" width="120" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="openDrawer(row)"
              >编辑</el-button
            >
            <el-button link type="danger" @click="handleDeleteItem(row)"
              >删除</el-button
            >
          </template>
        </el-table-column>
        <template #empty>暂无配置项，可上传整包 JSON 或单独添加配置项</template>
      </el-table>
    </el-card>

    <!-- 添加配置项对话框 -->
    <el-dialog v-model="addItemVisible" title="添加配置项" width="720px">
      <el-form label-width="110px">
        <el-form-item label="环境" required>
          <el-select v-model="addItemForm.env" style="width: 300px">
            <el-option label="生产 (prod)" value="prod" />
            <el-option label="测试 (test)" value="test" />
          </el-select>
        </el-form-item>
        <el-form-item label="版本范围" required>
          <el-autocomplete
            v-model="addItemForm.versionRange"
            :fetch-suggestions="queryVersionRange"
            clearable
            placeholder="如 1.0.0-2.0.0 / 1.5.0 / *，可直接输入新范围"
            style="width: 300px"
          />
          <div class="input-tip">
            下发时按客户端版本匹配该范围；对应配置不存在时会自动创建，无需先上传整包
            JSON
          </div>
        </el-form-item>
        <el-form-item label="所属域" required>
          <el-select
            v-model="addItemForm.domain"
            filterable
            allow-create
            default-first-option
            placeholder="选择已有域或输入新域"
            style="width: 300px"
          >
            <el-option
              v-for="d in domainOptions"
              :key="d"
              :label="d"
              :value="d"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="配置项" required>
          <el-input
            v-model="addItemForm.itemKey"
            placeholder="如 ai_entry"
            style="width: 300px"
          />
        </el-form-item>
        <el-form-item label="配置值" required>
          <codemirror
            v-model="addItemForm.itemValue"
            :style="{ height: '180px', width: '100%' }"
            :extensions="extensions"
          />
          <div class="input-tip">
            支持 JSON 值：true/false、数字、字符串、对象、数组
          </div>
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="addItemForm.remark" placeholder="备注（可选）" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="addItemVisible = false">取消</el-button>
        <el-button type="primary" :loading="addingItem" @click="handleAddItem"
          >确定</el-button
        >
      </template>
    </el-dialog>

    <!-- 上传整包 JSON 对话框 -->
    <el-dialog
      v-model="uploadVisible"
      title="上传动态配置（整包 JSON 拆解）"
      width="820px"
    >
      <el-form label-width="110px">
        <el-form-item label="版本范围" required>
          <el-input
            v-model="uploadForm.versionRange"
            placeholder="如 1.0.0-2.0.0 / 1.5.0 / *"
            style="width: 300px"
          />
        </el-form-item>
        <el-form-item label="环境" required>
          <el-select v-model="uploadForm.env" style="width: 300px">
            <el-option label="生产 (prod)" value="prod" />
            <el-option label="测试 (test)" value="test" />
          </el-select>
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="uploadForm.remark" placeholder="备注（可选）" />
        </el-form-item>
        <el-form-item label="配置JSON" required>
          <codemirror
            v-model="uploadForm.content"
            :style="{ height: '260px', width: '100%' }"
            :extensions="extensions"
          />
          <div class="input-tip">
            支持两种格式：{ 域: { 配置项: 值 } } 或 { metadata: {...}, configs:
            { 域: { 配置项: 值 } } }。 相同 (环境+版本范围)
            再次上传视为更新，已存在的配置项会保留其灰度配置。
          </div>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="uploadVisible = false">取消</el-button>
        <el-button type="primary" :loading="uploading" @click="handleUpload"
          >上传</el-button
        >
      </template>
    </el-dialog>

    <!-- 下发 JSON 预览对话框 -->
    <el-dialog v-model="previewVisible" title="下发 JSON 预览" width="820px">
      <div class="preview-bar">
        <el-input
          v-model="previewForm.version"
          placeholder="客户端版本号，如 1.0.0"
          style="width: 180px"
        />
        <el-select v-model="previewForm.env" style="width: 140px">
          <el-option label="生产 (prod)" value="prod" />
          <el-option label="测试 (test)" value="test" />
        </el-select>
        <el-input
          v-model="previewForm.username"
          placeholder="用户名（灰度判定，可留空）"
          clearable
          style="width: 220px"
        />
        <el-button type="primary" :loading="previewing" @click="handlePreview"
          >预览</el-button
        >
      </div>
      <div class="input-tip" style="margin: 8px 0 12px">
        用户名留空时，灰度中的配置项不会下发；预览结果为只读。
      </div>
      <codemirror
        v-model="previewContent"
        :style="{ height: '340px', width: '100%' }"
        :extensions="extensions"
        :disabled="true"
      />
    </el-dialog>

    <!-- 配置项统一抽屉（值编辑/状态/灰度/名单/历史） -->
    <ItemDrawer
      v-model="drawerVisible"
      :item="drawerItem"
      @changed="fetchList"
    />

    <!-- 整包历史抽屉 -->
    <ConfigHistoryDrawer
      v-model="historyDrawerVisible"
      :config-id="selectedRow ? selectedRow.configId : null"
      :current-content="drawerCurrentContent"
      :current-metadata="drawerCurrentMetadata"
    />
  </div>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref, computed } from "vue";
import { ElMessage, ElMessageBox } from "element-plus";
import { Plus } from "@element-plus/icons-vue";
import { Codemirror } from "vue-codemirror";
import { json } from "@codemirror/lang-json";
import { oneDark } from "@codemirror/theme-one-dark";
import {
  getDynamicConfigItems,
  getDynamicConfigList,
  addDynamicConfigItem,
  deleteDynamicConfigItem,
  uploadDynamicConfig,
  previewDynamicConfig,
  getDynamicConfigContent,
  DynamicConfig,
  DynamicConfigItem,
} from "@/api/dynamicConfig";
import ItemDrawer from "@/components/ItemDrawer.vue";
import ConfigHistoryDrawer from "@/components/ConfigHistoryDrawer.vue";
import { formatDate } from "../../utils/index";

const extensions = [json(), oneDark];

// ==================== 列表 ====================
const filters = reactive({
  env: "",
  versionRange: "",
  domain: "",
  keyword: "",
});
const itemList = ref<DynamicConfigItem[]>([]);
const loading = ref(false);
const selectedRow = ref<DynamicConfigItem | null>(null);

const fetchList = async () => {
  loading.value = true;
  try {
    const res: any = await getDynamicConfigItems({
      env: filters.env || undefined,
      versionRange: filters.versionRange || undefined,
      domain: filters.domain || undefined,
      keyword: filters.keyword || undefined,
    });
    itemList.value = res?.data || [];
  } finally {
    loading.value = false;
  }
};

const resetFilters = () => {
  filters.env = "";
  filters.versionRange = "";
  filters.domain = "";
  filters.keyword = "";
  fetchList();
};

const valueClass = (valueType: string) => {
  switch (valueType) {
    case "BOOLEAN":
      return "vt-boolean";
    case "NUMBER":
      return "vt-number";
    case "STRING":
      return "vt-string";
    default:
      return "vt-json";
  }
};

// ==================== 删除配置项 ====================
const handleDeleteItem = async (row: DynamicConfigItem) => {
  await ElMessageBox.confirm(
    `确定删除配置项「${row.domain}.${row.itemKey}」吗？其灰度名单将一并删除。`,
    "提示",
    { type: "warning" },
  );
  await deleteDynamicConfigItem(row.id);
  ElMessage.success("删除成功");
  fetchList();
};

// ==================== 配置项抽屉 ====================
const drawerVisible = ref(false);
const drawerItem = ref<DynamicConfigItem | null>(null);

const openDrawer = (row: DynamicConfigItem) => {
  drawerItem.value = row;
  drawerVisible.value = true;
};

// ==================== 添加配置项 ====================
const addItemVisible = ref(false);
const addingItem = ref(false);
const configOptions = ref<DynamicConfig[]>([]);
const domainOptions = ref<string[]>([]);
const addItemForm = reactive({
  env: "prod",
  versionRange: "",
  domain: "",
  itemKey: "",
  itemValue: "",
  remark: "",
});

// 已有配置的版本范围（按当前环境过滤）作为联想建议，可直接输入新范围
const versionOptions = computed(() =>
  Array.from(
    new Set(
      configOptions.value
        .filter((c) => c.env === addItemForm.env)
        .map((c) => c.versionRange),
    ),
  ),
);

const queryVersionRange = (
  queryString: string,
  cb: (arr: { value: string }[]) => void,
) => {
  const all = versionOptions.value.map((v) => ({ value: v }));
  const results = queryString
    ? all.filter((o) =>
        o.value.toLowerCase().includes(queryString.toLowerCase()),
      )
    : all;
  cb(results);
};

const openAddItem = async () => {
  addItemForm.env = "prod";
  addItemForm.versionRange = "";
  addItemForm.domain = "";
  addItemForm.itemKey = "";
  addItemForm.itemValue = "";
  addItemForm.remark = "";
  addItemVisible.value = true;
  try {
    const [cfgRes, itemRes]: any[] = await Promise.all([
      getDynamicConfigList(),
      getDynamicConfigItems({}),
    ]);
    configOptions.value = cfgRes?.data || [];
    domainOptions.value = Array.from(
      new Set<string>(
        ((itemRes?.data || []) as DynamicConfigItem[]).map((i) => i.domain),
      ),
    );
  } catch {
    configOptions.value = [];
    domainOptions.value = [];
  }
};

const handleAddItem = async () => {
  if (!addItemForm.versionRange.trim()) {
    ElMessage.warning("请填写版本范围");
    return;
  }
  if (!addItemForm.domain.trim()) {
    ElMessage.warning("请输入所属域");
    return;
  }
  if (!addItemForm.itemKey.trim()) {
    ElMessage.warning("请输入配置项名称");
    return;
  }
  let parsed: any;
  try {
    parsed = JSON.parse(addItemForm.itemValue);
  } catch {
    ElMessage.error("配置值不是合法 JSON");
    return;
  }
  addingItem.value = true;
  try {
    await addDynamicConfigItem({
      env: addItemForm.env,
      versionRange: addItemForm.versionRange.trim(),
      domain: addItemForm.domain.trim(),
      itemKey: addItemForm.itemKey.trim(),
      itemValue: JSON.stringify(parsed),
      remark: addItemForm.remark || undefined,
    });
    ElMessage.success("添加成功");
    addItemVisible.value = false;
    fetchList();
  } finally {
    addingItem.value = false;
  }
};

// ==================== 上传整包 JSON ====================
const uploadVisible = ref(false);
const uploading = ref(false);
const uploadForm = reactive({
  versionRange: "",
  env: "prod",
  remark: "",
  content: "",
});

const openUpload = () => {
  uploadForm.versionRange = "";
  uploadForm.env = "prod";
  uploadForm.remark = "";
  uploadForm.content = "";
  uploadVisible.value = true;
};

const handleUpload = async () => {
  if (!uploadForm.versionRange.trim()) {
    ElMessage.warning("请输入版本范围");
    return;
  }
  if (!uploadForm.content.trim()) {
    ElMessage.warning("请输入配置 JSON");
    return;
  }
  try {
    JSON.parse(uploadForm.content);
  } catch {
    ElMessage.error("配置 JSON 格式不正确");
    return;
  }
  uploading.value = true;
  try {
    const formData = new FormData();
    formData.append(
      "file",
      new Blob([uploadForm.content], { type: "application/json" }),
      "config.json",
    );
    formData.append("versionRange", uploadForm.versionRange.trim());
    formData.append("env", uploadForm.env);
    if (uploadForm.remark) formData.append("remark", uploadForm.remark);
    await uploadDynamicConfig(formData);
    ElMessage.success("上传成功，已拆解为配置项");
    uploadVisible.value = false;
    fetchList();
  } finally {
    uploading.value = false;
  }
};

// ==================== 下发 JSON 预览 ====================
const previewVisible = ref(false);
const previewing = ref(false);
const previewForm = reactive({ version: "", env: "prod", username: "" });
const previewContent = ref("");

const openPreview = () => {
  previewForm.version = "";
  previewForm.env = "prod";
  previewForm.username = "";
  previewContent.value = "";
  previewVisible.value = true;
};

const handlePreview = async () => {
  if (!previewForm.version.trim()) {
    ElMessage.warning("请输入客户端版本号");
    return;
  }
  previewing.value = true;
  try {
    const res: any = await previewDynamicConfig(
      previewForm.version.trim(),
      previewForm.env,
      previewForm.username.trim() || undefined,
    );
    const data = res?.data;
    previewContent.value = data
      ? JSON.stringify(data, null, 2)
      : "（未匹配到该版本与环境对应的配置）";
  } finally {
    previewing.value = false;
  }
};

// ==================== 整包历史抽屉 ====================
const historyDrawerVisible = ref(false);
const drawerCurrentContent = ref("");
const drawerCurrentMetadata = ref<any>(null);

const openHistoryDrawer = async () => {
  if (!selectedRow.value) return;
  try {
    const res: any = await getDynamicConfigContent(selectedRow.value.configId);
    const full = res?.data;
    drawerCurrentContent.value = full?.configs
      ? JSON.stringify(full.configs, null, 2)
      : "";
    drawerCurrentMetadata.value = full?.metadata || null;
  } catch {
    drawerCurrentContent.value = "";
    drawerCurrentMetadata.value = null;
  }
  historyDrawerVisible.value = true;
};

onMounted(() => {
  fetchList();
});
</script>

<style scoped>
.dynamic-config-container {
  padding: 16px;
}
.filter-bar {
  display: flex;
  gap: 10px;
  margin-bottom: 12px;
  flex-wrap: wrap;
}
.toolbar {
  display: flex;
  gap: 10px;
  margin-bottom: 12px;
}
.input-tip {
  font-size: 12px;
  color: var(--el-text-color-secondary);
  margin-top: 4px;
}
.preview-bar {
  display: flex;
  gap: 10px;
  align-items: center;
}
.vt-boolean {
  color: var(--el-color-success);
}
.vt-number {
  color: var(--el-color-warning);
}
.vt-string {
  color: var(--el-text-color-regular);
}
.vt-json {
  color: var(--el-color-primary);
}
</style>
