package com.app.appplatform.service;

import com.app.appplatform.entity.DynamicConfig;
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
}
