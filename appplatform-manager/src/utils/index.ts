import moment from "moment";

export function formatDate(dateString: string): string {
  return moment(dateString).format("YYYY-MM-DD HH:mm");
}
