package com.app.appplatform.controller;

import com.app.appplatform.common.Result;
import com.app.appplatform.service.MinioService;
import com.app.appplatform.util.FileDownloadUtil;
import jakarta.annotation.security.PermitAll;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.util.UUID;

@RestController
@RequestMapping("/api-files")
public class FileController {

    @Autowired
    private MinioService minioService;

    @Autowired
    private FileDownloadUtil fileDownloadUtil;

    @Value("${minio.bucket.logs}")
    private String logsBucketName;

    @Value("${minio.bucket.apps}")
    private String appsBucketName;

    @PostMapping("/upload/logs")
    public Result<String> uploadImage(@RequestParam("file") MultipartFile file) {
        return uploadFile(file, logsBucketName);
    }

    @PermitAll
    @PostMapping("/upload/apps")
    public Result<String> uploadApp(@RequestParam("file") MultipartFile file) {
        return uploadFile(file, appsBucketName);
    }

    @PermitAll
    @GetMapping("/download/logs/{objectName}")
    public ResponseEntity<StreamingResponseBody> downloadLog(@PathVariable String objectName) {
        return downloadFile(objectName, logsBucketName);
    }

    @PermitAll
    @GetMapping("/download/apps/{objectName}")
    public ResponseEntity<StreamingResponseBody> downloadApp(@PathVariable String objectName) {
        return downloadFile(objectName, appsBucketName);
    }

    @DeleteMapping("/delete/logs/{objectName}")
    public void deleteLog(@PathVariable String objectName) {
        deleteFile(objectName, logsBucketName);
    }

    @DeleteMapping("/delete/apps/{objectName}")
    public void deleteApp(@PathVariable String objectName) {
        deleteFile(objectName, appsBucketName);
    }

    private Result<String> uploadFile(MultipartFile file, String bucketName) {
        try {
            if (file == null || file.isEmpty()) {
                throw new RuntimeException("上传文件不能为空");
            }

            // 获取原始文件名
            String originalFilename = file.getOriginalFilename();
            if (originalFilename == null || originalFilename.isEmpty()) {
                throw new RuntimeException("无效的文件名");
            }

            // 生成带时间戳的唯一文件名
            String fileExtension = "";
            int dotIndex = originalFilename.lastIndexOf('.');
            if (dotIndex > 0) {  // 确保有点且不在文件名开头
                fileExtension = originalFilename.substring(dotIndex);
            }

            String objectName = UUID.randomUUID().toString() + fileExtension;

            minioService.uploadFile(file, objectName, bucketName);
            return Result.success(minioService.getFileUrl(objectName, bucketName));
        } catch (Exception e) {
            throw new RuntimeException("文件上传失败: " + e.getMessage());
        }
    }

    private ResponseEntity<StreamingResponseBody> downloadFile(String objectName, String bucketName) {
        try {
            return fileDownloadUtil.downloadFile(objectName, bucketName);
        } catch (Exception e) {
            throw new RuntimeException("文件下载失败: " + e.getMessage());
        }
    }

    private void deleteFile(String objectName, String bucketName) {
        try {
            minioService.deleteFile(objectName, bucketName);
        } catch (Exception e) {
            throw new RuntimeException("文件删除失败: " + e.getMessage());
        }
    }
}
