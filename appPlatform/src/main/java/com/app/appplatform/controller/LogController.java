package com.app.appplatform.controller;

import com.app.appplatform.common.PageResult;
import com.app.appplatform.common.Result;
import com.app.appplatform.entity.LogInfo;
import com.app.appplatform.enums.BucketType;
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

@RestController
@RequestMapping("/api-logs")
public class LogController {

    private final LogService logService;

    private final MinioService minioService;

    @Autowired
    public LogController(LogService logService, MinioService minioService) {
        this.logService = logService;
        this.minioService = minioService;
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
            @RequestParam String problem) throws IOException {

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
    public ResponseEntity<Result<Void>> deleteLog(@PathVariable("id") Integer id) {
        logService.deleteLog(id);
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(Result.success(null));
    }
}
