package com.app.appplatform.service;

/**
 * 配置服务接口
 */
public interface ConfigService {
    
    /**
     * 获取配置值
     * @param key 配置键
     * @param defaultValue 默认值
     * @return 配置值
     */
    String getConfig(String key, String defaultValue);
    
    /**
     * 获取布尔类型配置值
     * @param key 配置键
     * @param defaultValue 默认值
     * @return 配置值
     */
    boolean getBooleanConfig(String key, boolean defaultValue);
    
    /**
     * 更新配置值
     * @param key 配置键
     * @param value 配置值
     */
    void updateConfig(String key, String value);
}
