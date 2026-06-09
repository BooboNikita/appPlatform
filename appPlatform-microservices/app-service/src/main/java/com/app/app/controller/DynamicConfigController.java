package com.app.app.controller;

import com.app.app.entity.DynamicConfig;
import com.app.app.entity.DynamicConfigHistory;
import com.app.app.service.DynamicConfigService;
import com.app.common.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Tag(name = "动态配置管理", description = "管理客户端动态配置文件的上传、查询和删除")
@RestController
@RequestMapping("/api-dynamic-config")
public class DynamicConfigController {

    final private DynamicConfigService dynamicConfigService;

    @Autowired
    DynamicConfigController(DynamicConfigService dynamicConfigService) {
        this.dynamicConfigService = dynamicConfigService;
    }

    @Operation(summary = "上传动态配置", description = "上传一个新的动态配置文件并指定版本范围和环境")
    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Result<DynamicConfig> uploadConfig(
            @RequestParam("file") MultipartFile file,
            @RequestParam("versionRange") String versionRange,
            @RequestParam(value = "env", defaultValue = "prod") String env,
            @RequestParam(value = "remark", required = false) String remark) throws Exception {

        DynamicConfig config = dynamicConfigService.uploadConfig(file, versionRange, env, remark);
        return Result.success("上传成功", config);
    }

    @Operation(summary = "更新动态配置", description = "更新现有的动态配置元数据或文件")
    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Result<DynamicConfig> updateConfig(
            @PathVariable Long id,
            @RequestParam(value = "file", required = false) MultipartFile file,
            @RequestParam(value = "versionRange", required = false) String versionRange,
            @RequestParam(value = "env", required = false) String env,
            @RequestParam(value = "remark", required = false) String remark) throws Exception {

        DynamicConfig config = dynamicConfigService.updateConfig(id, file, versionRange, env, remark);
        return Result.success("更新成功", config);
    }

    @Operation(summary = "获取所有配置", description = "获取动态配置的元数据列表，支持按环境筛选")
    @GetMapping("/list")
    public Result<List<DynamicConfig>> getAllConfigs(@RequestParam(value = "env", required = false) String env) {
        List<DynamicConfig> configs = dynamicConfigService.getAllConfigs(env);
        return Result.success(configs);
    }

    @Operation(summary = "根据版本获取最新配置内容", description = "根据客户端版本号和环境获取适用的一条最新动态配置文件的 JSON 内容")
    @GetMapping("/match")
    public Result<Object> getLatestConfigByVersion(
            @RequestParam String version,
            @RequestParam(value = "env", defaultValue = "prod") String env) throws Exception {
        DynamicConfig config = dynamicConfigService.getLatestConfigByVersion(version, env);
        if (config == null) {
            return Result.error(404, "未找到适用于该版本及环境的配置");
        }
        Object content = dynamicConfigService.getConfigContent(config.getId(), env);
        return Result.success("获取成功", content);
    }

    @Operation(summary = "删除配置", description = "删除指定的动态配置及其对应的文件")
    @DeleteMapping("/{id}")
    public Result<Void> deleteConfig(@PathVariable Long id) throws Exception {
        dynamicConfigService.deleteConfig(id);
        return Result.success("删除成功", null);
    }

    @Operation(summary = "获取配置文件内容", description = "根据 ID 直接读取并返回 MinIO 中的配置文件 JSON 内容")
    @GetMapping("/{id}/content")
    public Result<Object> getConfigContent(
            @PathVariable Long id,
            @RequestParam(value = "env", defaultValue = "prod") String env) throws Exception {
        Object content = dynamicConfigService.getConfigContent(id, env);
        return Result.success("获取成功", content);
    }

    // 历史版本相关接口

    @Operation(summary = "获取配置历史版本列表", description = "获取指定配置的所有历史版本记录")
    @GetMapping("/{id}/history")
    public Result<List<DynamicConfigHistory>> getConfigHistory(@PathVariable Long id) {
        List<DynamicConfigHistory> history = dynamicConfigService.getConfigHistory(id);
        return Result.success("获取成功", history);
    }

    @Operation(summary = "获取所有历史版本", description = "获取所有配置的历史版本记录，支持按环境和版本范围筛选")
    @GetMapping("/history/all")
    public Result<List<DynamicConfigHistory>> getAllHistory(
            @RequestParam(value = "env", required = false) String env,
            @RequestParam(value = "versionRange", required = false) String versionRange) {
        List<DynamicConfigHistory> history = dynamicConfigService.getAllHistory(env, versionRange);
        return Result.success("获取成功", history);
    }

    @Operation(summary = "回溯到历史版本", description = "将指定配置回溯到某个历史版本")
    @PostMapping("/{configId}/revert/{historyId}")
    public Result<DynamicConfig> revertToHistory(
            @PathVariable Long configId,
            @PathVariable Long historyId,
            @RequestParam(value = "operator", defaultValue = "system") String operator) throws Exception {
        DynamicConfig config = dynamicConfigService.revertToHistory(configId, historyId, operator);
        return Result.success("回溯成功", config);
    }

    @Operation(summary = "获取历史版本详情", description = "获取指定历史版本的详细信息")
    @GetMapping("/history/{historyId}")
    public Result<DynamicConfigHistory> getHistoryById(@PathVariable Long historyId) {
        DynamicConfigHistory history = dynamicConfigService.getHistoryById(historyId);
        if (history == null) {
            return Result.error(404, "历史版本不存在");
        }
        return Result.success("获取成功", history);
    }

    @Operation(summary = "获取历史版本配置内容", description = "获取指定历史版本的配置文件内容")
    @GetMapping("/history/{historyId}/content")
    public Result<Object> getHistoryContent(@PathVariable Long historyId) throws Exception {
        Object content = dynamicConfigService.getHistoryContent(historyId);
        return Result.success("获取成功", content);
    }
}
