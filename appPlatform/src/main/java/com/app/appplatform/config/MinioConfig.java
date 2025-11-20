package com.app.appplatform.config;

import io.minio.MinioClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
public class MinioConfig {

    @Value("${minio.endpoint}")
    private String endpoint;

    @Value("${minio.access-key}")
    private String accessKey;

    @Value("${minio.secret-key}")
    private String secretKey;

    @Value("${minio.bucket.default}")
    private String defaultBucketName;

    @Bean
    public MinioClient minioClient() {
        return MinioClient.builder()
                .endpoint(endpoint)
                .credentials(accessKey, secretKey)
                .build();
    }

    // 默认存储桶
    @Bean
    @Primary
    public String bucketName() {
        return defaultBucketName;
    }

    // 日志存储桶
    @Bean(name = "imageBucketName")
    @Value("${minio.bucket.logs}")
    public String imageBucketName() {
        return "applogs";
    }

    // app文件存储桶
    @Bean(name = "documentBucketName")
    @Value("${minio.bucket.apps}")
    public String documentBucketName() {
        return "apps";
    }
}
