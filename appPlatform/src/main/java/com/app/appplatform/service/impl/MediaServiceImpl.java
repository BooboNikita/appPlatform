package com.app.appplatform.service.impl;

import com.app.appplatform.enums.BucketType;
import com.app.appplatform.service.MediaService;
import com.app.appplatform.service.MinioService;
import com.app.appplatform.util.FileDownloadUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

@Service
public class MediaServiceImpl implements MediaService {

    private final MinioService minioService;
    private final FileDownloadUtil fileDownloadUtil;

    @Autowired
    public MediaServiceImpl(MinioService minioService, FileDownloadUtil fileDownloadUtil) {
        this.minioService = minioService;
        this.fileDownloadUtil = fileDownloadUtil;
    }

    @Override
    public ResponseEntity<StreamingResponseBody> accessMediaFile(String objectName) throws Exception {
        // 获取文件扩展名以确定Content-Type
        String contentType = getContentType(objectName);
        
        ResponseEntity<StreamingResponseBody> response = fileDownloadUtil.downloadFile(objectName, BucketType.MEDIA);
        
        // 重新构建ResponseEntity，添加预览相关的响应头
        return ResponseEntity.ok()
                .contentType(org.springframework.http.MediaType.parseMediaType(contentType))
                .header("Content-Disposition", "inline; filename=\"" + objectName + "\"")
                .header("Cache-Control", "public, max-age=3600") // 缓存1小时
                .body(response.getBody());
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
