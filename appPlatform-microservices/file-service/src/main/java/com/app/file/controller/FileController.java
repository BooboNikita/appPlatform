package com.app.file.controller;

import com.app.common.Result;
import com.app.file.enums.BucketType;
import com.app.file.service.MediaService;
import com.app.file.service.MinioService;
import com.app.file.util.FileDownloadUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.util.UUID;

@RestController
@RequestMapping("/api-files")
public class FileController {

    private final MinioService minioService;
    private final FileDownloadUtil fileDownloadUtil;
    private final MediaService mediaService;

    @Autowired
    public FileController(MinioService minioService, FileDownloadUtil fileDownloadUtil, MediaService mediaService) {
        this.minioService = minioService;
        this.fileDownloadUtil = fileDownloadUtil;
        this.mediaService = mediaService;
    }

    @PostMapping("/upload/logs")
    public Result<String> uploadImage(@RequestParam("file") MultipartFile file) {
        return uploadFile(file, BucketType.LOGS);
    }

    @PostMapping("/upload/apps")
    public Result<String> uploadApp(@RequestParam("file") MultipartFile file) {
        return uploadFile(file, BucketType.APPS);
    }

    @PostMapping("/upload/media")
    public Result<String> uploadMedia(@RequestParam("file") MultipartFile file) {
        return uploadMediaFile(file);
    }

    @GetMapping("/download/logs/{objectName}")
    public ResponseEntity<StreamingResponseBody> downloadLog(@PathVariable String objectName) {
        return downloadFile(objectName, BucketType.LOGS);
    }

    @GetMapping("/download/apps/{objectName}")
    public ResponseEntity<StreamingResponseBody> downloadApp(@PathVariable String objectName) {
        return downloadFile(objectName, BucketType.APPS);
    }

    @GetMapping("/media/{objectName}")
    public ResponseEntity<StreamingResponseBody> accessMedia(@PathVariable String objectName) {
        try {
            return mediaService.accessMediaFile(objectName);
        } catch (Exception e) {
            throw new RuntimeException("媒体文件访问失败: " + e.getMessage());
        }
    }

    @DeleteMapping("/delete/logs/{objectName}")
    public void deleteLog(@PathVariable String objectName) {
        deleteFile(objectName, BucketType.LOGS);
    }

    @DeleteMapping("/delete/apps/{objectName}")
    public void deleteApp(@PathVariable String objectName) {
        deleteFile(objectName, BucketType.APPS);
    }

    private Result<String> uploadFile(MultipartFile file, BucketType bucketType) {
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

            minioService.uploadFile(file, objectName, bucketType);
            return Result.success(minioService.getFileUrl(objectName, bucketType));
        } catch (Exception e) {
            throw new RuntimeException("文件上传失败: " + e.getMessage());
        }
    }

    private Result<String> uploadMediaFile(MultipartFile file) {
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

            minioService.uploadFile(file, objectName, BucketType.MEDIA);
            // 返回文件名，方便后续访问
            return Result.success(objectName);
        } catch (Exception e) {
            throw new RuntimeException("媒体文件上传失败: " + e.getMessage());
        }
    }

    private ResponseEntity<StreamingResponseBody> downloadFile(String objectName, BucketType bucketType) {
        try {
            return fileDownloadUtil.downloadFile(objectName, bucketType);
        } catch (Exception e) {
            throw new RuntimeException("文件下载失败: " + e.getMessage());
        }
    }

    private void deleteFile(String objectName, BucketType bucketType) {
        try {
            minioService.deleteFile(objectName, bucketType);
        } catch (Exception e) {
            throw new RuntimeException("文件删除失败: " + e.getMessage());
        }
    }
}
