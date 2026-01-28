import moment from "moment";

export function formatDate(dateString: string): string {
  if (!dateString) return "-";
  return moment.parseZone(dateString).format("YYYY-MM-DD HH:mm:ss");
}

export function formatTimestamp(timestamp: number): string {
  return moment.parseZone(timestamp).format("YYYY-MM-DD HH:mm:ss");
}
