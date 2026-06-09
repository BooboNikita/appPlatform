package com.app.store.service;

import com.app.store.dto.StoreLinkConfigDto;
import com.app.store.entity.StoreLinkConfig;

import java.util.List;

/**
 * 应用商店链接配置服务接口
 */
public interface StoreLinkConfigService {

    /**
     * 获取所有启用的配置
     * @return 配置列表
     */
    List<StoreLinkConfig> findAllEnabled();

    /**
     * 根据设备品牌查询配置
     * @param deviceBrand 设备品牌
     * @return 配置信息
     */
    StoreLinkConfig findByDeviceBrand(String deviceBrand);

    /**
     * 获取所有配置
     * @return 配置列表
     */
    List<StoreLinkConfig> findAll();

    /**
     * 根据ID查询配置
     * @param id 主键ID
     * @return 配置信息
     */
    StoreLinkConfig findById(Long id);

    /**
     * 创建配置
     * @param dto 配置DTO
     * @return 创建的配置
     */
    StoreLinkConfig create(StoreLinkConfigDto dto);

    /**
     * 更新配置
     * @param id 主键ID
     * @param dto 配置DTO
     * @return 更新的配置
     */
    StoreLinkConfig update(Long id, StoreLinkConfigDto dto);

    /**
     * 删除配置
     * @param id 主键ID
     */
    boolean delete(Long id);

    /**
     * 启用/禁用配置
     * @param id 主键ID
     * @param enabled 是否启用
     */
    boolean updateEnabled(Long id, Integer enabled);
}
