package com.app.file.util;

import com.app.file.enums.BucketType;
import com.app.file.service.MinioService;
import io.minio.StatObjectResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

@Component
public record FileDownloadUtil(MinioService minioService) {

    public ResponseEntity<StreamingResponseBody> downloadFile(String objectName, BucketType bucketType) throws Exception {
        return downloadFile(objectName, objectName, bucketType);
    }

    public ResponseEntity<StreamingResponseBody> downloadFile(String objectName, String downloadFilename, BucketType bucketType) throws Exception {
        // 获取文件信息
        StatObjectResponse stat = minioService.getFileStat(objectName, bucketType);

        // 设置响应头
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        headers.setContentLength(stat.size());
        headers.setContentDispositionFormData("attachment", downloadFilename);

        // 获取流式响应体
        StreamingResponseBody responseBody = minioService.downloadFileAsStream(objectName, bucketType);

        return ResponseEntity.ok()
                .headers(headers)
                .body(responseBody);
    }
}
