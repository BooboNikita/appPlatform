package com.app.log.entity;

import lombok.Data;
import java.util.Date;

/**
 * 日志请求记录实体类
 * 用于存储后台向App发起的日志请求
 */
@Data
public class LogRequest {
    private Integer id;
    private String username;
    private Date requestTime;
    private Date expireTime;
    private Integer status;  // 0-待上传 1-已上传 2-已过期
}
