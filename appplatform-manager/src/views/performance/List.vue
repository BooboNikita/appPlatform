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
          <el-image
            style="width: 100px; height: 100px"
            :src="scope.row.coverImage"
            :preview-src-list="[scope.row.coverImage]"
            fit="cover"
            v-if="scope.row.coverImage"
          />
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
    // Assuming the response structure handled by request.ts returns the data directly if success
    // The request.ts interceptor returns res directly which is the body.
    // However, the api definition return type might need adjustment depending on how request.ts transforms it.
    // Based on request.ts: return res (which is response.data).
    // And if res.code != 200, it rejects.
    // So here res should be the data part if the API returns { code: 200, data: [...] }
    // But wait, getAllConfigs return type in api.ts is request.get<any, PerformanceReview[]>
    // Usually request.get<T, R> where R is the return type.
    // Let's assume request.ts returns the `data` field of the JSON response if successful?
    // Looking at request.ts: `return res;`. `res` is `response.data`.
    // So if backend returns { code: 200, msg: "...", data: [...] }, `res` is that object.
    // So `res.data` is the list.
    // I need to check `getAllConfigs` implementation again.
    // It returns `request.get<any, PerformanceReview[]>(...)`.
    // If generic types are correct, res should be PerformanceReview[].
    // BUT, usually axios wrapper returns the response wrapper.
    // Let's check `request.ts` usage in `app.ts` again.
    // `getAppList` returns `request.get<PageResult<AppInfo>>`.
    // So the return type is the full response body or just the data?
    // `request.ts` returns `res` (response.data).
    // So if backend returns `Result<List<...>>`, then `res` is `Result`.
    // So `res.data` is the list.
    // My API definition: `request.get<any, PerformanceReview[]>` might be misleading if I expect `PerformanceReview[]` directly but it returns `Result`.
    // Let's look at `app.ts` again: `return request.get<PageResult<AppInfo>>...`
    // And `request.ts` returns `res`.
    // So `res` matches `PageResult<AppInfo>`?
    // Wait, `PageResult` interface has `list`, `total` etc.
    // This matches the data structure of a successful response's data payload?
    // The Java Result class usually has `code`, `msg`, `data`.
    // If `request.ts` returns `response.data` (the whole JSON), then the return type of `request.get` should be the whole JSON structure.
    // However, `app.ts` defines `PageResult` which looks like the `data` part of a Result, OR the Result itself?
    // Let's check `app.ts` imports.
    // It doesn't import a `Result` type.
    // But `request.ts` checks `res.code !== 200`. This implies `res` HAS `code`.
    // So `res` is the wrapper.
    // So `getAppList` returning `PageResult` is suspicious unless `PageResult` extends the wrapper or `request.ts` unwraps it.
    // `request.ts`: `return res;` (line 54).
    // So it returns the wrapper.
    // So my API types should probably be `Result<PerformanceReview[]>`.
    // But I don't have `Result` type defined in frontend commonly?
    // Let's look at `app.ts` again. `PageResult` has `list`, `total`.
    // If `res` is the wrapper, `res.data` would be `PageResult`.
    // So `getAppList` should probably return `Promise<Result<PageResult>>`.
    // BUT `request.get` generic usually defines the resolve type.
    // Let's just inspect `res` in runtime or assume standard `data` property access.
    // I'll assume `res` has a `data` property which contains the list.
    // I will cast it to any to be safe or define a Result interface.

    // For now, I will assume res is the Result object.
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
      // Check if exists first? Or just try update/create
      // The controller doesn't have create, so we use updateDeptConfig which is PUT.
      // Usually PUT is idempotent and can create if ID is provided.
      try {
        // First check if it exists to avoid overwriting? Or just proceed.
        // User wants to "add".
        const exists = await existsByDeptId(temp.deptId);
        // exists API returns Result<Boolean>.
        // Based on my assumption about request.ts returning Result object.
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
