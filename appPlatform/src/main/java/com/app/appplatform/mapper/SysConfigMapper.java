package com.app.appplatform.mapper;

import com.app.appplatform.entity.SysConfig;
import org.apache.ibatis.annotations.*;

import java.util.List;

/**
 * 系统配置 Mapper 接口
 */
@Mapper
public interface SysConfigMapper {
    
    /**
     * 根据ID查询配置
     */
    @Select("SELECT * FROM sys_config WHERE id = #{id}")
    SysConfig selectById(@Param("id") Long id);
    
    /**
     * 查询所有配置
     */
    @Select("SELECT * FROM sys_config")
    List<SysConfig> selectAll();
    
    /**
     * 根据键名查询配置值
     * @param key 配置键
     * @return 配置值
     */
    @Select("SELECT config_value FROM sys_config WHERE config_key = #{key}")
    String getByKey(@Param("key") String key);
    
    /**
     * 根据键名更新配置值
     * @param key 配置键
     * @param value 配置值
     * @return 更新记录数
     */
    @Update("UPDATE sys_config SET config_value = #{value} WHERE config_key = #{key}")
    int updateByKey(@Param("key") String key, @Param("value") String value);
    
    /**
     * 插入配置
     */
    @Insert("INSERT INTO sys_config(config_key, config_value, remark) " +
            "VALUES(#{configKey}, #{configValue}, #{remark})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(SysConfig sysConfig);
    
    /**
     * 更新配置
     */
    @Update("<script>" +
            "UPDATE sys_config " +
            "<set>" +
            "<if test='configKey != null'>config_key = #{configKey},</if>" +
            "<if test='configValue != null'>config_value = #{configValue},</if>" +
            "<if test='remark != null'>remark = #{remark},</if>" +
            "</set>" +
            "WHERE id = #{id}" +
            "</script>")
    int update(SysConfig sysConfig);
    
    /**
     * 根据ID删除配置
     */
    @Delete("DELETE FROM sys_config WHERE id = #{id}")
    int deleteById(@Param("id") Long id);
}
