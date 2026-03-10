package com.app.appplatform.service;

import com.app.appplatform.entity.LogRequest;

import java.util.List;

/**
 * 日志请求记录服务接口
 */
public interface LogRequestService {

    /**
     * 创建日志请求
     * @param username 用户名
     * @param timeoutMinutes 超时时间（分钟），默认120分钟（2小时）
     * @return 创建的日志请求记录
     */
    LogRequest createLogRequest(String username, Integer timeoutMinutes);

    /**
     * 根据用户名查询待上传的日志请求（只取最新的一条，多次请求也只上传一次）
     * @param username 用户名
     * @return 最新的待上传日志请求，如果没有则返回null
     */
    LogRequest getPendingLogRequest(String username);

    /**
     * 根据条件查询日志请求列表（管理平台使用）
     * @param username 用户名（可选）
     * @param status 状态（可选）：0-待上传 1-已上传 2-已过期
     * @param startDate 请求开始日期（可选）
     * @param endDate 请求结束日期（可选）
     * @return 日志请求列表
     */
    List<LogRequest> getLogRequestList(String username, Integer status, String startDate, String endDate);

    /**
     * 根据用户名标记待上传的日志请求为已上传
     * @param username 用户名
     */
    void markAsUploadedByUsername(String username);

    /**
     * 删除日志请求记录
     * @param id 记录ID
     */
    void deleteLogRequest(Integer id);
}
