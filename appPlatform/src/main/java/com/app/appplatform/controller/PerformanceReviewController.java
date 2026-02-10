package com.app.appplatform.controller;

import com.app.appplatform.common.Result;
import com.app.appplatform.entity.PerformanceReview;
import com.app.appplatform.service.PerformanceReviewService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.security.PermitAll;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 绩效评估配置管理控制器
 */
@Tag(name = "绩效评估配置管理", description = "管理不同部门的封面图和截止时间配置")
@RestController
@RequestMapping("/api-performance-review")
public class PerformanceReviewController {

    private final PerformanceReviewService performanceReviewService;

    @Autowired
    public PerformanceReviewController(PerformanceReviewService performanceReviewService) {
        this.performanceReviewService = performanceReviewService;
    }

    @Operation(summary = "获取部门封面图", description = "根据部门ID获取对应的封面图")
    @GetMapping("/cover/{deptId}")
    @PermitAll
    public Result<String> getCoverImage(@PathVariable String deptId) {
        String coverImage = performanceReviewService.getCoverImage(deptId);
        if (coverImage == null) {
            return Result.error(404, "未找到该部门的配置信息");
        }
        return Result.success("获取成功", coverImage);
    }

    @Operation(summary = "获取部门截止时间", description = "根据部门ID获取对应的截止时间")
    @GetMapping("/deadline/{deptId}")
    @PermitAll
    public Result<String> getDeadline(@PathVariable String deptId) {
        String deadline = performanceReviewService.getDeadline(deptId);
        if (deadline == null) {
            return Result.error(404, "未找到该部门的配置信息");
        }
        return Result.success("获取成功", deadline);
    }

    @Operation(summary = "获取组织名称", description = "根据部门ID获取对应的组织名称")
    @GetMapping("/name/{deptId}")
    @PermitAll
    public Result<String> getName(@PathVariable String deptId) {
        String name = performanceReviewService.getName(deptId);
        if (name == null) {
            return Result.error(404, "未找到该部门的配置信息");
        }
        return Result.success("获取成功", name);
    }

    @Operation(summary = "获取部门完整配置", description = "根据部门ID获取封面图和截止时间的完整配置")
    @GetMapping("/config/{deptId}")
    @PermitAll
    public Result<PerformanceReview> getDeptConfig(@PathVariable String deptId) {
        PerformanceReview config = performanceReviewService.getDeptConfig(deptId);
        if (config == null) {
            return Result.error(404, "未找到该部门的配置信息");
        }
        return Result.success("获取成功", config);
    }

    @Operation(summary = "设置部门封面图", description = "为指定部门设置封面图")
    @PostMapping("/cover/{deptId}")
    public Result<String> setCoverImage(
            @PathVariable String deptId,
            @RequestParam String coverImage,
            @RequestParam(defaultValue = "admin") String operator) {
        
        PerformanceReview config = performanceReviewService.setCoverImage(deptId, coverImage, operator);
        return Result.success("设置成功", config.getCoverImage());
    }

    @Operation(summary = "设置部门截止时间", description = "为指定部门设置截止时间")
    @PostMapping("/deadline/{deptId}")
    public Result<String> setDeadline(
            @PathVariable String deptId,
            @RequestParam String deadline,
            @RequestParam(defaultValue = "admin") String operator) {
        
        PerformanceReview config = performanceReviewService.setDeadline(deptId, deadline, operator);
        return Result.success("设置成功", config.getDeadline());
    }

    @Operation(summary = "设置组织名称", description = "为指定部门设置组织名称")
    @PostMapping("/name/{deptId}")
    public Result<String> setName(
            @PathVariable String deptId,
            @RequestParam String name,
            @RequestParam(defaultValue = "admin") String operator) {
        
        PerformanceReview config = performanceReviewService.setName(deptId, name, operator);
        return Result.success("设置成功", config.getName());
    }

    @Operation(summary = "更新部门完整配置", description = "同时更新部门的封面图和截止时间")
    @PutMapping("/config/{deptId}")
    public Result<PerformanceReview> updateDeptConfig(
            @PathVariable String deptId,
            @RequestBody PerformanceReview performanceReview) {
        
        PerformanceReview config = performanceReviewService.updateDeptConfig(deptId, performanceReview);
        return Result.success("更新成功", config);
    }

    @Operation(summary = "删除部门配置", description = "删除指定部门的配置信息")
    @DeleteMapping("/config/{deptId}")
    public Result<Void> deleteDeptConfig(@PathVariable String deptId) {
        boolean deleted = performanceReviewService.deleteDeptConfig(deptId);
        if (!deleted) {
            return Result.error(404, "未找到该部门的配置信息");
        }
        return Result.success("删除成功", null);
    }

    @Operation(summary = "获取所有部门配置", description = "获取所有部门的配置信息列表")
    @GetMapping("/list")
    public Result<List<PerformanceReview>> getAllConfigs() {
        List<PerformanceReview> configs = performanceReviewService.getAllConfigs();
        return Result.success("获取成功", configs);
    }

    @Operation(summary = "批量获取部门配置", description = "根据部门ID列表批量获取配置信息")
    @PostMapping("/batch")
    @PermitAll
    public Result<Map<String, PerformanceReview>> getBatchConfigs(@RequestBody List<String> deptIds) {
        Map<String, PerformanceReview> configs = performanceReviewService.getBatchConfigs(deptIds);
        return Result.success("获取成功", configs);
    }

    @Operation(summary = "检查部门配置是否存在", description = "检查指定部门的配置是否存在")
    @GetMapping("/exists/{deptId}")
    @PermitAll
    public Result<Boolean> existsByDeptId(@PathVariable String deptId) {
        boolean exists = performanceReviewService.existsByDeptId(deptId);
        return Result.success("检查完成", exists);
    }
}
