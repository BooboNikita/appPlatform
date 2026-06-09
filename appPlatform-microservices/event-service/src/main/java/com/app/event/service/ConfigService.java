package com.app.event.service;

public interface ConfigService {
    /**
     * 获取布尔类型的配置值
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
