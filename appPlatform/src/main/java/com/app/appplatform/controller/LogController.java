package com.app.appplatform.controller;

import com.app.appplatform.common.PageResult;
import com.app.appplatform.common.Result;
import com.app.appplatform.entity.LogInfo;
import com.app.appplatform.service.LogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Date;

@RestController
@RequestMapping("/api-logs")
public class LogController {

    @Value("${file.upload-dir:./uploads/logs}")
    private String uploadDir;

    private final LogService logService;

    @Autowired
    public LogController(LogService logService) {
        this.logService = logService;
    }

    /**
     * 上传日志文件
     */
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

        // 确保上传目录存在
        File dir = new File(uploadDir);
        if (!dir.exists()) {
            dir.mkdirs();
        }

        // 保存文件并收集文件路径
        StringBuilder filePaths = new StringBuilder();
        for (MultipartFile file : files) {
            String originalFilename = file.getOriginalFilename();
            String newFilename = username + "_" + appName + "_" + originalFilename;
            Path filePath = Paths.get(uploadDir, newFilename);
            file.transferTo(filePath);

            if (!filePaths.isEmpty()) {
                filePaths.append(",");
            }
            filePaths.append(filePath.toString());
        }

        // 保存日志信息到数据库
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
        Path path = Paths.get(filePath);
        if (!Files.exists(path) || !Files.isReadable(path)) {
            return Result.error(403, "文件不存在或无法读取");
        }
        return Result.success(Files.readString(path));
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
