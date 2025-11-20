package com.app.appplatform.util;

import com.app.appplatform.service.MinioService;
import io.minio.StatObjectResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

@Component
public record FileDownloadUtil(MinioService minioService) {

    public ResponseEntity<StreamingResponseBody> downloadFile(String objectName, String bucketName) throws Exception {
        return downloadFile(objectName, objectName, bucketName);
    }

    public ResponseEntity<StreamingResponseBody> downloadFile(String objectName, String downloadFilename, String bucketName) throws Exception {
        // 获取文件信息
        StatObjectResponse stat = minioService.getFileStat(objectName, bucketName);

        // 设置响应头
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        headers.setContentLength(stat.size());
        headers.setContentDispositionFormData("attachment", downloadFilename);

        // 获取流式响应体
        StreamingResponseBody responseBody = minioService.downloadFileAsStream(objectName, bucketName);

        return ResponseEntity.ok()
                .headers(headers)
                .body(responseBody);
    }
}