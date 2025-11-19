package com.app.appplatform.service.impl;

import com.app.appplatform.dto.AppModuleDto;
import com.app.appplatform.entity.AppModule;
import com.app.appplatform.mapper.AppModuleMapper;
import com.app.appplatform.service.AppModuleService;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class AppModuleServiceImpl implements AppModuleService {

    private final AppModuleMapper appModuleMapper;

    AppModuleServiceImpl(AppModuleMapper appModuleMapper) {
        this.appModuleMapper = appModuleMapper;
    }

    @Override
    @Cacheable(value = "appModules", key = "'active'")
    public List<AppModuleDto> getActiveModules(String username) {
        List<AppModule> moduleList = username.equals("apptest") ? appModuleMapper.findAllActiveAndHideForTest() : appModuleMapper.findAllActive();
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