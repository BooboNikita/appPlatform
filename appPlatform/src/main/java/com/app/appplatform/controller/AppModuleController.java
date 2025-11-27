package com.app.appplatform.controller;

import com.app.appplatform.common.Result;
import com.app.appplatform.dto.AppModuleDto;
import com.app.appplatform.entity.AppModule;
import com.app.appplatform.service.AppModuleService;
import jakarta.annotation.security.PermitAll;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api-modules")
public class AppModuleController {

    @Autowired
    private AppModuleService appModuleService;

    @PermitAll
    @GetMapping("/allActive")
    public Result<List<AppModuleDto>> listActiveModules(@RequestParam String userName, @RequestHeader HttpHeaders headers) {
        return Result.success(appModuleService.getActiveModules(userName, headers));
    }

    @GetMapping("/all")
    public Result<List<AppModule>> listAllModules() {
        return Result.success(appModuleService.getAllModules());
    }

    @GetMapping("/{id}")
    public Result<AppModule> getModule(@PathVariable Long id) {
        return Result.success(appModuleService.getModuleById(id));
    }

    @PostMapping("/create")
    public Result<Void> createModule(@RequestBody AppModule appModule) {
        appModuleService.createModule(appModule);
        return Result.success(null);
    }

    @PutMapping("/update/{id}")
    public Result<Void> updateModule(@PathVariable Long id, @RequestBody AppModule appModule) {
        appModule.setId(id);
        appModuleService.updateModule(appModule);
        return Result.success(null);
    }

    @DeleteMapping("/{id}")
    public Result<Void> deleteModule(@PathVariable Long id) {
        appModuleService.deleteModule(id);
        return Result.success(null);
    }

    @PermitAll
    @GetMapping("/checkHelperModuleExists")
    public Result<AppModuleDto> checkHelperModuleExists(@RequestParam String userName, @RequestHeader HttpHeaders headers) {
        return Result.success(appModuleService.checkHelperModuleExists(userName, headers));
    }
}