<template>
  <div class="app-container">
    <div class="filter-container">
      <el-button type="primary" @click="handleAdd">新增配置</el-button>
      <el-button type="success" @click="fetchData">刷新</el-button>
    </div>

    <el-table
      v-loading="listLoading"
      :data="list"
      element-loading-text="Loading"
      border
      fit
      highlight-current-row
    >
      <el-table-column label="部门ID" prop="deptId" align="center">
      </el-table-column>
      <el-table-column label="组织名称" prop="name" align="center">
      </el-table-column>
      <el-table-column label="封面图" align="center">
        <template #default="scope">
          <div v-if="scope.row.coverImage">
            <el-image
              v-for="(img, idx) in splitImageUrls(scope.row.coverImage)"
              :key="idx"
              :src="img"
              fit="cover"
              :preview-src-list="splitImageUrls(scope.row.coverImage)"
              style="width: 50px; height: 50px; margin-right: 8px"
              :preview-teleported="true"
            />
          </div>
          <span v-else>无</span>
        </template>
      </el-table-column>
      <el-table-column label="截止时间" prop="deadline" align="center">
      </el-table-column>
      <el-table-column label="操作" align="center" width="200">
        <template #default="scope">
          <el-button size="small" @click="handleEdit(scope.row)"
            >编辑</el-button
          >
          <el-button size="small" type="danger" @click="handleDelete(scope.row)"
            >删除</el-button
          >
        </template>
      </el-table-column>
    </el-table>

    <el-dialog :title="dialogTitle" v-model="dialogVisible" width="500px">
      <el-form
        ref="dataFormRef"
        :model="temp"
        :rules="rules"
        label-position="left"
        label-width="100px"
      >
        <el-form-item label="部门ID" prop="deptId">
          <el-input
            v-model="temp.deptId"
            :disabled="dialogStatus === 'update'"
            placeholder="请输入部门ID"
          />
        </el-form-item>
        <el-form-item label="组织名称" prop="name">
          <el-input v-model="temp.name" placeholder="请输入组织名称" />
        </el-form-item>
        <el-form-item label="封面图URL" prop="coverImage">
          <el-input v-model="temp.coverImage" placeholder="请输入封面图URL" />
        </el-form-item>
        <el-form-item label="截止时间" prop="deadline">
          <el-date-picker
            v-model="temp.deadline"
            type="datetime"
            placeholder="选择日期时间"
            value-format="YYYY-MM-DD HH:mm:ss"
            style="width: 100%"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button
            type="primary"
            @click="dialogStatus === 'create' ? createData() : updateData()"
          >
            确认
          </el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, nextTick } from "vue";
import { ElMessage, ElMessageBox } from "element-plus";
import type { FormInstance, FormRules } from "element-plus";
import {
  getAllConfigs,
  updateDeptConfig,
  deleteDeptConfig,
  existsByDeptId,
  type PerformanceReview,
} from "@/api/performanceReview";

const list = ref<PerformanceReview[]>([]);
const listLoading = ref(true);
const dialogVisible = ref(false);
const dialogStatus = ref<"create" | "update">("create");
const dialogTitle = ref("");
const dataFormRef = ref<FormInstance>();

const temp = reactive<PerformanceReview>({
  deptId: "",
  name: "",
  coverImage: "",
  deadline: "",
});

const rules = reactive<FormRules>({
  deptId: [{ required: true, message: "请输入部门ID", trigger: "blur" }],
  name: [{ required: true, message: "请输入组织名称", trigger: "blur" }],
  coverImage: [{ required: true, message: "请输入封面图URL", trigger: "blur" }],
  deadline: [{ required: true, message: "请选择截止时间", trigger: "change" }],
});

const fetchData = async () => {
  listLoading.value = true;
  try {
    const res = await getAllConfigs();
    if ((res as any).data) {
      list.value = (res as any).data;
    } else {
      list.value = [];
    }
  } catch (error) {
    console.error(error);
  } finally {
    listLoading.value = false;
  }
};

const handleAdd = () => {
  resetTemp();
  dialogStatus.value = "create";
  dialogTitle.value = "新增配置";
  dialogVisible.value = true;
  nextTick(() => {
    dataFormRef.value?.clearValidate();
  });
};

const handleEdit = (row: PerformanceReview) => {
  temp.deptId = row.deptId;
  temp.name = row.name;
  temp.coverImage = row.coverImage;
  temp.deadline = row.deadline;
  dialogStatus.value = "update";
  dialogTitle.value = "编辑配置";
  dialogVisible.value = true;
  nextTick(() => {
    dataFormRef.value?.clearValidate();
  });
};

const handleDelete = (row: PerformanceReview) => {
  ElMessageBox.confirm("确认删除该配置吗?", "提示", {
    confirmButtonText: "确定",
    cancelButtonText: "取消",
    type: "warning",
  }).then(async () => {
    try {
      await deleteDeptConfig(row.deptId);
      ElMessage.success("删除成功");
      fetchData();
    } catch (error) {
      console.error(error);
    }
  });
};

const createData = async () => {
  if (!dataFormRef.value) return;
  await dataFormRef.value.validate(async (valid) => {
    if (valid) {
      try {
        const exists = await existsByDeptId(temp.deptId);
        if ((exists as any).data === true) {
          ElMessage.error("该部门配置已存在");
          return;
        }

        await updateDeptConfig(temp.deptId, temp);
        dialogVisible.value = false;
        ElMessage.success("创建成功");
        fetchData();
      } catch (error) {
        console.error(error);
      }
    }
  });
};

const updateData = async () => {
  if (!dataFormRef.value) return;
  await dataFormRef.value.validate(async (valid) => {
    if (valid) {
      try {
        await updateDeptConfig(temp.deptId, temp);
        dialogVisible.value = false;
        ElMessage.success("更新成功");
        fetchData();
      } catch (error) {
        console.error(error);
      }
    }
  });
};

const resetTemp = () => {
  temp.deptId = "";
  temp.name = "";
  temp.coverImage = "";
  temp.deadline = "";
};

const splitImageUrls = (imageUrls: string) => {
  if (!imageUrls) return [];
  return imageUrls.split(",").map((url) => url.trim());
};

onMounted(() => {
  fetchData();
});
</script>

<style scoped>
.app-container {
  padding: 20px;
}
.filter-container {
  margin-bottom: 20px;
}
</style>
