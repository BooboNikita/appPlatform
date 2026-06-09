<template>
  <div class="app-upload">
    <el-card>
      <template #header>
        <div class="card-header">
          <el-button
            @click="goBack"
            icon="ArrowLeft"
            size="small"
            style="margin-right: 12px"
            >返回</el-button
          >
          <span>{{ isEdit ? "编辑应用" : "上传应用" }}</span>
        </div>
      </template>

      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        label-width="120px"
        class="demo-ruleForm"
      >
        <el-form-item label="应用名称" prop="appName">
          <el-input v-model="form.appName" placeholder="请输入应用名称" />
        </el-form-item>

        <el-form-item label="包名" prop="packageName">
          <el-input
            v-model="form.packageName"
            :disabled="isEdit"
            placeholder="例如：com.example.app"
          />
        </el-form-item>

        <el-form-item label="版本号" prop="version">
          <el-input
            v-model="form.version"
            :disabled="isEdit"
            placeholder="例如：1.0.0"
          />
        </el-form-item>

        <el-form-item label="构建号" prop="buildNumber">
          <el-input v-model="form.buildNumber" placeholder="例如：100" />
        </el-form-item>

        <el-form-item label="新特性" prop="features">
          <el-input
            v-model="form.features"
            type="textarea"
            :rows="3"
            placeholder="请描述本次更新的主要内容"
          />
        </el-form-item>

        <el-form-item label="Beta版本" prop="isBeta">
          <el-switch v-model="form.isBeta" />
        </el-form-item>

        <el-form-item v-if="!isEdit" label="应用文件" prop="file" required>
          <el-upload
            class="app-uploader"
            drag
            :auto-upload="false"
            :on-change="handleFileChange"
            :on-remove="handleFileRemove"
            :limit="1"
            :file-list="fileList"
            :before-upload="beforeUpload"
          >
            <el-icon class="el-icon--upload"><upload-filled /></el-icon>
            <div class="el-upload__text">
              将文件拖到此处，或<em>点击上传</em>
            </div>
            <template #tip>
              <div class="el-upload__tip">
                支持APK、IPA等应用文件，最大500MB
              </div>
            </template>
          </el-upload>
        </el-form-item>

        <el-form-item v-if="isEdit" label="应用文件">
          <el-alert
            type="info"
            :closable="false"
            title="提示：编辑模式下无法修改应用文件，如需更换请上传新版本"
          />
        </el-form-item>

        <el-form-item>
          <el-button type="primary" :loading="loading" @click="submitForm">
            {{ isEdit ? "更新" : "提交" }}
          </el-button>
          <el-button @click="resetForm">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script lang="ts" setup>
import { ref, reactive, onMounted } from "vue";
import { useRouter, useRoute } from "vue-router";
import {
  ElMessage,
  type FormInstance,
  type FormRules,
  type UploadProps,
} from "element-plus";
import { UploadFilled, ArrowLeft } from "@element-plus/icons-vue";
import { uploadApp, getAppById, updateApp } from "@/api/app";

const router = useRouter();
const route = useRoute();
const formRef = ref<FormInstance>();
const loading = ref(false);
const fileList = ref<any[]>([]);
const isEdit = ref(false);
const editId = ref<number | null>(null);

const goBack = () => {
  router.back();
};

const form = reactive({
  appName: "监督监管",
  packageName: "szyd",
  version: "",
  buildNumber: "",
  features: "",
  isBeta: false,
  file: null as File | null,
});

const rules = reactive<FormRules>({
  appName: [{ required: true, message: "请输入应用名称", trigger: "blur" }],
  packageName: [
    { required: true, message: "请输入包名", trigger: "blur" },
    {
      pattern: /^[a-z][a-z0-9_]*(\.[a-z0-9_]+)+[0-9a-z_]$/i,
      message: "包名格式不正确，例如：com.example.app",
    },
  ],
  version: [
    { required: true, message: "请输入版本号", trigger: "blur" },
    {
      pattern: /^\d+(\.\d+)*$/,
      message: "版本号格式不正确，例如：1.0.0",
    },
  ],
  buildNumber: [
    { required: true, message: "请输入构建号", trigger: "blur" },
    {
      pattern: /^\d+$/,
      message: "构建号必须是数字",
    },
  ],
  features: [{ required: true, message: "请输入更新内容", trigger: "blur" }],
});

// 获取应用详情
const fetchAppDetail = async (id: number) => {
  try {
    const { data: app } = await getAppById(id);
    if (app) {
      form.appName = app.appName;
      form.packageName = app.packageName;
      form.version = app.version;
      form.buildNumber = app.buildNumber;
      form.features = app.features || "";
      form.isBeta = app.isBeta;
      editId.value = id;
    }
  } catch (error) {
    ElMessage.error("加载应用信息失败");
  }
};

// 文件上传前校验
const beforeUpload: UploadProps["beforeUpload"] = (file) => {
  const isLt500M = file.size / 1024 / 1024 < 500;
  if (!isLt500M) {
    ElMessage.error("文件大小不能超过500MB");
    return false;
  }
  return true;
};

// 文件改变
const handleFileChange: UploadProps["onChange"] = (uploadFile) => {
  form.file = uploadFile.raw as File;
};

// 移除文件
const handleFileRemove = () => {
  form.file = null;
};

// 提交表单
const submitForm = async () => {
  // 编辑模式：不需要文件，只更新信息
  if (!isEdit.value && !form.file) {
    ElMessage.warning("请选择要上传的文件");
    return;
  }

  try {
    loading.value = true;
    const formData = new FormData();

    // 编辑模式下，添加 id 和文件可选
    if (isEdit.value) {
      formData.append("id", String(editId.value));
    } else {
      formData.append("file", form.file!);
    }

    formData.append("appName", form.appName);
    formData.append("packageName", form.packageName);
    formData.append("version", form.version);
    formData.append("buildNumber", form.buildNumber);
    formData.append("features", form.features);
    formData.append("isBeta", String(form.isBeta));

    if (isEdit.value && route.query.id?.length) {
      await updateApp(Number(route.query.id), formData);
    } else {
      await uploadApp(formData);
    }
    ElMessage.success(isEdit.value ? "更新成功" : "上传成功");
    router.push("/app/list");
  } catch (error: any) {
    console.error(isEdit.value ? "更新失败:" : "上传失败:", error);
    const errorMsg = error?.response?.msg || error?.message || "操作失败";
    ElMessage.error(errorMsg);
  } finally {
    loading.value = false;
  }
};

// 重置表单
const resetForm = () => {
  if (isEdit.value) {
    fetchAppDetail(editId.value!);
    return;
  }
  formRef.value?.resetFields();
  fileList.value = [];
  form.file = null;
};

onMounted(() => {
  // 检查是否是编辑模式
  const id = route.query.id;
  if (id) {
    isEdit.value = true;
    fetchAppDetail(Number(id));
  }
});
</script>

<style lang="scss" scoped>
.app-upload {
  .app-uploader {
    width: 100%;

    :deep(.el-upload) {
      width: 100%;
    }

    :deep(.el-upload-dragger) {
      width: 100%;
      padding: 40px 20px;
    }
  }

  .el-form {
    max-width: 800px;
    margin: 0 auto;
  }
}
</style>
