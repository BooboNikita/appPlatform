package com.app.app.service;

import com.app.app.entity.DynamicConfig;
import com.app.app.entity.DynamicConfigHistory;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface DynamicConfigService {
    /**
     * 上传并保存动态配置
     */
    DynamicConfig uploadConfig(MultipartFile file, String versionRange, String env, String remark) throws Exception;

    /**
     * 根据 ID 获取配置
     */
    DynamicConfig getConfigById(Long id);

    /**
     * 获取所有配置
     */
    List<DynamicConfig> getAllConfigs(String env);

    /**
     * 根据版本和环境获取适用的最新配置
     */
    DynamicConfig getLatestConfigByVersion(String version, String env);

    /**
     * 删除配置
     */
    void deleteConfig(Long id) throws Exception;

    /**
     * 更新配置
     */
    DynamicConfig updateConfig(Long id, MultipartFile file, String versionRange, String env, String remark) throws Exception;

    /**
     * 获取配置文件的内容
     */
    Object getConfigContent(Long id, String env) throws Exception;

    // 历史版本相关方法

    /**
     * 获取指定配置的历史版本列表
     */
    List<DynamicConfigHistory> getConfigHistory(Long configId);

    /**
     * 获取所有历史版本（支持筛选）
     */
    List<DynamicConfigHistory> getAllHistory(String env, String versionRange);

    /**
     * 回溯到指定历史版本
     */
    DynamicConfig revertToHistory(Long configId, Long historyId, String operator) throws Exception;

    /**
     * 获取历史版本详情
     */
    DynamicConfigHistory getHistoryById(Long historyId);

    /**
     * 获取历史版本的配置内容
     */
    Object getHistoryContent(Long historyId) throws Exception;
}
