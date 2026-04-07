package com.app.appplatform.service;

import com.app.appplatform.common.PageResult;
import com.app.appplatform.dto.CrashReportRequest;
import com.app.appplatform.dto.CrashReportResponse;
import com.app.appplatform.entity.CrashReport;
import com.app.appplatform.util.DeviceInfoUtil;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Flutter Crash SDK Service Interface
 * 2024-04-07 API Protocol
 */
public interface CrashService {

    /**
     * Flutter SDK - Process crash report
     * 
     * @param request Flutter SDK crash report request
     * @param deviceInfo Device information from header
     * @return Crash report response
     * @throws IllegalArgumentException Validation failed
     */
    CrashReportResponse processCrashReport(CrashReportRequest request, DeviceInfoUtil.DeviceInfoData deviceInfo);

    /**
     * Get crash report list with pagination and filtering
     * 
     * @param pageNum Page number
     * @param pageSize Page size
     * @param appId Application ID filter (optional)
     * @param crashType Crash type filter (optional)
     * @param username Username filter (optional)
     * @param startDate Start date filter (optional)
     * @param endDate End date filter (optional)
     * @return Paginated crash report list
     */
    PageResult<CrashReport> getCrashList(int pageNum, int pageSize, String appId, 
                                         String crashType, String username, 
                                         LocalDateTime startDate, LocalDateTime endDate);

    /**
     * Get crash report by ID
     * 
     * @param id Crash report ID
     * @return Crash report details
     */
    CrashReport getCrashById(Long id);

    /**
     * Delete crash report by ID
     * 
     * @param id Crash report ID
     */
    void deleteCrashById(Long id);

    /**
     * Get crash statistics
     * 
     * @param startDate Start date filter (optional)
     * @param endDate End date filter (optional)
     * @return Crash statistics
     */
    Map<String, Object> getCrashStatistics(LocalDateTime startDate, LocalDateTime endDate);
}
