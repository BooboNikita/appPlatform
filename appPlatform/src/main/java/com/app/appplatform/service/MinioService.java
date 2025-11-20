package com.app.appplatform.service;

import io.minio.StatObjectResponse;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.io.InputStream;

public interface MinioService {
    /**
     * 上传文件到MinIO
     *
     * @param file       上传的文件
     * @param objectName 对象名称
     * @return 上传后的对象名称
     */
    String uploadFile(MultipartFile file, String objectName, String bucketName) throws Exception;

    /**
     * 获取文件的访问URL
     *
     * @param objectName 对象名称
     * @return 文件的访问URL
     */
    String getFileUrl(String objectName, String bucketName) throws Exception;

    /**
     * 下载文件
     *
     * @param objectName 对象名称
     * @return 文件输入流
     */
    InputStream downloadFile(String objectName, String bucketName) throws Exception;

    /**
     * 删除文件
     *
     * @param objectName 对象名称
     */
    void deleteFile(String objectName, String bucketName) throws Exception;

    /**
     * 下载文件并返回StreamingResponseBody
     * @param objectName 对象名称
     * @param bucketName 存储桶名称
     * @return StreamingResponseBody 用于流式下载
     * @throws Exception 下载异常
     */
    StreamingResponseBody downloadFileAsStream(String objectName, String bucketName) throws Exception;

    /**
     * 获取文件信息
     * @param objectName 对象名称
     * @param bucketName 存储桶名称
     * @return 文件信息
     * @throws Exception 获取文件信息异常
     */
    StatObjectResponse getFileStat(String objectName, String bucketName) throws Exception;
}