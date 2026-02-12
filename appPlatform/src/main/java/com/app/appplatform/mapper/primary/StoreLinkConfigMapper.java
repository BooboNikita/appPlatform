package com.app.appplatform.mapper.primary;

import com.app.appplatform.entity.StoreLinkConfig;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 应用商店链接配置Mapper接口
 */
@Mapper
public interface StoreLinkConfigMapper {
    
    /**
     * 查询所有启用的商店链接配置
     * @return 配置列表
     */
    List<StoreLinkConfig> findAllEnabled();
    
    /**
     * 根据设备品牌查询配置
     * @param deviceBrand 设备品牌
     * @return 配置信息
     */
    StoreLinkConfig findByDeviceBrand(@Param("deviceBrand") String deviceBrand);
    
    /**
     * 根据设备品牌或别名查询配置
     * @param deviceBrand 设备品牌
     * @return 配置信息
     */
    StoreLinkConfig findByDeviceBrandOrAlias(@Param("deviceBrand") String deviceBrand);
    
    /**
     * 查询默认配置
     * @return 默认配置
     */
    StoreLinkConfig findDefaultConfig();
    
    /**
     * 插入配置
     * @param config 配置信息
     * @return 影响行数
     */
    int insert(StoreLinkConfig config);
    
    /**
     * 更新配置
     * @param config 配置信息
     * @return 影响行数
     */
    int update(StoreLinkConfig config);
    
    /**
     * 删除配置
     * @param id 主键ID
     * @return 影响行数
     */
    int deleteById(@Param("id") Long id);
    
    /**
     * 启用/禁用配置
     * @param id 主键ID
     * @param enabled 是否启用
     * @return 影响行数
     */
    int updateEnabled(@Param("id") Long id, @Param("enabled") Integer enabled);
}
