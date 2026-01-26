package com.app.appplatform.mapper;

import com.app.appplatform.entity.DynamicConfig;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface DynamicConfigMapper {
    
    @Insert("INSERT INTO dynamic_config(version_range, file_url, env, remark, create_time, update_time) " +
            "VALUES(#{versionRange}, #{fileUrl}, #{env}, #{remark}, NOW(), NOW())")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(DynamicConfig dynamicConfig);

    @Select("SELECT id, version_range as versionRange, file_url as fileUrl, env, remark, " +
            "create_time as createTime, update_time as updateTime FROM dynamic_config WHERE id = #{id}")
    DynamicConfig findById(Long id);

    @Select("<script>" +
            "SELECT id, version_range as versionRange, file_url as fileUrl, env, remark, " +
            "create_time as createTime, update_time as updateTime FROM dynamic_config " +
            "WHERE id = #{id} " +
            "<if test='env != null'>AND env = #{env}</if>" +
            "</script>")
    DynamicConfig findByIdAndEnv(@Param("id") Long id, @Param("env") String env);

    @Select("<script>" +
            "SELECT id, version_range as versionRange, file_url as fileUrl, env, remark, " +
            "create_time as createTime, update_time as updateTime FROM dynamic_config " +
            "<where>" +
            "  <if test='env != null'>AND env = #{env}</if>" +
            "</where>" +
            "ORDER BY create_time DESC" +
            "</script>")
    List<DynamicConfig> findAll(@Param("env") String env);

    @Update("<script>" +
            "UPDATE dynamic_config " +
            "<set>" +
            "  <if test='versionRange != null'>version_range = #{versionRange},</if>" +
            "  <if test='fileUrl != null'>file_url = #{fileUrl},</if>" +
            "  <if test='env != null'>env = #{env},</if>" +
            "  <if test='remark != null'>remark = #{remark},</if>" +
            "  update_time = NOW()" +
            "</set>" +
            "WHERE id = #{id}" +
            "</script>")
    int update(DynamicConfig dynamicConfig);

    @Delete("DELETE FROM dynamic_config WHERE id = #{id}")
    int deleteById(Long id);

    @Select("<script>" +
            "SELECT id, version_range as versionRange, file_url as fileUrl, env, remark, " +
            "create_time as createTime, update_time as updateTime FROM dynamic_config " +
            "<where>" +
            "  <if test='env != null'>AND env = #{env}</if>" +
            "</where>" +
            "ORDER BY update_time DESC" +
            "</script>")
    List<DynamicConfig> findByEnv(@Param("env") String env);
}
