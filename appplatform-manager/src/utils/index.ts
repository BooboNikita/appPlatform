import moment from "moment";

export function formatDate(dateString: string, offset: number = 0): string {
  if (!dateString) return "-";
  return moment
    .parseZone(dateString)
    .utcOffset(offset)
    .format("YYYY-MM-DD HH:mm:ss");
}

export function formatTimestamp(timestamp: number): string {
  return moment.parseZone(timestamp).format("YYYY-MM-DD HH:mm:ss");
}
