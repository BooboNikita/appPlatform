package com.app.appplatform.service;

import com.app.appplatform.dto.AppModuleDto;
import com.app.appplatform.entity.AppModule;
import org.springframework.http.HttpHeaders;

import java.util.List;

public interface AppModuleService {
    List<AppModuleDto> getActiveModules(String username, HttpHeaders headers);

    List<AppModule> getAllModules();

    AppModule getModuleById(Long id);

    void createModule(AppModule appModule);

    void updateModule(AppModule appModule);

    void deleteModule(Long id);

    AppModuleDto checkHelperModuleExists(String username, HttpHeaders headers);
}