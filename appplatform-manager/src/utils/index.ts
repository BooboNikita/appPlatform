import moment from "moment";

// 统一以北京/东八区显示时间
// - 带时区偏移的 ISO 字符串 / 时间戳：解析出真实时刻后转到东八区
// - 默认偏移为 8，保证各处时间列显示一致
export function formatDate(dateString: string, offset: number = 8): string {
  if (!dateString) return "-";
  return moment
    .parseZone(dateString)
    .utcOffset(offset)
    .format("YYYY-MM-DD HH:mm:ss");
}

export function formatTimestamp(timestamp: number | string): string {
  if (timestamp === null || timestamp === undefined || timestamp === "") {
    return "-";
  }
  return moment.parseZone(timestamp).utcOffset(8).format("YYYY-MM-DD HH:mm:ss");
}
