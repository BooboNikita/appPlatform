package com.app.appplatform.service.impl;

import com.app.appplatform.common.PageResult;
import com.app.appplatform.dto.CrashReportRequest;
import com.app.appplatform.dto.CrashReportResponse;
import com.app.appplatform.entity.CrashReport;
import com.app.appplatform.mapper.primary.CrashReportMapper;
import com.app.appplatform.service.CrashService;
import com.app.appplatform.util.DeviceInfoUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.Page;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

@Slf4j
@Service
public class CrashServiceImpl implements CrashService {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final CrashReportMapper crashReportMapper;

    @Value("${app.package-name}")
    private String packageName;

    public CrashServiceImpl(CrashReportMapper crashReportMapper) {
        this.crashReportMapper = crashReportMapper;
    }

    @Override
    @Transactional
    public CrashReportResponse processCrashReport(CrashReportRequest request, DeviceInfoUtil.DeviceInfoData deviceInfo) {
        log.info("Flutter SDK - Processing crash report: crashId={}, crashType={}", 
                   request.getCrashId(), request.getCrashType());

        // Validate essential fields
        if (request.getCrashId() == null || request.getCrashId().trim().isEmpty()) {
            throw new IllegalArgumentException("Missing required field: crashId");
        }

        if (request.getMessage() == null || request.getMessage().trim().isEmpty()) {
            throw new IllegalArgumentException("Missing required field: message");
        }

        // Check if crash ID already exists
        if (isCrashIdExists(request.getCrashId())) {
            log.warn("Flutter SDK - Crash ID already exists: {}", request.getCrashId());
            return new CrashReportResponse(false, "Crash ID already exists", request.getCrashId());
        }

        // Convert to internal format and save
        try {
            saveCrashReport(request, deviceInfo);
            log.info("Flutter SDK - Crash report processed successfully: crashId={}", request.getCrashId());
            return new CrashReportResponse(true, null, request.getCrashId());
        } catch (Exception e) {
            log.error("Flutter SDK - Failed to save crash report: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to process crash report", e);
        }
    }

    @Override
    public PageResult<CrashReport> getCrashList(int pageNum, int pageSize, String appId, 
                                                String crashType, String username, 
                                                LocalDateTime startDate, LocalDateTime endDate) {
        log.info("Admin backend - Query crash list: pageNum={}, pageSize={}, appId={}, crashType={}, username={}, startDate={}, endDate={}", 
                 pageNum, pageSize, appId, crashType, username, startDate, endDate);

        try {
            // Query crash reports with PageHelper
            Page<CrashReport> page = PageHelper.startPage(pageNum, pageSize)
                    .doSelectPage(() -> crashReportMapper.findByConditions(
                            appId, crashType, username, startDate, endDate));
            
            // Get page info
            long total = page.getTotal();
            int pages = page.getPages();
            List<CrashReport> crashList = page.getResult();
            
            PageResult<CrashReport> pageResult = new PageResult<>(crashList, total, pageNum, pageSize, pages);
            
            log.info("Admin backend - Crash list query completed: total={}, pages={}", total, pages);
            return pageResult;
            
        } catch (Exception e) {
            log.error("Admin backend - Failed to query crash list", e);
            throw new RuntimeException("Failed to query crash list: " + e.getMessage(), e);
        }
    }

    @Override
    public CrashReport getCrashById(Long id) {
        log.info("Admin backend - Query crash detail: id={}", id);
        
        try {
            CrashReport crashReport = crashReportMapper.findById(id);
            if (crashReport == null) {
                throw new IllegalArgumentException("Crash report not found");
            }
            
            log.info("Admin backend - Crash detail query completed: id={}", id);
            return crashReport;
            
        } catch (Exception e) {
            log.error("Admin backend - Failed to query crash detail: id={}", id, e);
            throw new RuntimeException("Failed to query crash detail: " + e.getMessage(), e);
        }
    }

    @Override
    @Transactional
    public void deleteCrashById(Long id) {
        log.info("Admin backend - Delete crash report: id={}", id);
        
        try {
            CrashReport crashReport = crashReportMapper.findById(id);
            if (crashReport == null) {
                throw new IllegalArgumentException("Crash report not found");
            }
            
            crashReportMapper.deleteById(id);
            
            log.info("Admin backend - Crash report deleted successfully: id={}", id);
            
        } catch (Exception e) {
            log.error("Admin backend - Failed to delete crash report: id={}", id, e);
            throw new RuntimeException("Failed to delete crash report: " + e.getMessage(), e);
        }
    }

    @Override
    public Map<String, Object> getCrashStatistics(LocalDateTime startDate, LocalDateTime endDate) {
        log.info("Admin backend - Query crash statistics: startDate={}, endDate={}", startDate, endDate);
        
        try {
            // Get statistics
            long totalCrashes = crashReportMapper.countByConditions(null, null, null, startDate, endDate);
            
            Map<String, Object> statistics = new HashMap<>();
            statistics.put("totalCrashes", totalCrashes);
            statistics.put("startDate", startDate);
            statistics.put("endDate", endDate);
            
            log.info("Admin backend - Crash statistics query completed: totalCrashes={}", totalCrashes);
            return statistics;
            
        } catch (Exception e) {
            log.error("Admin backend - Failed to query crash statistics", e);
            throw new RuntimeException("Failed to query crash statistics: " + e.getMessage(), e);
        }
    }

    /**
     * Save crash report to database
     */
    private void saveCrashReport(CrashReportRequest request, DeviceInfoUtil.DeviceInfoData deviceInfo) {
        CrashReport crashReport = new CrashReport();
        
        // Manual field mapping to avoid BeanUtil.copyProperties issues
        crashReport.setCrashId(request.getCrashId());
        crashReport.setCrashTimestamp(request.getTimestamp()); // timestamp -> crashTimestamp
        crashReport.setCrashType(request.getCrashType());
        crashReport.setMessage(request.getMessage());
        crashReport.setStackTrace(request.getStackTrace());
        crashReport.setAppVersion(request.getAppVersion());
        crashReport.setAppBuildNumber(request.getAppBuildNumber());
        crashReport.setUserId(request.getUsername()); // username -> userId
        crashReport.setSessionId(request.getSessionId());
        
        // Set additional fields
        crashReport.setAppId(packageName); // Use configured package name
        crashReport.setReportTimestamp(LocalDateTime.now());
        crashReport.setSdkVersion("flutter_sdk_1.0.0");
        crashReport.setCreatedAt(LocalDateTime.now());
        if (deviceInfo != null) {
            crashReport.setDeviceModel(deviceInfo.getModel());
            crashReport.setDeviceBrand(deviceInfo.getBrand());
            crashReport.setOsVersion(deviceInfo.getOsVersion());
            crashReport.setPlatform(deviceInfo.getOs()); // Use os as platform
            crashReport.setScreenResolution(deviceInfo.getScreenResolution());
            crashReport.setNetworkType(deviceInfo.getNetworkType());
        }

        // Handle custom data
        if (request.getCustomData() != null) {
            try {
                String customDataJson = objectMapper.writeValueAsString(request.getCustomData());
                crashReport.setCustomData(customDataJson);
            } catch (JsonProcessingException e) {
                log.warn("Flutter SDK - Custom data serialization failed: {}", e.getMessage());
                crashReport.setCustomData("{}");
            }
        }

        // Save to database
        crashReportMapper.insert(crashReport);
    }

    /**
     * Check if crash ID already exists
     */
    private boolean isCrashIdExists(String crashId) {
        return crashReportMapper.countByCrashId(crashId) > 0;
    }

    /**
     * Validate crash type against allowed values
     * 
     * @param crashType The crash type to validate
     * @return true if valid, false otherwise
     */
    private boolean isValidCrashType(String crashType) {
        String[] validTypes = {"error", "exception", "fatal", "anr"};
        return Arrays.asList(validTypes).contains(crashType);
    }
}
