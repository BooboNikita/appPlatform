package com.app.appplatform.service.impl;

import com.app.appplatform.entity.SysConfig;
import com.app.appplatform.mapper.primary.SysConfigMapper;
import com.app.appplatform.service.ConfigService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 配置服务实现类
 */
@Service
public class ConfigServiceImpl implements ConfigService {

    @Autowired
    private SysConfigMapper sysConfigMapper;

    @Override
    public String getConfig(String key, String defaultValue) {
        String value = sysConfigMapper.getByKey(key);
        return value != null ? value : defaultValue;
    }

    @Override
    public boolean getBooleanConfig(String key, boolean defaultValue) {
        String value = getConfig(key, String.valueOf(defaultValue));
        return Boolean.parseBoolean(value);
    }

    @Override
    @Transactional
    public void updateConfig(String key, String value) {
        // 先尝试更新
        int updated = sysConfigMapper.updateByKey(key, value);
        
        // 如果更新记录数为0，说明记录不存在，执行插入
        if (updated == 0) {
            SysConfig config = new SysConfig();
            config.setConfigKey(key);
            config.setConfigValue(value);
            config.setRemark("系统自动创建");
            sysConfigMapper.insert(config);
        }
    }
}
