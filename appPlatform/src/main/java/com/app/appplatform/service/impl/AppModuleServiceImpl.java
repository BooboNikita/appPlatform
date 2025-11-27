package com.app.appplatform.service.impl;

import com.app.appplatform.dto.AppModuleDto;
import com.app.appplatform.entity.AppModule;
import com.app.appplatform.mapper.AppModuleMapper;
import com.app.appplatform.service.AppModuleService;
import com.app.appplatform.util.JsonUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import com.fasterxml.jackson.core.type.TypeReference;
import java.util.stream.Collectors;

@Service
public class AppModuleServiceImpl implements AppModuleService {

    private static final Log logger = LogFactory.getLog(AppModuleServiceImpl.class);

    private final AppModuleMapper appModuleMapper;

    AppModuleServiceImpl(AppModuleMapper appModuleMapper) {
        this.appModuleMapper = appModuleMapper;
    }

    @Override
    @Cacheable(value = "appModules",
            key = "'active:' + #username + ':' + T(java.util.Objects).hashCode(#headers.getFirst('deviceInfo'))")
    public List<AppModuleDto> getActiveModules(String username, HttpHeaders headers) {
        logger.info("getActiveModules " + username + " " + headers);

        boolean hideForTest = username.equals("apptest") || username.isEmpty();

        String deviceInfo = headers.getFirst("deviceInfo");
        String deviceBrand = null;
        if (deviceInfo != null) {
            try {
                Map<String, Object> map = JsonUtil.toObject(deviceInfo, new TypeReference<Map<String, Object>>() {});
                if (map != null) {
                    deviceBrand = (String) map.get("brand");
                }
            } catch (Exception e) {
                // 解析失败，可以记录日志
                System.err.println("Failed to parse deviceInfo: " + e.getMessage());
            }
        }

        logger.info("getActiveModules brand:" + deviceBrand + "hideForTest:" + hideForTest);

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

    @Override
    public AppModuleDto checkHelperModuleExists(String username, HttpHeaders headers) {
        List<AppModuleDto> moduleList = getActiveModules(username, headers);

        AppModuleDto res =  moduleList.stream()
                .filter(module -> module.getTitle().equalsIgnoreCase("智能问答"))
                .findFirst()
                .orElse(null);
        if (res != null) {
            res.setIcon("http://172.31.101.166:8008/scfile/doors/faction/20251127/ai_module_1764205660779.png");
        }
        return res;
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