<template>
  <div class="module-edit">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>{{ isEdit ? "编辑模块" : "创建模块" }}</span>
        </div>
      </template>

      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        label-width="120px"
        class="demo-ruleForm"
      >
        <el-form-item label="模块名称" prop="title">
          <el-input v-model="form.title" placeholder="请输入模块名称" />
        </el-form-item>

        <el-form-item label="图标URL" prop="iconUrl">
          <el-input v-model="form.iconUrl" placeholder="请输入图标URL" />
          <div v-if="form.iconUrl" class="icon-preview">
            <img :src="form.iconUrl" :alt="form.title" />
          </div>
        </el-form-item>

        <el-form-item label="目标URL" prop="targetUrl">
          <el-input
            v-model="form.targetUrl"
            placeholder="请输入目标URL或路由路径"
          />
        </el-form-item>

        <el-form-item label="背景颜色" prop="color">
          <el-color-picker v-model="form.color" show-alpha color-format="hex" />
          <span
            v-if="form.color"
            class="color-preview"
            :style="{ backgroundColor: form.color }"
          />
        </el-form-item>

        <el-form-item label="端口号" prop="port">
          <el-input-number v-model="form.port" :min="0" :max="65535" />
        </el-form-item>

        <el-form-item label="路由类型" prop="route">
          <el-select v-model="form.route" placeholder="请选择路由类型">
            <el-option label="开发中" value="under_development" />
            <el-option label="内部页面" value="inner" />
            <el-option label="网页" value="webview" />
          </el-select>
          <span v-if="form.route" class="route-type-hint">
            {{
              form.route === "under_development"
                ? "模块功能正在开发中"
                : form.route === "inner"
                ? "跳转至内部页面"
                : "通过 WebView 加载网页"
            }}
          </span>
        </el-form-item>

        <el-form-item label="排序顺序" prop="sortOrder">
          <el-input-number v-model="form.sortOrder" :min="0" :max="999" />
        </el-form-item>

        <el-form-item label="激活状态" prop="isActive">
          <el-switch v-model="form.isActive" />
          <span style="margin-left: 12px; font-size: 12px; color: #999">
            {{ form.isActive ? "已激活" : "未激活" }}
          </span>
        </el-form-item>

        <el-form-item label="测试时隐藏" prop="hideForTest">
          <el-switch v-model="form.hideForTest" />
          <span style="margin-left: 12px; font-size: 12px; color: #999">
            {{ form.hideForTest ? "已隐藏" : "显示" }}
          </span>
        </el-form-item>

        <el-form-item>
          <el-button type="primary" :loading="loading" @click="submitForm">
            {{ isEdit ? "更新" : "创建" }}
          </el-button>
          <el-button @click="resetForm">重置</el-button>
          <el-button @click="goBack">返回</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script lang="ts" setup>
import { ref, reactive, onMounted } from "vue";
import { useRouter, useRoute } from "vue-router";
import { ElMessage, type FormInstance, type FormRules } from "element-plus";
import {
  getModuleById,
  createModule,
  updateModule,
  type ModuleItem,
} from "@/api/modules";

const router = useRouter();
const route = useRoute();
const formRef = ref<FormInstance>();
const loading = ref(false);
const isEdit = ref(false);
const editId = ref<number | null>(null);

const form = reactive<ModuleItem>({
  title: "",
  iconUrl: "",
  targetUrl: "",
  color: "#409EFF",
  port: 0,
  route: "inner",
  sortOrder: 0,
  isActive: true,
  hideForTest: false,
});

const rules = reactive<FormRules>({
  title: [{ required: true, message: "请输入模块名称", trigger: "blur" }],
  iconUrl: [
    { required: true, message: "请输入图标URL", trigger: "blur" },
    {
      type: "url",
      message: "请输入正确的URL格式",
      trigger: "blur",
    },
  ],
  color: [{ required: true, message: "请选择背景颜色", trigger: "blur" }],
  route: [{ required: true, message: "请选择路由类型", trigger: "blur" }],
  sortOrder: [{ required: true, message: "请输入排序顺序", trigger: "blur" }],
});

// 获取模块详情
const fetchModuleDetail = async (id: number) => {
  try {
    const { data } = await getModuleById(id);
    if (data) {
      form.title = data.title;
      form.iconUrl = data.iconUrl;
      form.targetUrl = data.targetUrl;
      form.color = data.color;
      form.port = data.port;
      form.route = data.route;
      form.sortOrder = data.sortOrder;
      form.isActive = data.isActive;
      form.hideForTest = data.hideForTest;
      editId.value = id;
    }
  } catch (error) {
    ElMessage.error("加载模块信息失败");
    goBack();
  }
};

// 提交表单
const submitForm = async () => {
  if (!formRef.value) return;

  try {
    await formRef.value.validate();
    loading.value = true;

    if (isEdit.value) {
      await updateModule(editId.value!, form);
      ElMessage.success("更新成功");
    } else {
      await createModule(form);
      ElMessage.success("创建成功");
    }

    router.push("/modules");
  } catch (error: any) {
    console.error(isEdit.value ? "更新失败:" : "创建失败:", error);
    const errorMsg = error?.response?.msg || error?.message || "操作失败";
    ElMessage.error(errorMsg);
  } finally {
    loading.value = false;
  }
};

// 重置表单
const resetForm = () => {
  if (isEdit.value) {
    fetchModuleDetail(editId.value!);
    return;
  }
  formRef.value?.resetFields();
  form.isActive = true;
  form.sortOrder = 0;
  form.color = "#409EFF";
  form.port = 0;
  form.route = "inner";
  form.hideForTest = false;
};

// 返回列表
const goBack = () => {
  router.push("/modules");
};

onMounted(() => {
  const id = route.query.id;
  if (id) {
    isEdit.value = true;
    fetchModuleDetail(Number(id));
  }
});
</script>

<style lang="scss" scoped>
.module-edit {
  .el-form {
    max-width: 600px;
  }

  .icon-preview {
    margin-top: 12px;
    max-width: 200px;

    img {
      max-width: 100%;
      max-height: 200px;
      border-radius: 4px;
    }
  }

  .color-preview {
    display: inline-block;
    width: 30px;
    height: 30px;
    border-radius: 4px;
    border: 1px solid #dcdfe6;
    margin-left: 12px;
    vertical-align: middle;
  }

  .route-type-hint {
    display: block;
    margin-top: 8px;
    font-size: 12px;
    color: #909399;
  }
}
</style>
