package com.app.appplatform.controller;

import com.app.appplatform.common.Result;
import com.app.appplatform.entity.DynamicConfig;
import com.app.appplatform.entity.DynamicConfigHistory;
import com.app.appplatform.entity.DynamicConfigItem;
import com.app.appplatform.entity.DynamicConfigItemGrayUser;
import com.app.appplatform.entity.DynamicConfigItemHistory;
import com.app.appplatform.service.DynamicConfigService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.security.PermitAll;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Collections;
import java.util.List;

@Tag(name = "动态配置管理", description = "管理客户端动态配置的上传、查询和删除，支持按配置项灰度下发")
@RestController
@RequestMapping("/api-dynamic-config")
public class DynamicConfigController {

    final private DynamicConfigService dynamicConfigService;

    DynamicConfigController(DynamicConfigService dynamicConfigService) {
        this.dynamicConfigService = dynamicConfigService;
    }

    @Operation(summary = "上传动态配置", description = "上传整包 JSON，服务端拆解为配置项入库")
    @PostMapping("/upload")
    public Result<DynamicConfig> uploadConfig(
            @Parameter(description = "配置JSON文件", required = true) @RequestParam("file") MultipartFile file,
            @Parameter(description = "版本范围，如 1.0.0-2.0.0 或 1.5.0", required = true) @RequestParam("versionRange") String versionRange,
            @Parameter(description = "环境类型: prod/test", required = true) @RequestParam("env") String env,
            @Parameter(description = "备注信息") @RequestParam(value = "remark", required = false) String remark) throws Exception {
        DynamicConfig config = dynamicConfigService.uploadConfig(file, versionRange, env, remark);
        return Result.success("上传成功", config);
    }

    @Operation(summary = "更新动态配置", description = "更新元数据和/或整包 JSON 内容")
    @PutMapping("/{id}")
    public Result<DynamicConfig> updateConfig(
            @Parameter(description = "配置ID", required = true) @PathVariable Long id,
            @Parameter(description = "配置JSON文件") @RequestParam(value = "file", required = false) MultipartFile file,
            @Parameter(description = "版本范围") @RequestParam(value = "versionRange", required = false) String versionRange,
            @Parameter(description = "环境类型: prod/test") @RequestParam(value = "env", required = false) String env,
            @Parameter(description = "备注信息") @RequestParam(value = "remark", required = false) String remark) throws Exception {
        DynamicConfig config = dynamicConfigService.updateConfig(id, file, versionRange, env, remark);
        return Result.success("更新成功", config);
    }

    @Operation(summary = "获取所有配置列表")
    @GetMapping("/list")
    public Result<List<DynamicConfig>> listAllConfigs(
            @Parameter(description = "环境类型") @RequestParam(value = "env", required = false) String env) {
        List<DynamicConfig> configs = dynamicConfigService.getAllConfigs(env);
        return Result.success("获取成功", configs);
    }

    @Operation(summary = "获取最新的匹配配置（客户端）", description = "按版本范围、环境筛选最新配置，并按 username 灰度过滤后拼装 JSON 返回")
    @PermitAll
    @GetMapping("/match")
    public Result<Object> getLatestConfigByVersion(
            @Parameter(description = "客户端版本号", required = true) @RequestParam("version") String version,
            @Parameter(description = "环境类型", required = true) @RequestParam(value = "env", defaultValue = "prod") String env,
            @Parameter(description = "请求用户名（灰度判定用，可不传）") @RequestParam(value = "username", required = false) String username) throws Exception {
        Object content = dynamicConfigService.matchConfigContent(version, env, username);
        if (content == null) {
            return Result.error(404, "未找到适用于该版本及环境的配置");
        }
        return Result.success("获取成功", content);
    }

    @Operation(summary = "删除动态配置", description = "删除配置及其全部配置项与灰度名单")
    @DeleteMapping("/{id}")
    public Result<Void> deleteConfig(
            @Parameter(description = "配置ID", required = true) @PathVariable Long id) throws Exception {
        dynamicConfigService.deleteConfig(id);
        return Result.success("删除成功", null);
    }

    @Operation(summary = "获取配置内容（管理端）", description = "全量返回，不做灰度过滤")
    @GetMapping("/{id}/content")
    public Result<Object> getConfigContent(
            @Parameter(description = "配置ID", required = true) @PathVariable Long id,
            @Parameter(description = "环境类型") @RequestParam(value = "env", required = false) String env) throws Exception {
        Object content = dynamicConfigService.getConfigContent(id, env);
        return Result.success("获取成功", content);
    }

    // ==================== 配置项管理 ====================

    @Operation(summary = "配置项列表", description = "扁平列表，支持按环境、版本范围、域、关键字筛选")
    @GetMapping("/items")
    public Result<List<DynamicConfigItem>> getItems(
            @Parameter(description = "环境类型") @RequestParam(value = "env", required = false) String env,
            @Parameter(description = "版本范围（模糊）") @RequestParam(value = "versionRange", required = false) String versionRange,
            @Parameter(description = "所属域") @RequestParam(value = "domain", required = false) String domain,
            @Parameter(description = "关键字（匹配域/配置项/备注）") @RequestParam(value = "keyword", required = false) String keyword) {
        List<DynamicConfigItem> items = dynamicConfigService.getItems(env, versionRange, domain, keyword);
        return Result.success("获取成功", items);
    }

    @Operation(summary = "更新配置项", description = "更新值/启用状态/灰度开关/灰度百分比/备注；不含名单")
    @PutMapping("/item/{id}")
    public Result<DynamicConfigItem> updateItem(
            @Parameter(description = "配置项ID", required = true) @PathVariable Long id,
            @RequestBody DynamicConfigItem item,
            @Parameter(description = "操作人") @RequestParam(value = "operator", defaultValue = "system") String operator) throws Exception {
        DynamicConfigItem updated = dynamicConfigService.updateItem(id, item, operator);
        return Result.success("更新成功", updated);
    }

    @Operation(summary = "新增配置项", description = "按环境+版本范围新增配置项；对应配置不存在时自动创建。值需为合法 JSON，同配置下同名同版本范围不可重复")
    @PostMapping("/item")
    public Result<DynamicConfigItem> addItem(
            @RequestBody DynamicConfigItem item,
            @Parameter(description = "操作人") @RequestParam(value = "operator", defaultValue = "system") String operator) throws Exception {
        return Result.success("新增成功", dynamicConfigService.addItem(item, operator));
    }

    @Operation(summary = "删除配置项", description = "级联删除其灰度名单，并写一条 DELETE 历史")
    @DeleteMapping("/item/{id}")
    public Result<Void> deleteItem(
            @Parameter(description = "配置项ID", required = true) @PathVariable Long id,
            @Parameter(description = "操作人") @RequestParam(value = "operator", defaultValue = "system") String operator) throws Exception {
        dynamicConfigService.deleteItem(id, operator);
        return Result.success("删除成功", null);
    }

    // ==================== 灰度名单 ====================

    @Operation(summary = "查询灰度名单", description = "按配置项与名单类型查询，支持按用户名搜索")
    @GetMapping("/item/{itemId}/gray-user")
    public Result<List<DynamicConfigItemGrayUser>> getGrayUsers(
            @Parameter(description = "配置项ID", required = true) @PathVariable Long itemId,
            @Parameter(description = "名单类型：WHITE/BLACK") @RequestParam(value = "listType", required = false) String listType,
            @Parameter(description = "用户名关键字") @RequestParam(value = "keyword", required = false) String keyword) {
        List<DynamicConfigItemGrayUser> users = dynamicConfigService.getGrayUsers(itemId, listType, keyword);
        return Result.success("获取成功", users);
    }

    @Operation(summary = "批量新增灰度名单", description = "usernames 支持逗号/分号/换行分隔，幂等返回实际新增条数")
    @PostMapping("/item/{itemId}/gray-user")
    public Result<Integer> addGrayUsers(
            @Parameter(description = "配置项ID", required = true) @PathVariable Long itemId,
            @Parameter(description = "名单类型：WHITE/BLACK", required = true) @RequestParam("listType") String listType,
            @Parameter(description = "用户名列表（逗号/分号/换行分隔）", required = true) @RequestParam("usernames") String usernames,
            @Parameter(description = "操作人") @RequestParam(value = "operator", defaultValue = "system") String operator) throws Exception {
        int added = dynamicConfigService.addGrayUsers(itemId, listType, Collections.singletonList(usernames), operator);
        return Result.success("新增成功", added);
    }

    @Operation(summary = "删除单条名单")
    @DeleteMapping("/gray-user/{id}")
    public Result<Void> deleteGrayUser(
            @Parameter(description = "名单ID", required = true) @PathVariable Long id,
            @Parameter(description = "操作人") @RequestParam(value = "operator", defaultValue = "system") String operator) throws Exception {
        dynamicConfigService.deleteGrayUser(id, operator);
        return Result.success("删除成功", null);
    }

    @Operation(summary = "清空指定类型的名单")
    @DeleteMapping("/item/{itemId}/gray-user")
    public Result<Void> clearGrayUsers(
            @Parameter(description = "配置项ID", required = true) @PathVariable Long itemId,
            @Parameter(description = "名单类型：WHITE/BLACK", required = true) @RequestParam("listType") String listType,
            @Parameter(description = "操作人") @RequestParam(value = "operator", defaultValue = "system") String operator) throws Exception {
        dynamicConfigService.clearGrayUsers(itemId, listType, operator);
        return Result.success("清空成功", null);
    }

    @Operation(summary = "反查用户命中的配置项", description = "按用户名查询其所在名单对应的配置项ID列表")
    @GetMapping("/gray-user/by-username")
    public Result<List<Long>> findItemIdsByUsername(
            @Parameter(description = "用户名", required = true) @RequestParam("username") String username) {
        return Result.success("获取成功", dynamicConfigService.findItemIdsByUsername(username));
    }

    // ==================== 整包历史 ====================

    @Operation(summary = "获取指定配置的历史版本列表")
    @GetMapping("/{id}/history")
    public Result<List<DynamicConfigHistory>> getConfigHistory(
            @Parameter(description = "配置ID", required = true) @PathVariable Long id) {
        List<DynamicConfigHistory> histories = dynamicConfigService.getConfigHistory(id);
        return Result.success("获取成功", histories);
    }

    @Operation(summary = "获取所有历史版本", description = "支持按环境、版本范围筛选")
    @GetMapping("/history/all")
    public Result<List<DynamicConfigHistory>> getAllHistory(
            @Parameter(description = "环境类型") @RequestParam(value = "env", required = false) String env,
            @Parameter(description = "版本范围（模糊）") @RequestParam(value = "versionRange", required = false) String versionRange) {
        List<DynamicConfigHistory> histories = dynamicConfigService.getAllHistory(env, versionRange);
        return Result.success("获取成功", histories);
    }

    @Operation(summary = "获取历史版本详情")
    @GetMapping("/history/{historyId}")
    public Result<DynamicConfigHistory> getHistoryById(
            @Parameter(description = "历史ID", required = true) @PathVariable Long historyId) {
        DynamicConfigHistory history = dynamicConfigService.getHistoryById(historyId);
        if (history == null) {
            return Result.error(404, "历史版本不存在");
        }
        return Result.success("获取成功", history);
    }

    @Operation(summary = "获取历史版本内容", description = "解析整包快照返回 {metadata, configs}；旧版文件存储的历史不支持")
    @GetMapping("/history/{historyId}/content")
    public Result<Object> getHistoryContent(
            @Parameter(description = "历史ID", required = true) @PathVariable Long historyId) throws Exception {
        Object content = dynamicConfigService.getHistoryContent(historyId);
        return Result.success("获取成功", content);
    }

    @Operation(summary = "整包回溯", description = "把整份配置回滚到某次发布的状态（覆盖全部配置项与名单）")
    @PostMapping("/{configId}/revert/{historyId}")
    public Result<DynamicConfig> revertToHistory(
            @Parameter(description = "配置ID", required = true) @PathVariable Long configId,
            @Parameter(description = "历史ID", required = true) @PathVariable Long historyId,
            @Parameter(description = "操作人") @RequestParam(value = "operator", defaultValue = "system") String operator) throws Exception {
        DynamicConfig config = dynamicConfigService.revertToHistory(configId, historyId, operator);
        return Result.success("回溯成功", config);
    }

    // ==================== 配置项级历史与单项回溯 ====================

    @Operation(summary = "配置项变更历史", description = "按业务键查询单项历史，时间倒序")
    @GetMapping("/item/{itemId}/history")
    public Result<List<DynamicConfigItemHistory>> getItemHistory(
            @Parameter(description = "配置项ID", required = true) @PathVariable Long itemId) {
        return Result.success("获取成功", dynamicConfigService.getItemHistory(itemId));
    }

    @Operation(summary = "配置项历史详情")
    @GetMapping("/item-history/{historyId}")
    public Result<DynamicConfigItemHistory> getItemHistoryById(
            @Parameter(description = "历史ID", required = true) @PathVariable Long historyId) {
        DynamicConfigItemHistory history = dynamicConfigService.getItemHistoryById(historyId);
        if (history == null) {
            return Result.error(404, "历史记录不存在");
        }
        return Result.success("获取成功", history);
    }

    @Operation(summary = "单项回溯", description = "把指定历史的状态写回该配置项（含名单），仅影响该配置项")
    @PostMapping("/item/{itemId}/revert/{historyId}")
    public Result<DynamicConfigItem> revertItemToHistory(
            @Parameter(description = "配置项ID", required = true) @PathVariable Long itemId,
            @Parameter(description = "历史ID", required = true) @PathVariable Long historyId,
            @Parameter(description = "操作人") @RequestParam(value = "operator", defaultValue = "system") String operator) throws Exception {
        return Result.success("回溯成功", dynamicConfigService.revertItemToHistory(itemId, historyId, operator));
    }
}
