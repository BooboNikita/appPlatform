package com.app.appplatform.controller;

import com.app.appplatform.common.PageResult;
import com.app.appplatform.common.Result;
import com.app.appplatform.dto.CrashReportRequest;
import com.app.appplatform.dto.CrashReportResponse;
import com.app.appplatform.entity.CrashReport;
import com.app.appplatform.service.CrashService;
import com.app.appplatform.util.DeviceInfoUtil;
import jakarta.annotation.security.PermitAll;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

/**
 * Flutter Crash SDK API Controller
 * 2024-04-07 API Protocol
 */
@Slf4j
@RestController
@RequestMapping("/api/crash")
public class CrashController {

    private final CrashService crashService;

    public CrashController(CrashService crashService) {
        this.crashService = crashService;
    }

    /**
     * Flutter Crash SDK - Crash Report Endpoint
     * URL: POST /api/crash
     * Content-Type: application/json
     * 
     * @param request CrashReportRequest containing crash data
     * @param headers HTTP headers containing deviceInfo
     * @return Result containing CrashReportResponse as data
     */
    @PostMapping
    @PermitAll
    public Result<CrashReportResponse> reportCrash(@RequestBody CrashReportRequest request,
                                                   @RequestHeader HttpHeaders headers) {
        try {
            log.info("Flutter SDK - Received crash report: crashId={}, crashType={}", 
                       request.getCrashId(), request.getCrashType());

            // Parse device info from header
            DeviceInfoUtil.DeviceInfoData deviceInfo = null;
            String deviceInfoHeader = headers.getFirst("deviceInfo");
            if (deviceInfoHeader != null) {
                deviceInfo = DeviceInfoUtil.parseDeviceInfo(deviceInfoHeader);
                log.info("Flutter SDK - Device info parsed: deviceId={}, brand={}, model={}", 
                         deviceInfo.getDeviceId(), deviceInfo.getBrand(), deviceInfo.getModel());
            }

            // Delegate to service layer
            CrashReportResponse response = crashService.processCrashReport(request, deviceInfo);
            
            log.info("Flutter SDK - Crash report processed successfully: crashId={}", request.getCrashId());
            return Result.success(response);

        } catch (IllegalArgumentException e) {
            log.warn("Flutter SDK - Validation failed: {}", e.getMessage());
            return Result.error(400, e.getMessage());

        } catch (Exception e) {
            log.error("Flutter SDK - Error processing crash report", e);
            return Result.error(500, "Internal server error");
        }
    }

    /**
     * Get crash report list for admin backend
     * URL: GET /api/crash/list
     * 
     * @param pageNum Page number (default: 1)
     * @param pageSize Page size (default: 10)
     * @param appId Application ID filter (optional)
     * @param crashType Crash type filter (optional)
     * @param username Username filter (optional)
     * @param startDate Start date filter (optional)
     * @param endDate End date filter (optional)
     * @return Paginated crash report list
     */
    @GetMapping("/list")
    public Result<PageResult<CrashReport>> getCrashList(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) String appId,
            @RequestParam(required = false) String crashType,
            @RequestParam(required = false) String username,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {
        
        try {
            log.info("Admin backend - Query crash list: pageNum={}, pageSize={}, appId={}, crashType={}, username={}, startDate={}, endDate={}", 
                     pageNum, pageSize, appId, crashType, username, startDate, endDate);

            // Convert date strings if provided
            LocalDateTime startDateTime = null;
            LocalDateTime endDateTime = null;
            
            if (startDate != null && !startDate.isEmpty()) {
                startDateTime = LocalDateTime.parse(startDate, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
            }
            if (endDate != null && !endDate.isEmpty()) {
                endDateTime = LocalDateTime.parse(endDate, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
            }

            // Delegate to service layer
            PageResult<CrashReport> pageResult = crashService.getCrashList(
                    pageNum, pageSize, appId, crashType, username, startDateTime, endDateTime);
            
            log.info("Admin backend - Crash list query completed: total={}, pages={}", 
                     pageResult.getTotal(), pageResult.getPages());
            return Result.success(pageResult);
            
        } catch (IllegalArgumentException e) {
            log.warn("Admin backend - Query crash list validation failed: {}", e.getMessage());
            return Result.error(400, e.getMessage());
        } catch (Exception e) {
            log.error("Admin backend - Failed to query crash list", e);
            return Result.error(500, "Failed to query crash list: " + e.getMessage());
        }
    }

    /**
     * Get crash report details by ID
     * URL: GET /api/crash/{id}
     * 
     * @param id Crash report ID
     * @return Crash report details
     */
    @GetMapping("/{id}")
    public Result<CrashReport> getCrashDetail(@PathVariable Long id) {
        try {
            log.info("Admin backend - Query crash detail: id={}", id);
            
            // Delegate to service layer
            CrashReport crashReport = crashService.getCrashById(id);
            
            log.info("Admin backend - Crash detail query completed: id={}", id);
            return Result.success(crashReport);
            
        } catch (IllegalArgumentException e) {
            log.warn("Admin backend - Crash detail not found: id={}", id);
            return Result.error(404, e.getMessage());
        } catch (Exception e) {
            log.error("Admin backend - Failed to query crash detail: id={}", id, e);
            return Result.error(500, "Failed to query crash detail: " + e.getMessage());
        }
    }

    /**
     * Delete crash report by ID
     * URL: DELETE /api/crash/{id}
     * 
     * @param id Crash report ID
     * @return Deletion result
     */
    @DeleteMapping("/{id}")
    public Result<String> deleteCrash(@PathVariable Long id) {
        try {
            log.info("Admin backend - Delete crash report: id={}", id);
            
            // Delegate to service layer
            crashService.deleteCrashById(id);
            
            log.info("Admin backend - Crash report deleted successfully: id={}", id);
            return Result.success("Crash report deleted successfully");
            
        } catch (IllegalArgumentException e) {
            log.warn("Admin backend - Crash report not found for deletion: id={}", id);
            return Result.error(404, e.getMessage());
        } catch (Exception e) {
            log.error("Admin backend - Failed to delete crash report: id={}", id, e);
            return Result.error(500, "Failed to delete crash report: " + e.getMessage());
        }
    }

    /**
     * Get crash statistics for admin dashboard
     * URL: GET /api/crash/statistics
     * 
     * @param startDate Start date filter (optional)
     * @param endDate End date filter (optional)
     * @return Crash statistics
     */
    @GetMapping("/statistics")
    public Result<Map<String, Object>> getCrashStatistics(
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {
        
        try {
            log.info("Admin backend - Query crash statistics: startDate={}, endDate={}", startDate, endDate);
            
            // Convert date strings if provided
            LocalDateTime startDateTime = null;
            LocalDateTime endDateTime = null;
            
            if (startDate != null && !startDate.isEmpty()) {
                startDateTime = LocalDateTime.parse(startDate, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
            }
            if (endDate != null && !endDate.isEmpty()) {
                endDateTime = LocalDateTime.parse(endDate, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
            }

            // Delegate to service layer
            Map<String, Object> statistics = crashService.getCrashStatistics(startDateTime, endDateTime);
            
            log.info("Admin backend - Crash statistics query completed: totalCrashes={}", statistics.get("totalCrashes"));
            return Result.success(statistics);
            
        } catch (Exception e) {
            log.error("Admin backend - Failed to query crash statistics", e);
            return Result.error(500, "Failed to query crash statistics: " + e.getMessage());
        }
    }
}
