package com.app.event.service.impl;

import com.app.event.service.ConfigService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
public class ConfigServiceImpl implements ConfigService {

    private final Map<String, String> configStore = new ConcurrentHashMap<>();

    @Override
    public boolean getBooleanConfig(String key, boolean defaultValue) {
        String value = configStore.get(key);
        if (value == null) {
            return defaultValue;
        }
        return Boolean.parseBoolean(value);
    }

    @Override
    public void updateConfig(String key, String value) {
        configStore.put(key, value);
        log.info("Config updated: {} = {}", key, value);
    }
}
