import moment from "moment";

// 统一以北京/东八区显示时间
// - 带时区标记（Z 或 ±hh:mm）的 ISO 字符串 / 时间戳：解析出真实时刻后转到东八区
// - 不带时区标记的字符串：后端返回的已是北京时间，原样显示（parseZone 会把无时区字符串按 UTC 处理，不能再 +8）
// - 默认偏移为 8，保证各处时间列显示一致
export function formatDate(
  dateString: string | number,
  offset: number = 8,
): string {
  if (dateString === null || dateString === undefined || dateString === "") {
    return "-";
  }
  if (typeof dateString === "number") {
    return moment(dateString).utcOffset(offset).format("YYYY-MM-DD HH:mm:ss");
  }
  const hasTimezone = /(?:Z|[+-]\d{2}:?\d{2})$/i.test(dateString);
  const m = moment.parseZone(dateString);
  return (hasTimezone ? m.utcOffset(offset) : m).format("YYYY-MM-DD HH:mm:ss");
}

export function formatTimestamp(timestamp: number | string): string {
  if (timestamp === null || timestamp === undefined || timestamp === "") {
    return "-";
  }
  return moment.parseZone(timestamp).utcOffset(8).format("YYYY-MM-DD HH:mm:ss");
}
