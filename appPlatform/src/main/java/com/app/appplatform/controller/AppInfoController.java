package com.app.appplatform.controller;

import com.app.appplatform.common.PageResult;
import com.app.appplatform.common.Result;
import com.app.appplatform.dto.AppInfoDto;
import com.app.appplatform.dto.AppVersionCheckDto;
import com.app.appplatform.entity.AppInfo;
import com.app.appplatform.enums.BucketType;
import com.app.appplatform.service.AppInfoService;
import com.app.appplatform.service.ConfigService;
import com.app.appplatform.util.FileDownloadUtil;
import com.app.appplatform.util.JsonUtil;
import com.fasterxml.jackson.core.type.TypeReference;
import jakarta.annotation.security.PermitAll;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api-app")
public class AppInfoController {

    private static final String CONFIG_KEY_APP_UPDATE_TOTAL_ENABLED = "app_update_total_enabled";

    private final AppInfoService appInfoService;

    private final FileDownloadUtil fileDownloadUtil;
    
    private final ConfigService configService;

    @Autowired
    public AppInfoController(AppInfoService appInfoService, FileDownloadUtil fileDownloadUtil, ConfigService configService) {
        this.appInfoService = appInfoService;
        this.fileDownloadUtil = fileDownloadUtil;
        this.configService = configService;
    }

    /**
     * 上传APP文件
     */
    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Result<AppInfo> uploadApp(
            @RequestParam("file") MultipartFile file,
            @RequestParam String appName,
            @RequestParam String packageName,
            @RequestParam String version,
            @RequestParam String buildNumber,
            @RequestParam String features,
            @RequestParam(defaultValue = "false") boolean isBeta) throws IOException {

        // 检查是否已存在相同包名和版本的应用
        if (appInfoService.existsByPackageAndVersion(packageName, version)) {
            return Result.error(409, "已存在相同包名和版本的应用");
        }

        AppInfo appInfo = new AppInfo();
        appInfo.setAppName(appName);
        appInfo.setPackageName(packageName);
        appInfo.setBuildNumber(buildNumber);
        appInfo.setVersion(version);
        appInfo.setFeatures(features);
        appInfo.setIsBeta(isBeta);

        AppInfo savedApp = appInfoService.uploadApp(file, appInfo);
        return Result.success("上传成功", savedApp);
    }

    /**
     * 获取应用列表（支持分页和筛选）
     * @param pageNum 页码，默认为1
     * @param pageSize 每页数量，默认为10，最大100
     * @param appName 应用名称（模糊查询）
     * @param version 版本号（精确匹配）
     * @param buildNumber 构建号（精确匹配）
     * @param isBeta 是否测试版
     * @return 分页应用列表
     */
    @PermitAll
    @GetMapping("/apps")
    public Result<PageResult<AppInfoDto>> getAllApps(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) String appName,
            @RequestParam(required = false) String version,
            @RequestParam(required = false) String buildNumber,
            @RequestParam(required = false) Boolean isBeta) {

        // 确保分页参数有效
        pageNum = Math.max(1, pageNum);
        pageSize = Math.min(100, Math.max(1, pageSize)); // 限制每页最多100条

        PageResult<AppInfoDto> pageResult = appInfoService.getAllApps(
            pageNum, 
            pageSize, 
            appName, 
            version, 
            buildNumber, 
            isBeta
        );
        return Result.success(pageResult);
    }

    /**
     * 根据包名获取应用列表
     */
    @PermitAll
    @GetMapping("/package/{packageName}")
    public List<AppInfo> getAppsByPackageName(@PathVariable String packageName) {
        return appInfoService.getAppsByPackageName(packageName);
    }

    /**
     * 根据ID获取应用信息
     */
    @GetMapping("app/{id}")
    @PermitAll
    public Result<AppInfo> getAppById(@PathVariable Integer id) {
        AppInfo appInfo = appInfoService.getAppById(id);
        if (appInfo == null) {
            return Result.error(404, "应用不存在");
        }
        return Result.success(appInfo);
    }

    /**
     * 下载APP文件
     */
    @PermitAll
    @GetMapping(value = "download/{id}", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    public ResponseEntity<StreamingResponseBody> downloadApp(@PathVariable Integer id, @RequestHeader(value = "Range", required = false) String rangeHeader) throws IOException {
        AppInfo appInfo = appInfoService.getAppById(id);
        if (appInfo == null) {
            return ResponseEntity.notFound().build();
        }

        // 增加下载次数
        appInfoService.incrementDownloadCount(id);

        try {
            return fileDownloadUtil.downloadFile(appInfo.getPath(), BucketType.APPS);
        } catch (Exception e) {
            throw new RuntimeException("文件下载失败: " + e.getMessage(), e);
        }
    }

    /**
     * 更新应用信息
     * @param id 应用ID
     * @param appName 应用名称
     * @param packageName 包名
     * @param version 版本号
     * @param buildNumber 构建号
     * @param features 功能描述
     * @param isBeta 是否测试版
     * @return 更新后的应用信息
     */
    @PutMapping(value = "update/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Result<AppInfo>> updateApp(
            @PathVariable Integer id,
            @RequestParam(required = false) String appName,
            @RequestParam(required = false) String packageName,
            @RequestParam(required = false) String version,
            @RequestParam(required = false) String buildNumber,
            @RequestParam(required = false) String features,
            @RequestParam(required = false) Boolean isBeta) {
        try {
            // 获取现有应用信息
            AppInfo existingApp = appInfoService.getAppById(id);
            if (existingApp == null) {
                return ResponseEntity.badRequest().body(Result.error(400, "应用不存在，ID: " + id));
            }
            
            // 更新非空字段
            if (appName != null) existingApp.setAppName(appName);
            if (packageName != null) existingApp.setPackageName(packageName);
            if (version != null) existingApp.setVersion(version);
            if (buildNumber != null) existingApp.setBuildNumber(buildNumber);
            if (features != null) existingApp.setFeatures(features);
            if (isBeta != null) existingApp.setIsBeta(isBeta);
            
            // 更新应用信息
            AppInfo updatedApp = appInfoService.updateAppInfo(existingApp);
            return ResponseEntity.ok(Result.success("更新成功", updatedApp));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Result.error(400, e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Result.error(500, "更新应用信息失败: " + e.getMessage()));
        }
    }
    
    /**
     * 删除应用
     * @param id 应用ID
     * @return 操作结果
     */
    @RequestMapping(value = "delete/{id}")
    public ResponseEntity<?> deleteApp(@PathVariable Integer id) {
        try {
            boolean deleted = appInfoService.deleteApp(id);
            if (deleted) {
                return ResponseEntity.ok(Result.success("删除成功"));
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Result.error(404, "应用不存在"));
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Result.error(500, "删除应用失败: " + e.getMessage()));
        }
    }

    /**
     * 检查应用版本更新
     * @param headers HTTP请求头，包含appinfo和deviceInfo
     * @return 版本检查结果
     */
    @PermitAll
    @PostMapping("/check-version")
    public Result<AppVersionCheckDto> checkVersionUpdate(@RequestHeader HttpHeaders headers) {
        try {
            AppVersionCheckDto result = appInfoService.checkVersionUpdate(headers);
            return Result.success(result);
        } catch (IllegalArgumentException e) {
            return Result.error(400, e.getMessage());
        } catch (Exception e) {
            return Result.error(500, "版本检查失败: " + e.getMessage());
        }
    }

    /**
     * 设置应用版本的弹窗控制
     * @param id 应用ID
     * @param showUpdatePopup 是否显示更新弹窗
     * @param forceUpdate 是否强制更新
     * @return 操作结果
     */
    @PostMapping("/app/{id}/popup-control")
    public Result<?> setAppPopupControl(
            @PathVariable Integer id,
            @RequestParam boolean showUpdatePopup,
            @RequestParam(defaultValue = "false") boolean forceUpdate) {
        try {
            AppInfo appInfo = appInfoService.getAppById(id);
            if (appInfo == null) {
                return Result.error(404, "应用不存在");
            }
            
            // 更新弹窗控制设置
            appInfo.setShowUpdatePopup(showUpdatePopup);
            appInfo.setForceUpdate(forceUpdate);
            
            AppInfo updatedApp = appInfoService.updateAppInfo(appInfo);
            return Result.success("弹窗控制设置已更新", updatedApp);
        } catch (Exception e) {
            return Result.error(500, "设置弹窗控制失败: " + e.getMessage());
        }
    }

    /**
     * 获取应用版本的弹窗控制状态
     * @param id 应用ID
     * @return 弹窗控制状态
     */
    @GetMapping("/app/{id}/popup-control")
    public Result<Map<String, Object>> getAppPopupControl(@PathVariable Integer id) {
        try {
            AppInfo appInfo = appInfoService.getAppById(id);
            if (appInfo == null) {
                return Result.error(404, "应用不存在");
            }
            
            Map<String, Object> status = Map.of(
                "id", appInfo.getId(),
                "appName", appInfo.getAppName(),
                "version", appInfo.getVersion(),
                "showUpdatePopup", appInfo.getShowUpdatePopup(),
                "forceUpdate", appInfo.getForceUpdate()
            );
            return Result.success(status);
        } catch (Exception e) {
            return Result.error(500, "获取弹窗控制状态失败: " + e.getMessage());
        }
    }

    /**
     * 获取APP更新总开关状态
     * @return 当前APP更新总开关状态
     */
    @PermitAll
    @GetMapping("/update-total/status")
    public Result<Map<String, Boolean>> getUpdateTotalStatus() {
        boolean isEnabled = configService.getBooleanConfig(CONFIG_KEY_APP_UPDATE_TOTAL_ENABLED, true);
        return Result.success(Map.of("appUpdateTotalEnabled", isEnabled));
    }

    /**
     * 设置APP更新总开关状态
     * @param enabled 是否开启APP更新
     * @return 操作结果
     */
    @PermitAll
    @PostMapping("/update-total/set-status")
    public Result<?> setUpdateTotalStatus(@RequestParam boolean enabled) {
        configService.updateConfig(CONFIG_KEY_APP_UPDATE_TOTAL_ENABLED, String.valueOf(enabled));
        return Result.success("APP更新已" + (enabled ? "开启" : "关闭"));
    }
}
