<template>
  <el-drawer
    :model-value="modelValue"
    @update:model-value="$emit('update:modelValue', $event)"
    title="发送日志请求"
    size="400px"
    destroy-on-close
  >
    <el-form
      ref="formRef"
      :model="form"
      :rules="rules"
      label-width="100px"
      style="padding: 20px"
    >
      <el-form-item label="用户名" prop="username">
        <el-input v-model="form.username" placeholder="请输入用户名" clearable />
      </el-form-item>
      <el-form-item label="过期时间" prop="timeoutMinutes">
        <el-input-number v-model="form.timeoutMinutes" :min="1" :max="1440" />
        <span style="margin-left: 10px">分钟</span>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" :loading="loading" @click="submit"
          >发送</el-button
        >
        <el-button @click="$emit('update:modelValue', false)">取消</el-button>
      </el-form-item>
    </el-form>
  </el-drawer>
</template>

<script lang="ts" setup>
import { ref, reactive, watch } from "vue";
import { ElMessage, type FormInstance } from "element-plus";
import { createLogRequest } from "@/api/logs";

const props = defineProps<{
  modelValue: boolean;
  username?: string;
}>();

const emit = defineEmits<{
  (e: "update:modelValue", value: boolean): void;
  (e: "success"): void;
}>();

const formRef = ref<FormInstance>();
const loading = ref(false);

const form = reactive({
  username: "",
  timeoutMinutes: 120,
});

const rules = {
  username: [{ required: true, message: "请输入用户名", trigger: "blur" }],
};

watch(
  () => props.username,
  (val) => {
    if (val) form.username = val;
  },
  { immediate: true }
);

const submit = async () => {
  if (!formRef.value) return;
  await formRef.value.validate(async (valid) => {
    if (valid) {
      try {
        loading.value = true;
        await createLogRequest(form.username, form.timeoutMinutes);
        ElMessage.success("请求发送成功");
        emit("success");
        emit("update:modelValue", false);
      } catch (err) {
        console.error(err);
      } finally {
        loading.value = false;
      }
    }
  });
};
</script>
