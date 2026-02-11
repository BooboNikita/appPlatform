package com.app.appplatform.controller;

import com.app.appplatform.common.Result;
import com.app.appplatform.dto.StoreLinkConfigDto;
import com.app.appplatform.entity.StoreLinkConfig;
import com.app.appplatform.service.StoreLinkConfigService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 应用商店链接配置管理接口
 */
@RestController
@RequestMapping("/api/store-link-config")
public class StoreLinkConfigController {
    
    @Autowired
    private StoreLinkConfigService storeLinkConfigService;
    
    /**
     * 获取所有启用的配置
     */
    @GetMapping("/enabled")
    public Result<List<StoreLinkConfig>> getEnabledConfigs() {
        List<StoreLinkConfig> configs = storeLinkConfigService.findAllEnabled();
        return Result.success(configs);
    }
    
    /**
     * 根据设备品牌查询配置
     */
    @GetMapping("/brand/{deviceBrand}")
    public Result<StoreLinkConfig> getConfigByBrand(@PathVariable String deviceBrand) {
        StoreLinkConfig config = storeLinkConfigService.findByDeviceBrand(deviceBrand);
        return Result.success(config);
    }
    
    /**
     * 获取所有配置
     */
    @GetMapping
    public Result<List<StoreLinkConfig>> getAllConfigs() {
        List<StoreLinkConfig> configs = storeLinkConfigService.findAll();
        return Result.success(configs);
    }
    
    /**
     * 根据ID查询配置
     */
    @GetMapping("/{id}")
    public Result<StoreLinkConfig> getConfigById(@PathVariable Long id) {
        StoreLinkConfig config = storeLinkConfigService.findById(id);
        return Result.success(config);
    }
    
    /**
     * 创建配置
     */
    @PostMapping
    public Result<StoreLinkConfig> createConfig(@RequestBody StoreLinkConfigDto dto) {
        StoreLinkConfig config = storeLinkConfigService.create(dto);
        return Result.success(config);
    }
    
    /**
     * 更新配置
     */
    @PutMapping("/{id}")
    public Result<StoreLinkConfig> updateConfig(@PathVariable Long id, @RequestBody StoreLinkConfigDto dto) {
        StoreLinkConfig config = storeLinkConfigService.update(id, dto);
        return Result.success(config);
    }
    
    /**
     * 删除配置
     */
    @DeleteMapping("/{id}")
    public Result<Boolean> deleteConfig(@PathVariable Long id) {
        return Result.success(storeLinkConfigService.delete(id));
    }
    
    /**
     * 启用/禁用配置
     */
    @PutMapping("/{id}/enabled")
    public Result<Boolean> updateConfigEnabled(@PathVariable Long id, @RequestParam Integer enabled) {
        return Result.success(storeLinkConfigService.updateEnabled(id, enabled));
    }
}
