package com.app.appplatform.controller;

import com.app.appplatform.common.PageResult;
import com.app.appplatform.common.Result;
import com.app.appplatform.entity.LogInfo;
import com.app.appplatform.entity.LogRequest;
import com.app.appplatform.enums.BucketType;
import com.app.appplatform.service.LogRequestService;
import com.app.appplatform.service.LogService;
import com.app.appplatform.service.MinioService;
import jakarta.annotation.security.PermitAll;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api-logs")
public class LogController {

    private final LogService logService;

    private final MinioService minioService;

    private final LogRequestService logRequestService;

    @Autowired
    public LogController(LogService logService, MinioService minioService, LogRequestService logRequestService) {
        this.logService = logService;
        this.minioService = minioService;
        this.logRequestService = logRequestService;
    }

    private static final Log logger = LogFactory.getLog(LogController.class);

    /**
     * 上传日志文件
     */
    @PermitAll()
    @PostMapping(value = "/upload",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Result<Void>> uploadLogs(
            @RequestPart("files") MultipartFile[] files,
            @RequestParam String username,
            @RequestParam String nickname,
            @RequestParam String appName,
            @RequestParam String version,
            @RequestParam String imageUrls,
            @RequestParam String problem,
            @RequestParam(required = false, defaultValue = "false") Boolean isActiveUpload) throws IOException {

        if (files == null || files.length == 0) {
            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(Result.error(500, "文件不能为空"));
        }

        StringBuilder filePaths = new StringBuilder();
        for (MultipartFile file : files) {
            if (file.isEmpty()) {
                continue;
            }

            try {
                String originalFilename = file.getOriginalFilename();
                String newFilename = username + "_" + appName + "_" + originalFilename;
                String filePath = minioService.uploadFile(file, newFilename, BucketType.LOGS);
                if (!filePaths.isEmpty()) {
                    filePaths.append(",");
                }
                filePaths.append(filePath);

            } catch (Exception e) {
                logger.error("上传日志文件失败", e);
            }

        }

        if (filePaths.isEmpty()) {
            return ResponseEntity.badRequest().body(Result.error(304, "没有成功上传任何文件"));
        }

        LogInfo logInfo = new LogInfo();
        logInfo.setUsername(username);
        logInfo.setNickname(nickname);
        logInfo.setAppName(appName);
        logInfo.setVersion(version);
        logInfo.setPath(filePaths.toString());
        logInfo.setUploadTime(new Date());
        logInfo.setImageUrls(imageUrls);
        logInfo.setProblem(problem);

        logService.save(logInfo);

        // 如果是主动上传，更新对应用户的logRequest记录状态为已上传
        if (Boolean.TRUE.equals(isActiveUpload)) {
            try {
                logRequestService.markAsUploadedByUsername(username);
            } catch (Exception e) {
                logger.warn("更新日志请求状态失败, username: " + username, e);
            }
        }

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(Result.success("上传成功", null));
    }

    /**
     * 获取日志列表
     */
    @GetMapping(value = "/list", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Result<PageResult<LogInfo>>> getLogList(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) String appName,
            @RequestParam(required = false) String username,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(Result.success(logService.getLogList(pageNum, pageSize, appName, username, startDate, endDate)));
    }

    /**
     * 获取日志详情
     */
    @GetMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Result<LogInfo>> getLogDetail(@PathVariable Integer id) {
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(Result.success(logService.getLogById(id)));
    }

    /**
     * 读取日志文件内容
     */
    @PostMapping(value = "/file", produces = MediaType.APPLICATION_JSON_VALUE)
    public Result<String> readLogFile(@RequestParam String filePath) throws IOException {
        try {
            try (InputStream inputStream = minioService.downloadFile(filePath, BucketType.LOGS)) {
                if (inputStream == null) {
                    return Result.error(500, "文件不存在");
                }

                // 将输入流转换为字符串
                String content = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
                return Result.success(content);
            }
        } catch (Exception e) {
            logger.error("读取日志文件失败: " + filePath, e);
            return Result.error(500, "读取日志文件失败");
        }
    }

    /**
     * 删除日志记录
     */
    @DeleteMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Result<Void>> deleteLog(@PathVariable Integer id) {
        logService.deleteLog(id);
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(Result.success(null));
    }

    /**
     * 后台创建日志请求
     * 请求App上传日志，记录到数据库并设置超时时间
     */
    @PostMapping(value = "/request", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Result<LogRequest>> createLogRequest(
            @RequestParam String username,
            @RequestParam(required = false) Integer timeoutMinutes) {
        try {
            LogRequest logRequest = logRequestService.createLogRequest(username, timeoutMinutes);
            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(Result.success("日志请求创建成功", logRequest));
        } catch (Exception e) {
            logger.error("创建日志请求失败", e);
            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(Result.error(500, "创建日志请求失败: " + e.getMessage()));
        }
    }

    /**
     * App查询日志请求
     * 检查是否有需要上传的日志请求，如有则说明App需要主动上传日志
     * 多次请求也只上传一次，返回{request: bool}格式
     */
    @PermitAll()
    @GetMapping(value = "/request/check", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Result<Map<String, Boolean>>> checkLogRequest(
            @RequestParam String username) {
        try {
            // 查询所有待上传的日志请求
            List<LogRequest> pendingRequests = logRequestService.getPendingLogRequest(username);
            boolean hasPendingRequest = !pendingRequests.isEmpty();
            Map<String, Boolean> result = new HashMap<>();
            result.put("request", hasPendingRequest);
            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(Result.success(result));
        } catch (Exception e) {
            logger.error("查询日志请求失败", e);
            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(Result.error(500, "查询日志请求失败: " + e.getMessage()));
        }
    }

    /**
     * 管理平台查询已发送的日志请求
     * 支持根据请求日期、status过滤
     */
    @GetMapping(value = "/request/list", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Result<List<LogRequest>>> getLogRequestList(
            @RequestParam(required = false) String username,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {
        try {
            List<LogRequest> list = logRequestService.getLogRequestList(username, status, startDate, endDate);
            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(Result.success(list));
        } catch (Exception e) {
            logger.error("查询日志请求列表失败", e);
            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(Result.error(500, "查询日志请求列表失败: " + e.getMessage()));
        }
    }

    /**
     * 删除单个日志请求
     */
    @DeleteMapping(value = "/request/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Result<Void>> deleteLogRequest(@PathVariable Integer id) {
        try {
            logRequestService.deleteLogRequest(id);
            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(Result.success("删除成功", null));
        } catch (Exception e) {
            logger.error("删除日志请求失败", e);
            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(Result.error(500, "删除日志请求失败: " + e.getMessage()));
        }
    }
}
