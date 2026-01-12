package com.app.appplatform.controller;

import com.app.appplatform.common.Result;
import com.app.appplatform.entity.DynamicConfig;
import com.app.appplatform.service.DynamicConfigService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.security.PermitAll;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@Tag(name = "动态配置管理", description = "管理客户端动态配置文件的上传、查询和删除")
@RestController
@RequestMapping("/api-dynamic-config")
public class DynamicConfigController {

    final private DynamicConfigService dynamicConfigService;

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
    @PermitAll
    public Result<String> getLatestConfigByVersion(
            @RequestParam String version,
            @RequestParam(value = "env", defaultValue = "prod") String env) throws Exception {
        DynamicConfig config = dynamicConfigService.getLatestConfigByVersion(version, env);
        if (config == null) {
            return Result.error(404, "未找到适用于该版本及环境的配置");
        }
        String content = dynamicConfigService.getConfigContent(config.getId());
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
    public Result<String> getConfigContent(@PathVariable Long id) throws Exception {
        String content = dynamicConfigService.getConfigContent(id);
        return Result.success("获取成功", content);
    }
}
