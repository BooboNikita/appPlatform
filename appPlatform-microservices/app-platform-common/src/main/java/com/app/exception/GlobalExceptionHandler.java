package com.app.exception;

import com.app.common.Result;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import java.io.IOException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    // 自定义文件大小限制异常处理
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public Result<Void> handleMaxSizeException(MaxUploadSizeExceededException exc) {
        return Result.error(413, "文件大小超过限制，最大允许上传500MB");
    }

    // 参数非法异常处理
    @ExceptionHandler(IllegalArgumentException.class)
    public Result<Void> handleIllegalArgumentException(IllegalArgumentException ex) {
        return Result.error(400, ex.getMessage());
    }

    // IO异常处理
    @ExceptionHandler(IOException.class)
    public Result<Void> handleIOException(IOException ex) {
        logger.error("文件处理错误", ex);
        return Result.error(500, "文件处理错误: " + ex.getMessage());
    }

    // 其他未捕获的异常处理
    @ExceptionHandler(Exception.class)
    public Result<Void> handleAllExceptions(Exception ex) {
        logger.error("系统异常", ex);
        return Result.error(500, "系统错误: " + ex.getMessage());
    }
}
