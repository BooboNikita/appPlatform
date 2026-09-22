package com.app.appplatform.mapper.primary;

import com.app.appplatform.entity.DynamicConfigItem;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface DynamicConfigItemMapper {

    String COLS = "id, config_id as configId, version_range as versionRange, env, domain, item_key as itemKey, " +
            "item_value as itemValue, value_type as valueType, enabled, gray_enabled as grayEnabled, " +
            "gray_percent as grayPercent, remark, create_time as createTime, update_time as updateTime";

    @Insert("INSERT INTO dynamic_config_item(config_id, version_range, env, domain, item_key, item_value, value_type, enabled, gray_enabled, gray_percent, remark, create_time, update_time) " +
            "VALUES(#{configId}, #{versionRange}, #{env}, #{domain}, #{itemKey}, #{itemValue}, #{valueType}, #{enabled}, #{grayEnabled}, #{grayPercent}, #{remark}, NOW(), NOW())")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(DynamicConfigItem item);

    @Select("SELECT " + COLS + " FROM dynamic_config_item WHERE id = #{id}")
    DynamicConfigItem findById(Long id);

    @Select("SELECT " + COLS + " FROM dynamic_config_item WHERE config_id = #{configId} ORDER BY id ASC")
    List<DynamicConfigItem> findByConfigId(Long configId);

    @Select("SELECT " + COLS + " FROM dynamic_config_item WHERE env = #{env} ORDER BY config_id ASC, id ASC")
    List<DynamicConfigItem> findByEnv(@Param("env") String env);

    @Select("SELECT " + COLS + " FROM dynamic_config_item WHERE config_id = #{configId} AND domain = #{domain} AND item_key = #{itemKey}")
    DynamicConfigItem findByConfigIdAndKey(@Param("configId") Long configId, @Param("domain") String domain, @Param("itemKey") String itemKey);

    @Select("SELECT " + COLS + " FROM dynamic_config_item WHERE config_id = #{configId} AND domain = #{domain} AND item_key = #{itemKey} AND version_range = #{versionRange}")
    DynamicConfigItem findByConfigIdKeyAndVersion(@Param("configId") Long configId, @Param("domain") String domain,
                                                  @Param("itemKey") String itemKey, @Param("versionRange") String versionRange);

    @Select("<script>" +
            "SELECT i.id, i.config_id as configId, i.version_range as versionRange, i.env, i.domain, i.item_key as itemKey, " +
            "i.item_value as itemValue, i.value_type as valueType, i.enabled, i.gray_enabled as grayEnabled, i.gray_percent as grayPercent, " +
            "i.remark, i.create_time as createTime, i.update_time as updateTime, " +
            "(SELECT COUNT(*) FROM dynamic_config_item_gray_user g WHERE g.item_id = i.id AND g.list_type = 'WHITE') as whiteCount, " +
            "(SELECT COUNT(*) FROM dynamic_config_item_gray_user g WHERE g.item_id = i.id AND g.list_type = 'BLACK') as blackCount " +
            "FROM dynamic_config_item i " +
            "<where>" +
            "  <if test=\"env != null and env != ''\">AND i.env = #{env}</if>" +
            "  <if test=\"versionRange != null and versionRange != ''\">AND i.version_range LIKE CONCAT('%', #{versionRange}, '%')</if>" +
            "  <if test=\"domain != null and domain != ''\">AND i.domain = #{domain}</if>" +
            "  <if test=\"keyword != null and keyword != ''\">AND (i.domain LIKE CONCAT('%', #{keyword}, '%') OR i.item_key LIKE CONCAT('%', #{keyword}, '%') OR i.remark LIKE CONCAT('%', #{keyword}, '%'))</if>" +
            "</where>" +
            "ORDER BY i.update_time DESC, i.id DESC" +
            "</script>")
    List<DynamicConfigItem> findPage(@Param("env") String env, @Param("versionRange") String versionRange,
                                     @Param("domain") String domain, @Param("keyword") String keyword);

    @Update("<script>" +
            "UPDATE dynamic_config_item " +
            "<set>" +
            "  <if test='itemValue != null'>item_value = #{itemValue},</if>" +
            "  <if test='valueType != null'>value_type = #{valueType},</if>" +
            "  <if test='enabled != null'>enabled = #{enabled},</if>" +
            "  <if test='grayEnabled != null'>gray_enabled = #{grayEnabled},</if>" +
            "  <if test='grayPercent != null'>gray_percent = #{grayPercent},</if>" +
            "  <if test='remark != null'>remark = #{remark},</if>" +
            "  <if test='versionRange != null'>version_range = #{versionRange},</if>" +
            "  update_time = NOW()" +
            "</set>" +
            "WHERE id = #{id}" +
            "</script>")
    int update(DynamicConfigItem item);

    @Update("UPDATE dynamic_config_item SET version_range = #{versionRange}, env = #{env}, update_time = NOW() WHERE config_id = #{configId}")
    int updateMetaByConfigId(@Param("configId") Long configId, @Param("versionRange") String versionRange, @Param("env") String env);

    @Delete("DELETE FROM dynamic_config_item WHERE id = #{id}")
    int deleteById(Long id);

    @Delete("DELETE FROM dynamic_config_item WHERE config_id = #{configId}")
    int deleteByConfigId(Long configId);
}
