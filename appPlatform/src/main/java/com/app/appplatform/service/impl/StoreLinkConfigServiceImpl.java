package com.app.appplatform.service.impl;

import com.app.appplatform.dto.StoreLinkConfigDto;
import com.app.appplatform.entity.StoreLinkConfig;
import com.app.appplatform.mapper.primary.StoreLinkConfigMapper;
import com.app.appplatform.service.StoreLinkConfigService;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

/**
 * 应用商店链接配置服务实现类
 */
@Service
public class StoreLinkConfigServiceImpl implements StoreLinkConfigService {
    
    private final StoreLinkConfigMapper storeLinkConfigMapper;
    
    public StoreLinkConfigServiceImpl(StoreLinkConfigMapper storeLinkConfigMapper) {
        this.storeLinkConfigMapper = storeLinkConfigMapper;
    }
    
    @Override
    public List<StoreLinkConfig> findAllEnabled() {
        return storeLinkConfigMapper.findAllEnabled();
    }
    
    @Override
    public StoreLinkConfig findByDeviceBrand(String deviceBrand) {
        return storeLinkConfigMapper.findByDeviceBrand(deviceBrand);
    }
    
    @Override
    public List<StoreLinkConfig> findAll() {
        return storeLinkConfigMapper.findAllEnabled();
    }
    
    @Override
    public StoreLinkConfig findById(Long id) {
        // 这里需要在Mapper中添加方法
        return null; // 暂时返回null，需要完善
    }
    
    @Override
    public StoreLinkConfig create(StoreLinkConfigDto dto) {
        StoreLinkConfig config = new StoreLinkConfig();
        BeanUtils.copyProperties(dto, config);
        config.setCreateTime(new Date());
        config.setUpdateTime(new Date());
        
        storeLinkConfigMapper.insert(config);
        return config;
    }
    
    @Override
    public StoreLinkConfig update(Long id, StoreLinkConfigDto dto) {
        StoreLinkConfig config = new StoreLinkConfig();
        BeanUtils.copyProperties(dto, config);
        config.setId(id);
        config.setUpdateTime(new Date());
        
        storeLinkConfigMapper.update(config);
        return config;
    }
    
    @Override
    public boolean delete(Long id) {
        return storeLinkConfigMapper.deleteById(id) > 0;
    }
    
    @Override
    public boolean updateEnabled(Long id, Integer enabled) {
        return storeLinkConfigMapper.updateEnabled(id, enabled) > 0;
    }
}
