package com.app.appplatform.mapper.primary;

import com.app.appplatform.entity.DynamicConfig;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface DynamicConfigMapper {

    String COLS = "id, version_range as versionRange, env, remark, create_time as createTime, update_time as updateTime";

    @Insert("INSERT INTO dynamic_config(version_range, env, remark, create_time, update_time) " +
            "VALUES(#{versionRange}, #{env}, #{remark}, NOW(), NOW())")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(DynamicConfig dynamicConfig);

    @Select("SELECT " + COLS + " FROM dynamic_config WHERE id = #{id}")
    DynamicConfig findById(Long id);

    @Select("SELECT " + COLS + " FROM dynamic_config WHERE env = #{env} AND version_range = #{versionRange}")
    DynamicConfig findByEnvAndVersionRange(@Param("env") String env, @Param("versionRange") String versionRange);

    @Select("<script>" +
            "SELECT " + COLS + " FROM dynamic_config " +
            "WHERE id = #{id} " +
            "<if test='env != null'>AND env = #{env}</if>" +
            "</script>")
    DynamicConfig findByIdAndEnv(@Param("id") Long id, @Param("env") String env);

    @Select("<script>" +
            "SELECT " + COLS + " FROM dynamic_config " +
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
            "SELECT " + COLS + " FROM dynamic_config " +
            "<where>" +
            "  <if test='env != null'>AND env = #{env}</if>" +
            "</where>" +
            "ORDER BY update_time DESC" +
            "</script>")
    List<DynamicConfig> findByEnv(@Param("env") String env);
}
