package com.app.appplatform.service.impl;

import com.app.appplatform.entity.PerformanceReview;
import com.app.appplatform.mapper.primary.PerformanceReviewMapper;
import com.app.appplatform.service.PerformanceReviewService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 绩效评估配置服务实现类
 */
@Service
public class PerformanceReviewServiceImpl implements PerformanceReviewService {

    private final PerformanceReviewMapper performanceReviewMapper;

    @Autowired
    public PerformanceReviewServiceImpl(PerformanceReviewMapper performanceReviewMapper) {
        this.performanceReviewMapper = performanceReviewMapper;
    }

    @Override
    public String getCoverImage(String deptId) {
        PerformanceReview config = performanceReviewMapper.findByDeptId(deptId);
        if (config == null) {
            return null;
        }
        return config.getCoverImage() != null ? config.getCoverImage() : "";
    }

    @Override
    public String getDeadline(String deptId) {
        PerformanceReview config = performanceReviewMapper.findByDeptId(deptId);
        if (config == null) {
            return null;
        }
        return config.getDeadline() != null ? config.getDeadline() : "";
    }

    @Override
    public String getName(String deptId) {
        PerformanceReview config = performanceReviewMapper.findByDeptId(deptId);
        if (config == null) {
            return null;
        }
        return config.getName() != null ? config.getName() : "";
    }

    @Override
    public PerformanceReview getDeptConfig(String deptId) {
        return performanceReviewMapper.findByDeptId(deptId);
    }

    @Override
    public List<PerformanceReview> getAllConfigs() {
        return performanceReviewMapper.findAll();
    }

    @Override
    public PerformanceReview setCoverImage(String deptId, String coverImage, String operator) {
        PerformanceReview config = performanceReviewMapper.findByDeptId(deptId);
        LocalDateTime now = LocalDateTime.now();
        
        if (config == null) {
            // 如果不存在，创建新配置
            config = new PerformanceReview();
            config.setDeptId(deptId);
            config.setCoverImage(coverImage);
            config.setCreateTime(now);
            config.setUpdateTime(now);
            performanceReviewMapper.insert(config);
        } else {
            // 如果存在，更新封面图
            performanceReviewMapper.updateCoverImage(deptId, coverImage, now);
            config.setCoverImage(coverImage);
            config.setUpdateTime(now);
        }
        
        return config;
    }

    @Override
    public PerformanceReview setDeadline(String deptId, String deadline, String operator) {
        PerformanceReview config = performanceReviewMapper.findByDeptId(deptId);
        LocalDateTime now = LocalDateTime.now();
        
        if (config == null) {
            // 如果不存在，创建新配置
            config = new PerformanceReview();
            config.setDeptId(deptId);
            config.setDeadline(deadline);
            config.setCreateTime(now);
            config.setUpdateTime(now);
            performanceReviewMapper.insert(config);
        } else {
            // 如果存在，更新截止时间
            performanceReviewMapper.updateDeadline(deptId, deadline, now);
            config.setDeadline(deadline);
            config.setUpdateTime(now);
        }
        
        return config;
    }

    @Override
    public PerformanceReview setName(String deptId, String name, String operator) {
        PerformanceReview config = performanceReviewMapper.findByDeptId(deptId);
        LocalDateTime now = LocalDateTime.now();
        
        if (config == null) {
            // 如果不存在，创建新配置
            config = new PerformanceReview();
            config.setDeptId(deptId);
            config.setName(name);
            config.setCreateTime(now);
            config.setUpdateTime(now);
            performanceReviewMapper.insert(config);
        } else {
            // 如果存在，更新组织名称
            performanceReviewMapper.updateName(deptId, name, now);
            config.setName(name);
            config.setUpdateTime(now);
        }
        
        return config;
    }

    @Override
    public PerformanceReview updateDeptConfig(String deptId, PerformanceReview config) {
        if (config == null) {
            return null;
        }
        
        PerformanceReview existingConfig = performanceReviewMapper.findByDeptId(deptId);
        LocalDateTime now = LocalDateTime.now();
        
        if (existingConfig == null) {
            // 如果不存在，创建新配置
            config.setDeptId(deptId);
            config.setCreateTime(now);
            config.setUpdateTime(now);
            performanceReviewMapper.insert(config);
        } else {
            // 如果存在，更新配置
            config.setDeptId(deptId);
            config.setCreateTime(existingConfig.getCreateTime());
            config.setUpdateTime(now);
            performanceReviewMapper.updateByDeptId(config);
        }
        
        return config;
    }

    @Override
    public boolean deleteDeptConfig(String deptId) {
        int result = performanceReviewMapper.deleteByDeptId(deptId);
        return result > 0;
    }

    @Override
    public Map<String, PerformanceReview> getBatchConfigs(List<String> deptIds) {
        if (deptIds == null || deptIds.isEmpty()) {
            return new HashMap<>();
        }
        
        List<PerformanceReview> configs = performanceReviewMapper.findByDeptIds(deptIds);
        return configs.stream()
                .collect(Collectors.toMap(
                        PerformanceReview::getDeptId,
                        config -> config
                ));
    }

    @Override
    public boolean existsByDeptId(String deptId) {
        return performanceReviewMapper.existsByDeptId(deptId);
    }
}
