<template>
  <div class="dynamic-config-container">
    <el-container class="config-layout">
      <!-- 左侧列表 -->
      <el-aside width="350px" class="config-aside">
        <div class="aside-header">
          <span class="title">配置文件</span>
          <el-button type="primary" size="small" @click="handleCreate">
            <el-icon><Plus /></el-icon>新增配置
          </el-button>
        </div>
        <div class="aside-search">
          <el-input
            v-model="searchQuery"
            placeholder="搜索配置文件..."
            clearable
            prefix-icon="Search"
          />
        </div>
        <el-scrollbar>
          <div
            v-for="item in filteredConfigList"
            :key="item.id"
            :class="['config-item', { active: selectedConfig?.id === item.id }]"
            @click="handleSelect(item)"
          >
            <div class="config-item-info">
              <div class="config-filename">
                {{ item.remark || "未命名配置" }}
              </div>
              <div class="config-meta">
                <el-tag
                  size="small"
                  :type="item.env === 'prod' ? 'danger' : 'warning'"
                  >{{ item.env === "prod" ? "生产" : "测试" }}</el-tag
                >
                <el-tag size="small" type="info" style="margin-left: 5px">{{
                  item.versionRange
                }}</el-tag>
                <span class="config-time">{{
                  formatDate(item.updateTime)
                }}</span>
              </div>
            </div>
          </div>
          <el-empty
            v-if="filteredConfigList.length === 0"
            description="暂无配置"
          />
        </el-scrollbar>
      </el-aside>

      <!-- 右侧内容 -->
      <el-main class="config-main">
        <div v-if="selectedConfig" class="editor-container">
          <div class="editor-header">
            <div class="header-left">
              <span class="selected-title">{{
                selectedConfig.remark || "未命名配置"
              }}</span>
              <el-tag
                size="small"
                effect="plain"
                type="success"
                style="margin-left: 10px"
                >ID: {{ selectedConfig.id }}</el-tag
              >
            </div>
            <div class="header-actions">
              <el-button type="danger" plain @click="handleDelete"
                >删除</el-button
              >
              <el-button type="primary" :loading="saving" @click="handleSave"
                >保存修改</el-button
              >
            </div>
          </div>

          <el-form :model="editForm" label-width="100px" class="config-form">
            <el-form-item label="版本范围">
              <el-input
                v-model="editForm.versionRange"
                placeholder="例如: 1.2.0, 1.0.0-2.0.0, 1.5.0+ 或 *"
              />
              <div class="input-tip">
                规则：精确匹配(1.2.0)、范围(1.0.0-2.0.0)、起始(1.5.0+)、通配符(*)
              </div>
            </el-form-item>
            <el-form-item label="环境">
              <el-select
                v-model="editForm.env"
                placeholder="请选择环境"
                style="width: 100%"
              >
                <el-option label="生产环境 (prod)" value="prod" />
                <el-option label="测试环境 (test)" value="test" />
              </el-select>
            </el-form-item>
            <el-form-item label="备注">
              <el-input v-model="editForm.remark" placeholder="配置用途说明" />
            </el-form-item>
            <el-form-item label="配置内容">
              <div class="json-editor-wrapper">
                <codemirror
                  v-model="editForm.content"
                  placeholder="请输入 JSON 配置内容..."
                  :style="{ height: '500px' }"
                  :autofocus="true"
                  :indent-with-tab="true"
                  :tab-size="2"
                  :extensions="extensions"
                />
              </div>
            </el-form-item>
          </el-form>
        </div>
        <el-empty v-else description="请从左侧选择一个配置文件进行查看或编辑" />
      </el-main>
    </el-container>

    <!-- 新增配置对话框 -->
    <el-dialog v-model="createDialogVisible" title="新增动态配置" width="800px">
      <el-form :model="createForm" label-width="100px">
        <el-form-item label="版本范围" required>
          <el-input
            v-model="createForm.versionRange"
            placeholder="例如: 1.2.0, 1.0.0-2.0.0, 1.5.0+ 或 *"
          />
          <div class="input-tip">
            支持：1.2.0 (精确)、1.0.0-2.0.0 (范围)、1.5.0+ (大于等于)、* (所有)
          </div>
        </el-form-item>
        <el-form-item label="环境" required>
          <el-select
            v-model="createForm.env"
            placeholder="请选择环境"
            style="width: 100%"
          >
            <el-option label="生产环境 (prod)" value="prod" />
            <el-option label="测试环境 (test)" value="test" />
          </el-select>
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="createForm.remark" placeholder="配置用途说明" />
        </el-form-item>
        <el-form-item label="配置内容 (configs)" required>
          <div class="json-editor-wrapper">
            <codemirror
              v-model="createForm.configs"
              placeholder='请输入 configs 对象的 JSON 内容，例如: { "theme": "dark" }'
              :style="{ height: '400px' }"
              :indent-with-tab="true"
              :tab-size="2"
              :extensions="extensions"
            />
          </div>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="createDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="creating" @click="submitCreate"
          >提交</el-button
        >
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from "vue";
import { ElMessage, ElMessageBox } from "element-plus";
import { Search, Plus } from "@element-plus/icons-vue";
import moment from "moment";
import { Codemirror } from "vue-codemirror";
import { json } from "@codemirror/lang-json";
import { oneDark } from "@codemirror/theme-one-dark";
import {
  getDynamicConfigList,
  getDynamicConfigContent,
  updateDynamicConfigContent,
  uploadDynamicConfig,
  deleteDynamicConfig,
  type DynamicConfig,
} from "@/api/dynamicConfig";
import { da } from "element-plus/es/locale";

// Codemirror 配置
const extensions = [json(), oneDark];

// 状态变量
const configList = ref<DynamicConfig[]>([]);
const searchQuery = ref("");
const selectedConfig = ref<DynamicConfig | null>(null);
const loading = ref(false);
const saving = ref(false);
const creating = ref(false);
const createDialogVisible = ref(false);

const editForm = ref({
  versionRange: "",
  remark: "",
  env: "prod",
  content: "", // 这里的 content 只包含 configs 对象
});

const createForm = ref({
  versionRange: "*",
  remark: "",
  env: "prod",
  configs: "{\n  \n}",
});

// 计算属性：过滤后的列表
const filteredConfigList = computed(() => {
  if (!searchQuery.value) return configList.value;
  const query = searchQuery.value.toLowerCase();
  return configList.value.filter(
    (item) =>
      (item.remark && item.remark.toLowerCase().includes(query)) ||
      (item.versionRange && item.versionRange.toLowerCase().includes(query))
  );
});

// 格式化日期
const formatDate = (date: string) => {
  if (!date) return "";
  return moment.parseZone(date).format("YYYY-MM-DD HH:mm:ss");
};

// 获取列表
const fetchList = async () => {
  try {
    loading.value = true;
    const res = await getDynamicConfigList();
    configList.value = (res as any).data || [];
  } catch (error) {
    console.error("获取列表失败", error);
  } finally {
    loading.value = false;
  }
};

// 选择配置
const handleSelect = async (item: DynamicConfig) => {
  selectedConfig.value = item;
  editForm.value.versionRange = item.versionRange;
  editForm.value.env = item.env || "prod";
  editForm.value.remark = item.remark || "";

  try {
    const res = await getDynamicConfigContent(item.id);
    try {
      const fullJson = JSON.parse((res as any).data);
      // 提取 env 和 configs
      if (fullJson.metadata) {
        editForm.value.env = fullJson.metadata.env || "production";
      }
      if (fullJson.configs) {
        editForm.value.content = JSON.stringify(fullJson.configs, null, 2);
      } else {
        editForm.value.content = JSON.stringify(fullJson, null, 2);
      }
    } catch (e) {
      editForm.value.content = (res as any).data;
    }
  } catch (error) {
    ElMessage.error("获取配置内容失败");
  }
};

// 保存修改
const handleSave = async () => {
  if (!selectedConfig.value) return;

  try {
    // 校验 JSON
    let configsObj;
    try {
      configsObj = JSON.parse(editForm.value.content);
    } catch (e) {
      return ElMessage.error("JSON 格式错误，请检查内容");
    }

    // 构造完整 JSON
    const fullJson = {
      metadata: {
        configId: selectedConfig.value.id,
        versionRange: editForm.value.versionRange,
        publishTime: moment().format("YYYY-MM-DD HH:mm:ss"),
        env: editForm.value.env,
        remark: editForm.value.remark,
      },
      configs: configsObj,
    };

    saving.value = true;
    await updateDynamicConfigContent(
      selectedConfig.value.id,
      JSON.stringify(fullJson, null, 2),
      editForm.value.versionRange,
      editForm.value.env,
      editForm.value.remark
    );
    ElMessage.success("保存成功");
    fetchList();
  } catch (error) {
    console.error("保存失败", error);
  } finally {
    saving.value = false;
  }
};

// 删除配置
const handleDelete = () => {
  if (!selectedConfig.value) return;

  ElMessageBox.confirm(
    `确定要删除配置 "${
      selectedConfig.value.remark || selectedConfig.value.id
    }" 吗？`,
    "警告",
    {
      confirmButtonText: "确定",
      cancelButtonText: "取消",
      type: "warning",
    }
  ).then(async () => {
    try {
      await deleteDynamicConfig(selectedConfig.value!.id);
      ElMessage.success("删除成功");
      selectedConfig.value = null;
      fetchList();
    } catch (error) {
      console.error("删除失败", error);
    }
  });
};

// 新增配置
const handleCreate = () => {
  createForm.value = {
    versionRange: "*",
    remark: "",
    env: "prod",
    configs: "{\n  \n}",
  };
  createDialogVisible.value = true;
};

const submitCreate = async () => {
  if (!createForm.value.configs) {
    return ElMessage.warning("请输入配置内容");
  }

  try {
    // 校验 JSON
    let configsObj;
    try {
      configsObj = JSON.parse(createForm.value.configs);
    } catch (e) {
      return ElMessage.error("JSON 格式错误，请检查内容");
    }

    creating.value = true;

    // 构造完整 JSON
    const fullJson = {
      metadata: {
        configId: 0,
        versionRange: createForm.value.versionRange,
        publishTime: moment().format("YYYY-MM-DD HH:mm:ss"),
        env: createForm.value.env,
        remark: createForm.value.remark,
      },
      configs: configsObj,
    };

    const formData = new FormData();
    const blob = new Blob([JSON.stringify(fullJson, null, 2)], {
      type: "application/json",
    });
    formData.append("file", blob, "config.json");
    formData.append("versionRange", createForm.value.versionRange);
    formData.append("env", createForm.value.env);
    formData.append("remark", createForm.value.remark);

    await uploadDynamicConfig(formData);
    ElMessage.success("新增成功");
    createDialogVisible.value = false;
    fetchList();
  } catch (error) {
    console.error("新增失败", error);
  } finally {
    creating.value = false;
  }
};

onMounted(() => {
  fetchList();
});
</script>

<style scoped lang="scss">
.dynamic-config-container {
  height: calc(100vh - 100px);
  padding: 20px;
  background-color: #f5f7fa;

  .config-layout {
    height: 100%;
    background: #fff;
    border-radius: 8px;
    box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
    overflow: hidden;
  }

  .json-editor-wrapper {
    width: 100%;
    border: 1px solid #dcdfe6;
    border-radius: 4px;
    overflow: hidden;

    :deep(.cm-editor) {
      font-size: 14px;
    }
  }

  .config-aside {
    border-right: 1px solid #ebeef5;
    display: flex;
    flex-direction: column;

    .aside-header {
      padding: 15px 20px;
      display: flex;
      justify-content: space-between;
      align-items: center;
      border-bottom: 1px solid #ebeef5;

      .title {
        font-size: 16px;
        font-weight: bold;
        color: #303133;
      }
    }

    .aside-search {
      padding: 10px 15px;
    }

    .config-item {
      padding: 15px 20px;
      cursor: pointer;
      transition: all 0.3s;
      border-bottom: 1px solid #f2f6fc;

      &:hover {
        background-color: #f5f7fa;
      }

      &.active {
        background-color: #ecf5ff;
        border-left: 4px solid #409eff;
      }

      .config-filename {
        font-size: 14px;
        color: #303133;
        font-weight: 500;
        margin-bottom: 8px;
        overflow: hidden;
        text-overflow: ellipsis;
        white-space: nowrap;
      }

      .config-meta {
        display: flex;
        justify-content: space-between;
        align-items: center;

        .config-time {
          font-size: 12px;
          color: #909399;
        }
      }
    }
  }

  .config-main {
    padding: 0;
    display: flex;
    flex-direction: column;

    .input-tip {
      font-size: 12px;
      color: #909399;
      line-height: 1.5;
      margin-top: 4px;
    }

    .editor-container {
      display: flex;
      flex-direction: column;
      height: 100%;

      .editor-header {
        padding: 15px 20px;
        display: flex;
        justify-content: space-between;
        align-items: center;
        border-bottom: 1px solid #ebeef5;

        .selected-title {
          font-size: 18px;
          font-weight: bold;
          color: #303133;
        }
      }

      .config-form {
        padding: 20px;
        flex: 1;
        overflow-y: auto;
      }
    }
  }
}
</style>
