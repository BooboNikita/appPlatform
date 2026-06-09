package com.app.app.controller;

import com.app.app.dto.AppModuleDto;
import com.app.app.entity.AppModule;
import com.app.app.service.AppModuleService;
import com.app.common.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "应用模块管理", description = "管理应用模块的CRUD操作")
@RestController
@RequestMapping("/api-modules")
public class AppModuleController {

    @Autowired
    private AppModuleService appModuleService;

    @Operation(summary = "获取所有启用的模块")
    @GetMapping("/allActive")
    public Result<List<AppModuleDto>> listActiveModules(@RequestParam String userName, @RequestHeader HttpHeaders headers) {
        return Result.success(appModuleService.getActiveModules(userName, headers));
    }

    @Operation(summary = "获取所有模块")
    @GetMapping("/all")
    public Result<List<AppModule>> listAllModules() {
        return Result.success(appModuleService.getAllModules());
    }

    @Operation(summary = "根据ID获取模块")
    @GetMapping("/{id}")
    public Result<AppModule> getModule(@PathVariable Long id) {
        return Result.success(appModuleService.getModuleById(id));
    }

    @Operation(summary = "创建模块")
    @PostMapping("/create")
    public Result<Void> createModule(@RequestBody AppModule appModule) {
        appModuleService.createModule(appModule);
        return Result.success(null);
    }

    @Operation(summary = "更新模块")
    @PutMapping("/update/{id}")
    public Result<Void> updateModule(@PathVariable Long id, @RequestBody AppModule appModule) {
        appModule.setId(id);
        appModuleService.updateModule(appModule);
        return Result.success(null);
    }

    @Operation(summary = "删除模块")
    @DeleteMapping("/{id}")
    public Result<Void> deleteModule(@PathVariable Long id) {
        appModuleService.deleteModule(id);
        return Result.success(null);
    }

    @Operation(summary = "检查Helper模块是否存在")
    @GetMapping("/checkHelperModuleExists")
    public Result<AppModuleDto> checkHelperModuleExists(@RequestParam String userName, @RequestHeader HttpHeaders headers) {
        return Result.success(appModuleService.checkHelperModuleExists(userName, headers));
    }
}
