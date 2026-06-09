/**
 * 埋点数据统计面板组件示例
 * 这是一个可选的增强组件，可用于展示埋点数据的统计信息
 * 使用方式：在 Tracking.vue 中导入并使用
 */

import { defineComponent, PropType } from "vue";
import { TrackingEvent } from "@/types/tracking";
import {
  getTrackingStatistics,
  calculateAverageResponseTime,
  getMostFrequentEvent,
  getMostCommonDevice,
} from "@/utils/tracking";

export default defineComponent({
  name: "TrackingStatistics",
  props: {
    data: {
      type: Array as PropType<TrackingEvent[]>,
      default: () => [],
    },
  },
  setup(props) {
    const statistics = () => {
      return getTrackingStatistics(props.data);
    };

    const avgResponseTime = () => {
      return calculateAverageResponseTime(props.data);
    };

    const mostFrequentEvent = () => {
      return getMostFrequentEvent(props.data);
    };

    const mostCommonDevice = () => {
      return getMostCommonDevice(props.data);
    };

    return {
      statistics,
      avgResponseTime,
      mostFrequentEvent,
      mostCommonDevice,
    };
  },
});
