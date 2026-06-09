<template>
  <div class="log-detail-container">
    <div class="header-bar">
      <el-button @click="goBack" icon="ArrowLeft">返回列表</el-button>
    </div>
    <div class="log-detail">
      <div class="left">
        <el-card>
          <h3>文件列表</h3>
          <div class="file-list-scroll">
            <div v-for="(f, idx) in files" :key="idx">
              <el-tooltip
                class="file-button"
                :content="fName(f)"
                effect="dark"
                placement="left-start"
              >
                <el-button type="text" @click="selectFile(f)">
                  {{ fName(f) }}
                </el-button>
              </el-tooltip>
            </div>
          </div>
        </el-card>
      </div>

      <div class="right">
        <el-card class="file-card">
          <div class="file-toolbar">
            <div class="toolbar-left">
              <span class="file-title">{{ currentFileName }}</span>
            </div>
            <div class="toolbar-right">
              <el-button size="small" @click="toggleWrap">
                {{ wrap ? "换行: 开" : "换行: 关" }}
              </el-button>
              <el-button size="small" @click="decreaseFont">A-</el-button>
              <el-button size="small" @click="increaseFont">A+</el-button>
              <el-button size="small" @click="copyContent">复制</el-button>
            </div>
          </div>

          <el-scrollbar class="file-scroll">
            <pre
              class="file-content"
              :style="{
                fontSize: fontSize + 'px',
                overflowWrap: wrap ? 'anywhere' : 'normal',
                wordBreak: wrap ? 'break-word' : 'normal',
              }"
              >{{ fileContent }}
            </pre>
          </el-scrollbar>
        </el-card>
      </div>
    </div>
  </div>
</template>

<script lang="ts" setup>
import { ref, onMounted } from "vue";
import { useRoute, useRouter } from "vue-router";
import { ElMessage } from "element-plus";
import { ArrowLeft } from "@element-plus/icons-vue";
import { getLogById, getLogFileContent } from "@/api/logs";

const route = useRoute();
const router = useRouter();
const id = Number(route.params.id);

const files = ref<string[]>([]);
const currentFile = ref("");
const currentFileName = ref("");
const fileContent = ref("");

// UI helpers for the log preview
const wrap = ref(true);
const fontSize = ref(13);

const goBack = () => {
  router.back();
};

const toggleWrap = () => {
  wrap.value = !wrap.value;
};

const increaseFont = () => {
  if (fontSize.value < 32) fontSize.value += 1;
};

const decreaseFont = () => {
  if (fontSize.value > 8) fontSize.value -= 1;
};

const copyContent = async () => {
  try {
    await navigator.clipboard.writeText(fileContent.value || "");
    ElMessage.success("已复制到剪贴板");
  } catch (e) {
    ElMessage.error("复制失败");
  }
};

const fName = (p: string) => {
  try {
    return decodeURIComponent(p.split(/[/\\]/).pop() || p);
  } catch (e) {
    return p;
  }
};

const loadFileContent = async (path: string) => {
  try {
    fileContent.value = "加载中...";
    const resp = await getLogFileContent(path);
    // axios typed response: resp.data may be string or response object
    // when using our request util, response.data is returned for normal requests; for text responseType axios returns response as string
    // To be defensive:
    // @ts-ignore
    fileContent.value =
      resp && (resp as any).data ? (resp as any).data : (resp as any) || "";
    currentFileName.value = fName(path);
  } catch (err) {
    console.error(err);
    fileContent.value = "加载失败";
    ElMessage.error("加载文件失败");
  }
};

const selectFile = (p: string) => {
  currentFile.value = p;
  loadFileContent(p);
  console.log("Selected file:", p);
};

onMounted(async () => {
  try {
    const { data } = await getLogById(id);
    files.value = data.path
      ? data.path
          .split(",")
          .map((s) => s.trim())
          .filter(Boolean)
      : [];
    if (files.value.length > 0) selectFile(files.value[0]);
  } catch (err) {
    console.error(err);
    ElMessage.error("加载详情失败");
  }
});
</script>

<style scoped lang="scss">
.log-detail-container {
  padding: 16px;
  display: flex;
  flex-direction: column;
  gap: 12px;
}
.header-bar {
  display: flex;
  align-items: center;
}
.log-detail {
  display: flex;
  gap: 16px;
}
.left {
  width: 300px;
}
.el-card {
  height: calc(100vh - 100px);
}
.file-list-scroll {
  display: flex;
  padding-bottom: 20px;
  flex-direction: column;
  overflow-y: auto;
  overflow-x: hidden;
  gap: 10px;
  max-height: calc(100vh - 180px);
}
.file-button {
  display: flex;
  justify-content: center;
}
.file-name {
  display: flex;
}
.file-card {
  height: calc(100vh - 100px);
  padding-bottom: 20px;
}
.file-scroll {
  height: 80vh;
}
.file-content {
  white-space: pre-wrap;
  background: #111;
  color: #dcdcdc;
  padding: 12px;
}
.file-toolbar {
  margin-bottom: 8px;
  font-weight: 600;
}
.toolbar-right {
  display: flex;
  gap: 10px;
}
</style>
