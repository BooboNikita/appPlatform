<template>
  <div class="store-link-list">
    <div class="action-container">
      <el-button type="primary" @click="handleAdd">
        <el-icon><Plus /></el-icon>
        新增配置
      </el-button>
      <div class="status-switch">
        <el-switch
          v-model="appUpdateStatus"
          @change="handleAppUpdateStatusChange"
        />
        <span class="switch-text">{{
          appUpdateStatus ? "应用更新可用" : "应用更新禁用"
        }}</span>
      </div>
    </div>

    <el-table v-loading="loading" :data="configList" border style="width: 100%">
      <el-table-column prop="deviceBrand" label="设备品牌" width="150" />
      <el-table-column
        prop="linkTemplate"
        label="应用商店链接模板"
        min-width="300"
      />
      <el-table-column prop="remark" label="备注" width="200" />
      <el-table-column prop="createTime" label="创建时间" width="180">
        <template #default="{ row }">
          {{ formatDate(row.createTime) }}
        </template>
      </el-table-column>
      <el-table-column label="操作" width="180" fixed="right">
        <template #default="{ row }">
          <el-button type="primary" size="small" @click="handleEdit(row)">
            编辑
          </el-button>
          <el-button type="danger" size="small" @click="handleDelete(row)">
            删除
          </el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- 编辑/新增对话框 -->
    <el-dialog
      :title="dialogTitle"
      v-model="dialogVisible"
      width="500px"
      @close="resetForm"
    >
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="设备品牌" prop="deviceBrand">
          <el-input v-model="form.deviceBrand" placeholder="例如：xiaomi" />
        </el-form-item>
        <el-form-item label="链接模板" prop="linkTemplate">
          <el-input
            v-model="form.linkTemplate"
            type="textarea"
            placeholder="例如：market://details?id={packageName}"
          />
        </el-form-item>
        <el-form-item label="排序权重" prop="sortOrder">
          <el-input-number v-model="form.sortOrder" :min="0" :max="999" />
        </el-form-item>
        <el-form-item label="状态" prop="enabled">
          <el-switch
            v-model="form.enabled"
            :active-value="1"
            :inactive-value="0"
          />
        </el-form-item>
        <el-form-item label="备注" prop="remark">
          <el-input v-model="form.remark" type="textarea" />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="submitForm">确定</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script lang="ts" setup>
import { ref, reactive, onMounted } from "vue";
import { ElMessage, ElMessageBox } from "element-plus";
import { Plus } from "@element-plus/icons-vue";
import {
  getAllConfigs,
  createConfig,
  updateConfig,
  deleteConfig,
  updateConfigEnabled,
  getAppUpdateStatus,
  setAppUpdateStatus,
  StoreLinkConfig,
  StoreLinkConfigDto,
} from "@/api/storeLinkConfig";
import { formatDate } from "@/utils/index";

const loading = ref(false);
const configList = ref<StoreLinkConfig[]>([]);
const appUpdateStatus = ref(false);

const dialogVisible = ref(false);
const dialogTitle = ref("");
const formRef = ref();

const form = reactive<StoreLinkConfigDto>({
  deviceBrand: "",
  linkTemplate: "",
  enabled: 1,
  sortOrder: 0,
  remark: "",
});

const currentId = ref<number | null>(null);

const rules = {
  deviceBrand: [{ required: true, message: "请输入设备品牌", trigger: "blur" }],
  linkTemplate: [
    { required: true, message: "请输入链接模板", trigger: "blur" },
  ],
};

// 获取配置列表
const fetchConfigs = async () => {
  try {
    loading.value = true;
    const { data } = await getAllConfigs();
    configList.value = data;
  } catch (error) {
    console.error("获取配置列表失败:", error);
  } finally {
    loading.value = false;
  }
};

// 获取应用更新总开关状态
const fetchAppUpdateStatus = async () => {
  try {
    const { data } = await getAppUpdateStatus();
    appUpdateStatus.value = data.appUpdateTotalEnabled;
  } catch (error) {
    console.error("获取应用更新状态失败:", error);
  }
};

// 切换应用更新总开关
const handleAppUpdateStatusChange = async (val: string | number | boolean) => {
  const status = val as boolean;
  try {
    await setAppUpdateStatus(status);
    ElMessage.success(`应用更新已${status ? "开启" : "关闭"}`);
  } catch (error) {
    console.error("设置应用更新状态失败:", error);
    appUpdateStatus.value = !status; // 恢复
    // ElMessage.error("设置应用更新状态失败");
  }
};

// 切换配置启用状态
const handleEnabledChange = async (row: StoreLinkConfig) => {
  try {
    await updateConfigEnabled(row.id, row.enabled);
    ElMessage.success("状态已更新");
  } catch (error) {
    row.enabled = row.enabled === 1 ? 0 : 1; // 恢复
    console.error("更新状态失败:", error);
  }
};

// 新增
const handleAdd = () => {
  dialogTitle.value = "新增配置";
  currentId.value = null;
  form.deviceBrand = "";
  form.linkTemplate = "";
  form.enabled = 1;
  form.sortOrder = 0;
  form.remark = "";
  dialogVisible.value = true;
};

// 编辑
const handleEdit = (row: StoreLinkConfig) => {
  dialogTitle.value = "编辑配置";
  currentId.value = row.id;
  form.deviceBrand = row.deviceBrand;
  form.linkTemplate = row.linkTemplate;
  form.enabled = row.enabled;
  form.sortOrder = row.sortOrder;
  form.remark = row.remark;
  dialogVisible.value = true;
};

// 删除
const handleDelete = (row: StoreLinkConfig) => {
  ElMessageBox.confirm("确认删除该配置吗？", "提示", {
    type: "warning",
  })
    .then(async () => {
      await deleteConfig(row.id);
      ElMessage.success("删除成功");
      fetchConfigs();
    })
    .catch(() => {});
};

// 提交表单
const submitForm = async () => {
  if (!formRef.value) return;
  await formRef.value.validate(async (valid: boolean) => {
    if (valid) {
      try {
        if (currentId.value) {
          await updateConfig(currentId.value, form);
          ElMessage.success("更新成功");
        } else {
          await createConfig(form);
          ElMessage.success("创建成功");
        }
        dialogVisible.value = false;
        fetchConfigs();
      } catch (error) {
        console.error("提交失败:", error);
      }
    }
  });
};

const resetForm = () => {
  if (formRef.value) {
    formRef.value.resetFields();
  }
};

onMounted(() => {
  fetchConfigs();
  fetchAppUpdateStatus();
});
</script>

<style scoped lang="scss">
.store-link-list {
  background: #fff;
  padding: 20px;
  border-radius: 4px;

  .action-container {
    margin-bottom: 20px;
    display: flex;
    align-items: center;
    gap: 20px;

    .status-switch {
      display: flex;
      align-items: center;
      gap: 10px;

      .switch-text {
        font-size: 14px;
        color: #606266;
      }
    }
  }
}
</style>
