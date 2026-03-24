package com.app.appplatform.service;

import org.springframework.http.ResponseEntity;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

public interface MediaService {
    /**
     * 访问媒体文件用于在线预览
     * @param objectName 对象名称
     * @return ResponseEntity包含正确的响应头用于在线预览
     * @throws Exception 访问文件异常
     */
    ResponseEntity<StreamingResponseBody> accessMediaFile(String objectName) throws Exception;
}
