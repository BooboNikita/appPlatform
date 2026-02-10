package com.app.appplatform.service;

import com.app.appplatform.entity.PerformanceReview;

import java.util.List;
import java.util.Map;

/**
 * 绩效评估配置服务接口
 */
public interface PerformanceReviewService {
    
    /**
     * 根据部门ID获取封面图
     */
    String getCoverImage(String deptId);
    
    /**
     * 根据部门ID获取截止时间
     */
    String getDeadline(String deptId);
    
    /**
     * 根据部门ID获取组织名称
     */
    String getName(String deptId);
    
    /**
     * 根据部门ID获取完整配置
     */
    PerformanceReview getDeptConfig(String deptId);
    
    /**
     * 获取所有部门配置
     */
    List<PerformanceReview> getAllConfigs();
    
    /**
     * 设置部门封面图
     */
    PerformanceReview setCoverImage(String deptId, String coverImage, String operator);
    
    /**
     * 设置部门截止时间
     */
    PerformanceReview setDeadline(String deptId, String deadline, String operator);
    
    /**
     * 设置组织名称
     */
    PerformanceReview setName(String deptId, String name, String operator);
    
    /**
     * 更新部门完整配置
     */
    PerformanceReview updateDeptConfig(String deptId, PerformanceReview config);
    
    /**
     * 删除部门配置
     */
    boolean deleteDeptConfig(String deptId);
    
    /**
     * 批量获取部门配置
     */
    Map<String, PerformanceReview> getBatchConfigs(List<String> deptIds);
    
    /**
     * 检查部门配置是否存在
     */
    boolean existsByDeptId(String deptId);
}
