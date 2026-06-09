/**
 * 埋点数据工具函数
 */

import { TrackingEvent } from "@/types/tracking";

/**
 * 导出埋点数据为 CSV
 */
export const exportTrackingDataAsCSV = (
  data: TrackingEvent[],
  filename: string = "tracking_data.csv"
) => {
  if (!data || data.length === 0) {
    console.warn("没有数据可导出");
    return;
  }

  // CSV 表头
  const headers = [
    "ID",
    "用户ID",
    "用户名",
    "会话ID",
    "页面URL",
    "Referrer",
    "状态",
    "应用版本",
    "构建号",
    "设备ID",
    "设备型号",
    "品牌",
    "IP地址",
    "操作系统",
    "系统版本",
    "网络类型",
    "屏幕分辨率",
    "事件ID",
    "事件类型",
    "事件时间",
    "接收时间",
  ];

  // 转换数据为 CSV 行
  const rows = data.map((item) => [
    item.id,
    item.userId,
    item.userName,
    item.sessionId,
    item.pageUrl,
    item.referrer,
    item.status,
    item.app.version,
    item.app.buildNumber,
    item.device.deviceId,
    item.device.model,
    item.device.brand,
    item.device.ip,
    item.device.os,
    item.device.osVersion,
    item.device.networkType,
    item.device.screenResolution,
    item.eventInfo.eventId,
    item.eventInfo.eventType,
    new Date(item.eventInfo.eventTime * 1000).toLocaleString(),
    new Date(item.eventInfo.recvTime * 1000).toLocaleString(),
  ]);

  // 合并为 CSV 字符串
  const csvContent = [headers, ...rows]
    .map((row) => row.map((cell) => `"${cell}"`).join(","))
    .join("\n");

  // 创建 Blob 并下载
  const blob = new Blob(["\uFEFF" + csvContent], {
    type: "text/csv;charset=utf-8;",
  });
  const link = document.createElement("a");
  const url = URL.createObjectURL(blob);

  link.setAttribute("href", url);
  link.setAttribute("download", filename);
  link.style.visibility = "hidden";

  document.body.appendChild(link);
  link.click();
  document.body.removeChild(link);
};

/**
 * 导出埋点数据为 JSON
 */
export const exportTrackingDataAsJSON = (
  data: TrackingEvent[],
  filename: string = "tracking_data.json"
) => {
  if (!data || data.length === 0) {
    console.warn("没有数据可导出");
    return;
  }

  const jsonContent = JSON.stringify(data, null, 2);
  const blob = new Blob([jsonContent], {
    type: "application/json;charset=utf-8;",
  });
  const link = document.createElement("a");
  const url = URL.createObjectURL(blob);

  link.setAttribute("href", url);
  link.setAttribute("download", filename);
  link.style.visibility = "hidden";

  document.body.appendChild(link);
  link.click();
  document.body.removeChild(link);
};

/**
 * 获取埋点数据统计信息
 */
export const getTrackingStatistics = (data: TrackingEvent[]) => {
  if (!data || data.length === 0) {
    return {
      total: 0,
      eventTypes: {},
      devices: {},
      users: new Set(),
      osTypes: {},
    };
  }

  const statistics = {
    total: data.length,
    eventTypes: {} as Record<string, number>,
    devices: {} as Record<string, number>,
    users: new Set<string>(),
    osTypes: {} as Record<string, number>,
  };

  data.forEach((item) => {
    // 统计事件类型
    statistics.eventTypes[item.eventInfo.eventType] =
      (statistics.eventTypes[item.eventInfo.eventType] || 0) + 1;

    // 统计设备型号
    statistics.devices[item.device.model] =
      (statistics.devices[item.device.model] || 0) + 1;

    // 统计用户
    statistics.users.add(item.userId);

    // 统计操作系统
    statistics.osTypes[item.device.os] =
      (statistics.osTypes[item.device.os] || 0) + 1;
  });

  return statistics;
};

/**
 * 按设备分组埋点数据
 */
export const groupTrackingByDevice = (data: TrackingEvent[]) => {
  const grouped = new Map<string, TrackingEvent[]>();

  data.forEach((item) => {
    const key = `${item.device.brand}/${item.device.model}`;
    if (!grouped.has(key)) {
      grouped.set(key, []);
    }
    grouped.get(key)!.push(item);
  });

  return Object.fromEntries(grouped);
};

/**
 * 按用户分组埋点数据
 */
export const groupTrackingByUser = (data: TrackingEvent[]) => {
  const grouped = new Map<string, TrackingEvent[]>();

  data.forEach((item) => {
    if (!grouped.has(item.userId)) {
      grouped.set(item.userId, []);
    }
    grouped.get(item.userId)!.push(item);
  });

  return Object.fromEntries(grouped);
};

/**
 * 按事件类型分组埋点数据
 */
export const groupTrackingByEventType = (data: TrackingEvent[]) => {
  const grouped = new Map<string, TrackingEvent[]>();

  data.forEach((item) => {
    if (!grouped.has(item.eventInfo.eventType)) {
      grouped.set(item.eventInfo.eventType, []);
    }
    grouped.get(item.eventInfo.eventType)!.push(item);
  });

  return Object.fromEntries(grouped);
};

/**
 * 按时间范围筛选埋点数据
 */
export const filterTrackingByTimeRange = (
  data: TrackingEvent[],
  startTime: number,
  endTime: number
) => {
  return data.filter(
    (item) =>
      item.eventInfo.eventTime >= startTime &&
      item.eventInfo.eventTime <= endTime
  );
};

/**
 * 按状态筛选埋点数据
 */
export const filterTrackingByStatus = (
  data: TrackingEvent[],
  status: number
) => {
  return data.filter((item) => item.status === status);
};

/**
 * 获取用户的会话信息
 */
export const getUserSessions = (data: TrackingEvent[], userId: string) => {
  const userEvents = data.filter((item) => item.userId === userId);
  const sessions = new Map<string, TrackingEvent[]>();

  userEvents.forEach((item) => {
    if (!sessions.has(item.sessionId)) {
      sessions.set(item.sessionId, []);
    }
    sessions.get(item.sessionId)!.push(item);
  });

  return Object.fromEntries(sessions);
};

/**
 * 获取页面的访问数据
 */
export const getPageVisits = (data: TrackingEvent[], pageUrl: string) => {
  return data.filter((item) => item.pageUrl === pageUrl);
};

/**
 * 计算平均响应时间
 */
export const calculateAverageResponseTime = (data: TrackingEvent[]): number => {
  if (data.length === 0) return 0;

  const totalTime = data.reduce((sum, item) => {
    return sum + (item.eventInfo.recvTime - item.eventInfo.eventTime);
  }, 0);

  return totalTime / data.length;
};

/**
 * 获取最频繁的事件
 */
export const getMostFrequentEvent = (data: TrackingEvent[]) => {
  const eventCounts = new Map<string, number>();

  data.forEach((item) => {
    const key = item.eventInfo.eventType;
    eventCounts.set(key, (eventCounts.get(key) || 0) + 1);
  });

  let maxEvent = "";
  let maxCount = 0;

  eventCounts.forEach((count, eventType) => {
    if (count > maxCount) {
      maxCount = count;
      maxEvent = eventType;
    }
  });

  return { event: maxEvent, count: maxCount };
};

/**
 * 获取最常用的设备
 */
export const getMostCommonDevice = (data: TrackingEvent[]) => {
  const deviceCounts = new Map<string, number>();

  data.forEach((item) => {
    const key = `${item.device.brand}/${item.device.model}`;
    deviceCounts.set(key, (deviceCounts.get(key) || 0) + 1);
  });

  let maxDevice = "";
  let maxCount = 0;

  deviceCounts.forEach((count, device) => {
    if (count > maxCount) {
      maxCount = count;
      maxDevice = device;
    }
  });

  return { device: maxDevice, count: maxCount };
};
