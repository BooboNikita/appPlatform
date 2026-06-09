import request from "@/utils/request";

export interface CrashReport {
  id: number;
  crashId: string;
  appId: string;
  userId: string;
  sessionId: string;
  crashType: string;
  message: string;
  stackTrace: string;
  appVersion: string;
  appBuildNumber: string;
  deviceInfo: string | null;
  deviceModel: string | null;
  deviceBrand: string | null;
  osVersion: string | null;
  platform: string | null;
  screenResolution: string | null;
  totalMemory: string | null;
  availableMemory: string | null;
  networkType: string | null;
  batteryLevel: string | null;
  customData: string;
  crashTimestamp: string;
  reportTimestamp: string;
  sdkVersion: string;
  deleted: string | null;
  deletedAt: string | null;
  createdAt: string;
}

interface PageParams {
  pageNum: number;
  pageSize: number;
  appId?: string;
  crashType?: string;
  username?: string;
  startDate?: string;
  endDate?: string;
}

interface PageResult<T> {
  list: T[];
  total: number;
  pageNum: number;
  pageSize: number;
  pages?: number;
}

const prefix = "/api/crash";

export const getCrashList = (params: PageParams) => {
  return request.get<PageResult<CrashReport>>(`${prefix}/list`, { params });
};

export const getCrashById = (id: number) => {
  return request.get<CrashReport>(`${prefix}/${id}`);
};

export const deleteCrash = (id: number) => {
  return request.delete<string>(`${prefix}/${id}`);
};

export const getCrashStatistics = (params?: {
  startDate?: string;
  endDate?: string;
}) => {
  return request.get<Record<string, any>>(`${prefix}/statistics`, { params });
};
