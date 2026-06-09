package com.app.crash.controller;

import com.app.common.PageResult;
import com.app.common.Result;
import com.app.crash.dto.CrashReportRequest;
import com.app.crash.dto.CrashReportResponse;
import com.app.crash.entity.CrashReport;
import com.app.crash.service.CrashService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;

@RestController
@RequestMapping("/api/crash")
public class CrashController {

    @Autowired
    private CrashService crashService;

    /**
     * 提交崩溃报告
     */
    @PostMapping("/report")
    public Result<CrashReportResponse> report(@RequestBody CrashReportRequest request) {
        try {
            if (crashService.isDuplicate(request.getCrashId())) {
                CrashReportResponse response = new CrashReportResponse(true, "Duplicate report ignored", request.getCrashId());
                return Result.success(response);
            }

            CrashReport report = crashService.submitReport(request);
            CrashReportResponse response = new CrashReportResponse(true, "Crash report submitted successfully", report.getCrashId());
            return Result.success(response);

        } catch (Exception e) {
            return Result.error(500, "Failed to submit crash report: " + e.getMessage());
        }
    }

    /**
     * 分页查询崩溃列表
     */
    @GetMapping("/list")
    public Result<PageResult<CrashReport>> list(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String appId,
            @RequestParam(required = false) String crashType,
            @RequestParam(required = false) String username,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime endDate) {

        try {
            PageResult<CrashReport> pageResult = crashService.list(pageNum, pageSize, appId, crashType, username, startDate, endDate);
            return Result.success(pageResult);
        } catch (Exception e) {
            return Result.error(500, "Failed to query crash list: " + e.getMessage());
        }
    }

    /**
     * 根据ID查询崩溃详情
     */
    @GetMapping("/detail/{id}")
    public Result<CrashReport> detail(@PathVariable Long id) {
        try {
            CrashReport report = crashService.findById(id);
            if (report == null) {
                return Result.error(404, "Crash report not found");
            }
            return Result.success(report);
        } catch (Exception e) {
            return Result.error(500, "Failed to query crash detail: " + e.getMessage());
        }
    }

    /**
     * 删除崩溃报告（逻辑删除）
     */
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        try {
            if (crashService.deleteById(id)) {
                return Result.success(null);
            }
            return Result.error(404, "Crash report not found");
        } catch (Exception e) {
            return Result.error(500, "Failed to delete crash report: " + e.getMessage());
        }
    }

    /**
     * 获取崩溃统计信息
     */
    @GetMapping("/stats")
    public Result<Map<String, Object>> stats(
            @RequestParam(required = false) String appId,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime endDate) {

        try {
            Map<String, Object> stats = crashService.getStats(appId, startDate, endDate);
            return Result.success(stats);
        } catch (Exception e) {
            return Result.error(500, "Failed to query crash stats: " + e.getMessage());
        }
    }
}
