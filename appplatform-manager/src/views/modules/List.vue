<template>
  <div class="modules-list">
    <div class="action-container">
      <el-button type="primary" @click="handleCreate">
        <el-icon><Plus /></el-icon>
        新增模块
      </el-button>
    </div>

    <el-table
      v-loading="loading"
      :data="modulesList"
      border
      style="width: 100%"
    >
      <el-table-column prop="title" label="模块名称" min-width="120">
        <template #default="{ row }">
          <div class="module-name-cell">
            <div
              v-if="row.color"
              class="color-dot"
              :style="{ backgroundColor: row.color }"
            />
            {{ row.title }}
          </div>
        </template>
      </el-table-column>
      <el-table-column prop="iconUrl" label="图标" width="80">
        <template #default="{ row }">
          <img
            v-if="row.iconUrl"
            :src="row.iconUrl"
            :alt="row.title"
            class="icon-thumb"
          />
        </template>
      </el-table-column>
      <el-table-column prop="route" label="路由类型" width="120">
        <template #default="{ row }">
          <el-tag
            :type="
              row.route === 'under_development'
                ? 'danger'
                : row.route === 'inner'
                ? 'success'
                : 'info'
            "
          >
            {{
              row.route === "under_development"
                ? "开发中"
                : row.route === "inner"
                ? "内部页面"
                : "网页"
            }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="sortOrder" label="排序" width="80" />
      <el-table-column prop="isActive" label="激活状态" width="100">
        <template #default="{ row }">
          <el-tag :type="row.isActive ? 'success' : 'info'">
            {{ row.isActive ? "激活" : "未激活" }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="hideForTest" label="测试账号隐藏" width="100">
        <template #default="{ row }">
          <el-tag :type="!row.hideForTest ? 'success' : 'info'">
            {{ row.hideForTest ? "隐藏" : "显示" }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="targetUrl" label="目标URL" min-width="150">
        <template #default="{ row }">
          {{ row.targetUrl }}
        </template>
      </el-table-column>
      <el-table-column prop="port" label="端口" width="80">
        <template #default="{ row }">
          {{ row.port || "-" }}
        </template>
      </el-table-column>

      <el-table-column prop="createAt" label="创建时间" width="180">
        <template #default="{ row }">
          {{ formatDate(row.createdAt) }}
        </template>
      </el-table-column>
      <el-table-column label="操作" width="200" fixed="right">
        <template #default="{ row }">
          <div class="operation-cell">
            <el-button type="warning" size="small" @click="handleEdit(row)">
              修改
            </el-button>
            <el-button type="danger" size="small" @click="handleDelete(row)">
              删除
            </el-button>
          </div>
        </template>
      </el-table-column>
    </el-table>
  </div>
</template>

<script lang="ts" setup>
import { ref, onMounted } from "vue";
import { useRouter } from "vue-router";
import { ElMessage, ElMessageBox } from "element-plus";
import { Plus } from "@element-plus/icons-vue";
import { getModulesList, deleteModule, type ModuleItem } from "@/api/modules";
import { formatDate } from "@/utils/index";

const router = useRouter();
const loading = ref(false);
const modulesList = ref<ModuleItem[]>([]);

// 获取模块列表
const fetchModulesList = async () => {
  try {
    loading.value = true;
    const { data } = await getModulesList();
    modulesList.value = data.list || data;
  } catch (error) {
    console.error("获取模块列表失败:", error?.toString());
    ElMessage.error("获取模块列表失败");
  } finally {
    loading.value = false;
  }
};

// 新增模块
const handleCreate = () => {
  router.push("/modules/edit");
};

// 编辑模块
const handleEdit = (row: ModuleItem) => {
  router.push({
    path: "/modules/edit",
    query: { id: row.id },
  });
};

// 删除模块
const handleDelete = (row: ModuleItem) => {
  ElMessageBox.confirm(`确定要删除模块 "${row.title}" 吗？`, "提示", {
    confirmButtonText: "确定",
    cancelButtonText: "取消",
    type: "warning",
  })
    .then(async () => {
      try {
        await deleteModule(row.id!);
        ElMessage.success("删除成功");
        fetchModulesList();
      } catch (error) {
        console.error("删除失败:", error);
        ElMessage.error("删除失败");
      }
    })
    .catch(() => {});
};

onMounted(() => {
  fetchModulesList();
});
</script>

<style lang="scss" scoped>
.modules-list {
  background: #fff;
  padding: 20px;
  border-radius: 4px;
  box-shadow: 0 1px 4px rgba(0, 21, 41, 0.08);

  .action-container {
    margin-bottom: 20px;
  }

  .operation-cell {
    display: flex;
    gap: 8px;
    flex-wrap: wrap;
  }

  .module-name-cell {
    display: flex;
    align-items: center;
    gap: 8px;

    .color-dot {
      width: 12px;
      height: 12px;
      border-radius: 50%;
      flex-shrink: 0;
    }
  }

  .icon-thumb {
    width: 40px;
    height: 40px;
    border-radius: 4px;
    object-fit: cover;
  }

  .url-preview {
    display: flex;
    align-items: center;
    gap: 8px;

    .icon-thumb {
      width: 32px;
      height: 32px;
      border-radius: 4px;
      object-fit: cover;
    }

    .url-text {
      font-size: 12px;
      color: #666;
      overflow: hidden;
      text-overflow: ellipsis;
      white-space: nowrap;
      flex: 1;
    }
  }
}
</style>
