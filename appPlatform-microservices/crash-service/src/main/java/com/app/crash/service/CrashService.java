package com.app.crash.service;

import com.app.common.PageResult;
import com.app.crash.dto.CrashReportRequest;
import com.app.crash.entity.CrashReport;
import com.app.crash.mapper.CrashReportMapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class CrashService {

    @Autowired
    private CrashReportMapper crashReportMapper;

    private static final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 提交崩溃报告
     */
    public CrashReport submitReport(CrashReportRequest request) {
        // 生成 crashId
        String crashId = request.getCrashId();
        if (!StringUtils.hasText(crashId)) {
            crashId = UUID.randomUUID().toString().replace("-", "");
        }

        // 构建实体
        CrashReport report = new CrashReport();
        report.setCrashId(crashId);
        report.setUserId(request.getUsername());
        report.setSessionId(request.getSessionId());
        report.setCrashType(request.getCrashType());
        report.setMessage(request.getMessage());
        report.setStackTrace(request.getStackTrace());
        report.setAppVersion(request.getAppVersion());
        report.setAppBuildNumber(request.getAppBuildNumber());

        // 自定义数据
        Map<String, Object> customData = request.getCustomData();
        try {
            if (customData != null && !customData.isEmpty()) {
                report.setCustomData(objectMapper.writeValueAsString(customData));
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to serialize custom data: " + e.getMessage(), e);
        }

        // 时间戳处理
        if (request.getTimestamp() != null) {
            ZonedDateTime zonedDateTime = request.getTimestamp().atZone(ZoneOffset.UTC);
            report.setCrashTimestamp(zonedDateTime.toLocalDateTime());
        }
        report.setReportTimestamp(LocalDateTime.now());
        report.setCreatedAt(LocalDateTime.now());
        report.setDeleted(false);

        crashReportMapper.insert(report);
        return report;
    }

    /**
     * 判断是否为重复上报
     */
    public boolean isDuplicate(String crashId) {
        return crashId != null && crashReportMapper.countByCrashId(crashId) > 0;
    }

    /**
     * 分页查询崩溃列表
     */
    public PageResult<CrashReport> list(
            Integer pageNum, Integer pageSize,
            String appId, String crashType, String username,
            LocalDateTime startDate, LocalDateTime endDate) {

        List<CrashReport> records = crashReportMapper.findByConditions(
                appId, crashType, username, startDate, endDate);

        long total = crashReportMapper.countByConditions(
                appId, crashType, username, startDate, endDate);

        // 手动分页
        int start = (pageNum - 1) * pageSize;
        List<CrashReport> pagedRecords = records.stream()
                .skip(start)
                .limit(pageSize)
                .collect(Collectors.toList());

        long totalPages = (total + pageSize - 1) / pageSize;

        return new PageResult<>(pagedRecords, total, pageNum, pageSize, (int) totalPages);
    }

    /**
     * 根据 ID 查询详情
     */
    public CrashReport findById(Long id) {
        return crashReportMapper.findById(id);
    }

    /**
     * 删除崩溃报告（逻辑删除）
     */
    public boolean deleteById(Long id) {
        return crashReportMapper.deleteById(id) > 0;
    }

    /**
     * 获取崩溃统计信息
     */
    public Map<String, Object> getStats(String appId, LocalDateTime startDate, LocalDateTime endDate) {
        List<CrashReport> reports = crashReportMapper.findByConditions(
                appId, null, null, startDate, endDate);

        Map<String, Object> stats = new HashMap<>();
        stats.put("total", reports.size());

        // 按类型统计
        Map<String, Long> typeCount = reports.stream()
                .collect(Collectors.groupingBy(
                        r -> r.getCrashType() != null ? r.getCrashType() : "unknown",
                        Collectors.counting()));
        stats.put("byType", typeCount);

        // 按平台统计
        Map<String, Long> platformCount = reports.stream()
                .collect(Collectors.groupingBy(
                        r -> r.getPlatform() != null ? r.getPlatform() : "unknown",
                        Collectors.counting()));
        stats.put("byPlatform", platformCount);

        return stats;
    }
}
