package com.app.app.service.impl;

import com.app.app.dto.AppModuleDto;
import com.app.app.entity.AppModule;
import com.app.app.mapper.AppModuleMapper;
import com.app.app.service.AppModuleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class AppModuleServiceImpl implements AppModuleService {

    private final AppModuleMapper appModuleMapper;

    @Autowired
    public AppModuleServiceImpl(AppModuleMapper appModuleMapper) {
        this.appModuleMapper = appModuleMapper;
    }

    @Override
    public List<AppModuleDto> getActiveModules(String username, HttpHeaders headers) {
        List<AppModule> modules = appModuleMapper.selectActiveModules();
        return modules.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<AppModule> getAllModules() {
        return appModuleMapper.selectAll();
    }

    @Override
    public AppModule getModuleById(Long id) {
        return appModuleMapper.selectById(id);
    }

    @Override
    public void createModule(AppModule appModule) {
        appModule.setCreatedAt(LocalDateTime.now());
        appModule.setUpdatedAt(LocalDateTime.now());
        appModuleMapper.insert(appModule);
    }

    @Override
    public void updateModule(AppModule appModule) {
        appModule.setUpdatedAt(LocalDateTime.now());
        appModuleMapper.update(appModule);
    }

    @Override
    public void deleteModule(Long id) {
        appModuleMapper.deleteById(id);
    }

    @Override
    public AppModuleDto checkHelperModuleExists(String username, HttpHeaders headers) {
        // TODO: 实现Helper模块检查逻辑
        return null;
    }

    private AppModuleDto convertToDto(AppModule module) {
        AppModuleDto dto = new AppModuleDto();
        dto.setTitle(module.getTitle());
        dto.setIcon(module.getIconUrl());
        dto.setUrl(module.getTargetUrl());
        dto.setRoute(module.getRoute());
        dto.setColor(module.getColor());
        dto.setPort(module.getPort());
        return dto;
    }
}
