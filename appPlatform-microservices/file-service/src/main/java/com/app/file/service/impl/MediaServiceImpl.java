package com.app.file.service.impl;

import com.app.file.enums.BucketType;
import com.app.file.service.MediaService;
import com.app.file.service.MinioService;
import io.minio.StatObjectResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

@Service
public class MediaServiceImpl implements MediaService {

    private final MinioService minioService;

    @Autowired
    public MediaServiceImpl(MinioService minioService) {
        this.minioService = minioService;
    }

    @Override
    public ResponseEntity<StreamingResponseBody> accessMediaFile(String objectName) throws Exception {
        // 获取文件扩展名以确定Content-Type
        String contentType = getContentType(objectName);

        // 获取文件信息
        StatObjectResponse stat = minioService.getFileStat(objectName, BucketType.MEDIA);

        // 设置正确的响应头用于在线预览
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Type", contentType);
        headers.add("Content-Disposition", "inline; filename=\"" + objectName + "\"");
        headers.add("Cache-Control", "public, max-age=3600");
        headers.add("Content-Length", String.valueOf(stat.size()));

        // 添加视频流支持的相关头
        if (contentType.startsWith("video/")) {
            headers.add("Accept-Ranges", "bytes");
            headers.add("Connection", "keep-alive");
        }

        // 获取流式响应体
        StreamingResponseBody responseBody = minioService.downloadFileAsStream(objectName, BucketType.MEDIA);

        return ResponseEntity.ok()
                .headers(headers)
                .body(responseBody);
    }

    private String getContentType(String objectName) {
        if (objectName == null) {
            return "application/octet-stream";
        }

        String extension = "";
        int dotIndex = objectName.lastIndexOf('.');
        if (dotIndex > 0) {
            extension = objectName.substring(dotIndex + 1).toLowerCase();
        }

        switch (extension) {
            // 图片类型
            case "jpg":
            case "jpeg":
                return "image/jpeg";
            case "png":
                return "image/png";
            case "gif":
                return "image/gif";
            case "webp":
                return "image/webp";
            case "svg":
                return "image/svg+xml";
            case "bmp":
                return "image/bmp";
            case "ico":
                return "image/x-icon";

            // 视频类型
            case "mp4":
                return "video/mp4";
            case "webm":
                return "video/webm";
            case "ogv":
                return "video/ogg";
            case "avi":
                return "video/x-msvideo";
            case "mov":
                return "video/quicktime";
            case "wmv":
                return "video/x-ms-wmv";
            case "flv":
                return "video/x-flv";
            case "mkv":
                return "video/x-matroska";

            // 音频类型
            case "mp3":
                return "audio/mpeg";
            case "wav":
                return "audio/wav";
            case "ogg":
                return "audio/ogg";
            case "oga":
                return "audio/ogg";
            case "aac":
                return "audio/aac";
            case "flac":
                return "audio/flac";

            default:
                return "application/octet-stream";
        }
    }
}
