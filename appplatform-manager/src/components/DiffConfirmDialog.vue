<template>
  <el-dialog
    v-model="visible"
    :title="title"
    :width="width"
    :teleported="false"
    @closed="onDialogClosed"
    destroy-on-close
  >
    <div class="diff-wrapper">
      <div v-if="metaChanges.length" class="diff-meta">
        <div class="diff-meta-title">元数据变更</div>
        <div class="diff-meta-content">
          <div
            v-for="(line, idx) in metaChanges"
            :key="idx"
            class="diff-meta-line"
          >
            {{ line }}
          </div>
        </div>
      </div>

      <div class="diff-grid-wrapper">
        <div class="diff-grid">
          <div class="diff-pane">
            <div class="diff-pane-title">修改前</div>
            <Codemirror
              :model-value="beforeText"
              :style="{ height: editorHeight }"
              :autofocus="false"
              :indent-with-tab="true"
              :tab-size="2"
              :extensions="leftExtensions"
              @update:model-value="noopUpdate"
            />
          </div>
          <div class="diff-pane">
            <div class="diff-pane-title">修改后</div>
            <Codemirror
              :model-value="afterText"
              :style="{ height: editorHeight }"
              :autofocus="false"
              :indent-with-tab="true"
              :tab-size="2"
              :extensions="rightExtensions"
              @update:model-value="noopUpdate"
            />
          </div>
        </div>
      </div>
    </div>

    <template #footer>
      <el-button @click="onCancel">{{ cancelText }}</el-button>
      <el-button type="primary" @click="onConfirm">{{ confirmText }}</el-button>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { computed, ref } from "vue";
import { Codemirror } from "vue-codemirror";
import { json } from "@codemirror/lang-json";
import { oneDark } from "@codemirror/theme-one-dark";
import { EditorState, StateField, type Range } from "@codemirror/state";
import { Decoration, EditorView, lineNumbers } from "@codemirror/view";

const props = withDefaults(
  defineProps<{
    modelValue: boolean;
    title?: string;
    width?: string | number;
    editorHeight?: string;
    confirmText?: string;
    cancelText?: string;
    metaChanges?: string[];
    beforeText: string;
    afterText: string;
  }>(),
  {
    title: "确认保存",
    width: "92vw",
    editorHeight: "55vh",
    confirmText: "确认保存",
    cancelText: "取消",
    metaChanges: () => [],
  },
);

const emit = defineEmits<{
  (e: "update:modelValue", v: boolean): void;
  (e: "confirm"): void;
  (e: "cancel"): void;
}>();

const visible = computed({
  get: () => props.modelValue,
  set: (v: boolean) => emit("update:modelValue", v),
});

const closeReason = ref<"confirm" | "cancel" | null>(null);

const noopUpdate = () => {};

const baseExtensions = [json(), oneDark];
const readOnlyExtensions = [
  ...baseExtensions,
  lineNumbers(),
  EditorState.readOnly.of(true),
  EditorView.editable.of(false),
];

const computeChangedLines = (oldText: string, newText: string) => {
  const oldLines = oldText.split(/\r?\n/);
  const newLines = newText.split(/\r?\n/);

  const maxLines = 600;
  const safeOldLines =
    oldLines.length > maxLines ? oldLines.slice(0, maxLines) : oldLines;
  const safeNewLines =
    newLines.length > maxLines ? newLines.slice(0, maxLines) : newLines;

  const m = safeOldLines.length;
  const n = safeNewLines.length;

  const oldChanged = new Set<number>();
  const newChanged = new Set<number>();

  const cellLimit = 200000;
  if (m * n > cellLimit) {
    const max = Math.max(m, n);
    for (let i = 0; i < max; i++) {
      const a = safeOldLines[i];
      const b = safeNewLines[i];
      if (a === undefined) newChanged.add(i + 1);
      else if (b === undefined) oldChanged.add(i + 1);
      else if (a !== b) {
        oldChanged.add(i + 1);
        newChanged.add(i + 1);
      }
    }
    return {
      oldChangedLines: Array.from(oldChanged),
      newChangedLines: Array.from(newChanged),
    };
  }

  const dp: number[][] = Array.from({ length: m + 1 }, () =>
    Array(n + 1).fill(0),
  );

  for (let i = m - 1; i >= 0; i--) {
    for (let j = n - 1; j >= 0; j--) {
      if (safeOldLines[i] === safeNewLines[j]) dp[i][j] = dp[i + 1][j + 1] + 1;
      else dp[i][j] = Math.max(dp[i + 1][j], dp[i][j + 1]);
    }
  }

  let i = 0;
  let j = 0;
  while (i < m && j < n) {
    if (safeOldLines[i] === safeNewLines[j]) {
      i++;
      j++;
      continue;
    }

    if (dp[i + 1][j] >= dp[i][j + 1]) {
      oldChanged.add(i + 1);
      i++;
    } else {
      newChanged.add(j + 1);
      j++;
    }
  }
  while (i < m) oldChanged.add(i++ + 1);
  while (j < n) newChanged.add(j++ + 1);

  return {
    oldChangedLines: Array.from(oldChanged),
    newChangedLines: Array.from(newChanged),
  };
};

const highlightLinesExtension = (
  lineNumbersList: number[],
  className: string,
) => {
  const sorted = Array.from(new Set(lineNumbersList)).sort((a, b) => a - b);
  const build = (state: EditorState) => {
    const decos: Range<Decoration>[] = [];
    const lineCount = state.doc.lines;
    for (const lineNo of sorted) {
      if (lineNo < 1 || lineNo > lineCount) continue;
      const line = state.doc.line(lineNo);
      decos.push(
        Decoration.line({ attributes: { class: className } }).range(line.from),
      );
    }
    return Decoration.set(decos, true);
  };

  const field = StateField.define({
    create(state) {
      return build(state);
    },
    update(value, tr) {
      if (!tr.docChanged) return value;
      return build(tr.state);
    },
    provide: (f) => EditorView.decorations.from(f),
  });

  return field;
};

const changed = computed(() =>
  computeChangedLines(props.beforeText || "", props.afterText || ""),
);

const leftExtensions = computed(() => [
  ...readOnlyExtensions,
  highlightLinesExtension(changed.value.oldChangedLines, "diff-line-removed"),
]);

const rightExtensions = computed(() => [
  ...readOnlyExtensions,
  highlightLinesExtension(changed.value.newChangedLines, "diff-line-added"),
]);

const onCancel = () => {
  closeReason.value = "cancel";
  visible.value = false;
  emit("cancel");
};

const onConfirm = () => {
  closeReason.value = "confirm";
  visible.value = false;
  emit("confirm");
};

const onDialogClosed = () => {
  if (!closeReason.value) emit("cancel");
  closeReason.value = null;
};
</script>

<style scoped lang="scss">
.diff-wrapper {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.diff-meta {
  padding: 10px 12px;
  border-radius: 6px;
  background: #f5f7fa;
  border: 1px solid #ebeef5;
}

.diff-meta-title {
  font-size: 12px;
  font-weight: 600;
  color: #303133;
  margin-bottom: 6px;
}

.diff-meta-content {
  font-size: 12px;
  line-height: 1.5;
  color: #606266;
  word-break: break-all;
}

.diff-grid-wrapper {
  overflow-x: auto;
}

.diff-grid {
  display: grid;
  grid-template-columns: minmax(0, 1fr) minmax(0, 1fr);
  gap: 12px;
  min-width: 980px;
}

.diff-pane {
  border: 1px solid #ebeef5;
  border-radius: 6px;
  overflow: hidden;
  background: #ffffff;
}

.diff-pane-title {
  padding: 8px 12px;
  background: #f5f7fa;
  border-bottom: 1px solid #ebeef5;
  font-size: 12px;
  font-weight: 600;
  color: #303133;
}

:deep(.cm-editor) {
  font-size: 12px;
}

:deep(.diff-line-added) {
  background: rgba(103, 194, 58, 0.14);
}

:deep(.diff-line-removed) {
  background: rgba(245, 108, 108, 0.14);
}
</style>
