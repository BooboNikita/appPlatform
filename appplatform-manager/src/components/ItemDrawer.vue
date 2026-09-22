<template>
  <el-drawer
    :model-value="modelValue"
    :title="title"
    size="560px"
    :destroy-on-close="true"
    @update:model-value="emit('update:modelValue', $event)"
    @open="syncLocal"
  >
    <template v-if="item">
      <el-descriptions :column="1" border size="small">
        <el-descriptions-item label="环境">
          <el-tag
            :type="item.env === 'prod' ? 'danger' : 'warning'"
            size="small"
            >{{ item.env }}</el-tag
          >
        </el-descriptions-item>
        <el-descriptions-item label="版本范围">{{
          item.versionRange
        }}</el-descriptions-item>
        <el-descriptions-item label="值类型">{{
          item.valueType
        }}</el-descriptions-item>
        <el-descriptions-item label="更新时间">{{
          formatDate(item.updateTime, 8)
        }}</el-descriptions-item>
      </el-descriptions>

      <div class="section-label">版本范围</div>
      <div class="row">
        <el-input
          v-model="editVersionRange"
          placeholder="默认继承所属配置的版本范围"
          style="flex: 1"
        />
      </div>
      <div class="input-tip">
        可填写独立版本范围（如
        1.5.0-1.8.0），下发时按客户端版本对该配置项单独匹配；清空保存则恢复继承
      </div>

      <div class="section-label">配置值</div>
      <codemirror
        v-model="editValue"
        :style="{ height: '200px', width: '100%' }"
        :extensions="extensions"
      />
      <div class="input-tip">
        支持 JSON 值：true/false、数字、字符串、对象、数组
      </div>
      <div class="row" style="margin-top: 8px">
        <el-input v-model="editRemark" placeholder="备注" style="flex: 1" />
        <el-button type="primary" :loading="saving" @click="handleSaveValue"
          >保存</el-button
        >
      </div>

      <div class="section-label">下发状态</div>
      <div class="row">
        <span class="row-label">启用</span>
        <el-switch
          v-model="localEnabled"
          :active-value="1"
          :inactive-value="0"
          @change="saveField('enabled', localEnabled)"
        />
        <span class="tip-inline">停用后该配置项不会下发给任何用户</span>
      </div>

      <div class="section-label">灰度设置</div>
      <div class="row">
        <span class="row-label">开启灰度</span>
        <el-switch
          v-model="localGrayEnabled"
          :active-value="1"
          :inactive-value="0"
          @change="saveField('grayEnabled', localGrayEnabled)"
        />
        <el-tag v-if="localGrayEnabled === 1" type="primary" size="small"
          >{{ localGrayPercent }}%</el-tag
        >
      </div>
      <div v-if="localGrayEnabled === 1" class="slider-row">
        <el-slider
          v-model="localGrayPercent"
          :max="100"
          show-input
          @change="saveField('grayPercent', localGrayPercent)"
        />
      </div>

      <div class="row">
        <el-button link type="warning" @click="historyVisible = true"
          ><el-icon><Clock /></el-icon>&nbsp;变更历史</el-button
        >
      </div>

      <el-divider />
      <GrayUserPanel
        ref="grayPanelRef"
        :item-id="item.id"
        @changed="emit('changed')"
      />
    </template>

    <!-- 二级页面：变更历史弹窗 -->
    <ItemHistoryDialog
      v-model="historyVisible"
      :item-id="item ? item.id : null"
      :title="item ? item.domain + '.' + item.itemKey : ''"
      @reverted="handleReverted"
    />
  </el-drawer>
</template>

<script setup lang="ts">
import { ref, computed, watch } from "vue";
import { ElMessage } from "element-plus";
import { Clock } from "@element-plus/icons-vue";
import { Codemirror } from "vue-codemirror";
import { json } from "@codemirror/lang-json";
import { oneDark } from "@codemirror/theme-one-dark";
import {
  updateDynamicConfigItem,
  DynamicConfigItem,
} from "@/api/dynamicConfig";
import GrayUserPanel from "@/components/GrayUserPanel.vue";
import ItemHistoryDialog from "@/components/ItemHistoryDialog.vue";
import { formatDate } from "@/utils/index";

const props = defineProps<{
  modelValue: boolean;
  item: DynamicConfigItem | null;
}>();

const emit = defineEmits<{
  (e: "update:modelValue", v: boolean): void;
  (e: "changed"): void;
}>();

const extensions = [json(), oneDark];

const editValue = ref("");
const editRemark = ref("");
const editVersionRange = ref("");
const saving = ref(false);
const localEnabled = ref(1);
const localGrayEnabled = ref(0);
const localGrayPercent = ref(0);
const historyVisible = ref(false);
const grayPanelRef = ref<{ fetchList: () => void } | null>(null);

// 回溯成功后：通知父级刷新列表以回传最新 item，同时刷新抽屉内名单面板
const handleReverted = () => {
  emit("changed");
  grayPanelRef.value?.fetchList();
};

const title = computed(() =>
  props.item ? `配置项 - ${props.item.domain}.${props.item.itemKey}` : "配置项",
);

const syncLocal = () => {
  if (!props.item) return;
  try {
    editValue.value = JSON.stringify(JSON.parse(props.item.itemValue), null, 2);
  } catch {
    editValue.value = props.item.itemValue;
  }
  editRemark.value = props.item.remark || "";
  editVersionRange.value = props.item.versionRange;
  localEnabled.value = props.item.enabled;
  localGrayEnabled.value = props.item.grayEnabled;
  localGrayPercent.value = props.item.grayPercent;
};

watch(
  () => props.item,
  () => syncLocal(),
);

const handleSaveValue = async () => {
  if (!props.item) return;
  let parsed: any;
  try {
    parsed = JSON.parse(editValue.value);
  } catch {
    ElMessage.error("配置值不是合法 JSON");
    return;
  }
  saving.value = true;
  try {
    await updateDynamicConfigItem(props.item.id, {
      itemValue: JSON.stringify(parsed),
      versionRange: editVersionRange.value.trim(),
      remark: editRemark.value,
    });
    ElMessage.success("保存成功");
    emit("changed");
  } finally {
    saving.value = false;
  }
};

const saveField = async (
  field: "enabled" | "grayEnabled" | "grayPercent",
  value: number,
) => {
  if (!props.item) return;
  try {
    await updateDynamicConfigItem(props.item.id, { [field]: value });
    ElMessage.success("已更新");
    emit("changed");
  } catch {
    syncLocal();
  }
};
</script>

<style scoped>
.section-label {
  font-weight: 600;
  margin: 16px 0 8px;
  font-size: 14px;
}
.row {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 12px;
}
.row-label {
  width: 70px;
  color: var(--el-text-color-regular);
}
.tip-inline {
  font-size: 12px;
  color: var(--el-text-color-secondary);
}
.input-tip {
  font-size: 12px;
  color: var(--el-text-color-secondary);
  margin-top: 4px;
}
.slider-row {
  padding: 0 8px 8px 0;
}
</style>
