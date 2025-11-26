import moment from "moment";

export function formatDate(dateString: string): string {
  return moment(dateString).utcOffset(0).format("YYYY-MM-DD HH:mm");
}
