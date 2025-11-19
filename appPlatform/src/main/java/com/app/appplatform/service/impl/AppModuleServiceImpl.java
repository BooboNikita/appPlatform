package com.app.appplatform.service.impl;

import com.app.appplatform.dto.AppModuleDto;
import com.app.appplatform.entity.AppModule;
import com.app.appplatform.mapper.AppModuleMapper;
import com.app.appplatform.service.AppModuleService;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.stream.Collectors;

@Service
public class AppModuleServiceImpl implements AppModuleService {

    private final AppModuleMapper appModuleMapper;

    AppModuleServiceImpl(AppModuleMapper appModuleMapper) {
        this.appModuleMapper = appModuleMapper;
    }

    @Override
    @Cacheable(value = "appModules", key = "'active'")
    public List<AppModuleDto> getActiveModules(String username, HttpHeaders headers) {
        boolean hideForTest = username.equals("apptest");

        String deviceInfo = headers.getFirst("deviceInfo");
        String deviceBrand = null;
        if (deviceInfo != null) {
            try {
                String fixedJson = deviceInfo
                        .replaceAll("(\\w+):", "\"$1\":")
                        .replaceAll(": (\\w+)", ": \"$1\"");

                ObjectMapper objectMapper = new ObjectMapper();
                Map<String, Object> deviceInfoMap = objectMapper.readValue(fixedJson, new TypeReference<Map<String, Object>>() {});
                deviceBrand = (String) deviceInfoMap.get("brand");
            } catch (Exception e) {
                // 解析失败，可以记录日志
                System.err.println("Failed to parse deviceInfo: " + e.getMessage());
            }
        }

        if (deviceBrand != null && deviceBrand.equalsIgnoreCase("huawei")) {
            hideForTest = false;
        }

        List<AppModule> moduleList = hideForTest ? appModuleMapper.findAllActiveAndHideForTest() : appModuleMapper.findAllActive();

        return moduleList.stream()
                .map(this::convertToAppInfoDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<AppModule> getAllModules() {
        return appModuleMapper.findAll();
    }

    @Override
    public AppModule getModuleById(Long id) {
        return appModuleMapper.findById(id);
    }

    @Override
    @Transactional
    @CacheEvict(value = "appModules", allEntries = true)
    public void createModule(AppModule appModule) {
        appModuleMapper.insert(appModule);
    }

    @Override
    @Transactional
    @CacheEvict(value = "appModules", allEntries = true)
    public void updateModule(AppModule appModule) {
        appModuleMapper.update(appModule);
    }

    @Override
    @Transactional
    @CacheEvict(value = "appModules", allEntries = true)
    public void deleteModule(Long id) {
        appModuleMapper.delete(id);
    }

    private AppModuleDto convertToAppInfoDto(AppModule appModule) {
        AppModuleDto dto = new AppModuleDto();
        dto.setTitle(appModule.getTitle());
        dto.setColor(appModule.getColor());
        dto.setPort(appModule.getPort());
        dto.setRoute(appModule.getRoute());
        dto.setUrl(appModule.getTargetUrl());
        dto.setIcon(appModule.getIconUrl());
        return dto;
    }
}