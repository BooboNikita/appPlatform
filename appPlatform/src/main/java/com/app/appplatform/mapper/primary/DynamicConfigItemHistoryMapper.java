package com.app.appplatform.mapper.primary;

import com.app.appplatform.entity.DynamicConfigItemHistory;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface DynamicConfigItemHistoryMapper {

    String COLS = "id, config_id as configId, domain, item_key as itemKey, version_range as versionRange, env, " +
            "item_value as itemValue, value_type as valueType, enabled, gray_enabled as grayEnabled, " +
            "gray_percent as grayPercent, gray_users_json as grayUsersJson, remark, " +
            "operation_type as operationType, operator, create_time as createTime";

    @Insert("INSERT INTO dynamic_config_item_history(config_id, domain, item_key, version_range, env, item_value, value_type, enabled, gray_enabled, gray_percent, gray_users_json, remark, operation_type, operator, create_time) " +
            "VALUES(#{configId}, #{domain}, #{itemKey}, #{versionRange}, #{env}, #{itemValue}, #{valueType}, #{enabled}, #{grayEnabled}, #{grayPercent}, #{grayUsersJson}, #{remark}, #{operationType}, #{operator}, NOW())")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(DynamicConfigItemHistory history);

    @Select("SELECT " + COLS + " FROM dynamic_config_item_history " +
            "WHERE config_id = #{configId} AND domain = #{domain} AND item_key = #{itemKey} AND version_range = #{versionRange} " +
            "ORDER BY create_time DESC, id DESC")
    List<DynamicConfigItemHistory> findByConfigIdAndKey(@Param("configId") Long configId,
                                                        @Param("domain") String domain,
                                                        @Param("itemKey") String itemKey,
                                                        @Param("versionRange") String versionRange);

    @Select("SELECT " + COLS + " FROM dynamic_config_item_history " +
            "WHERE config_id = #{configId} AND domain = #{domain} AND item_key = #{itemKey} " +
            "ORDER BY create_time DESC, id DESC")
    List<DynamicConfigItemHistory> findByConfigIdAndKeyIgnoreVersion(@Param("configId") Long configId,
                                                                     @Param("domain") String domain,
                                                                     @Param("itemKey") String itemKey);

    @Select("SELECT " + COLS + " FROM dynamic_config_item_history WHERE id = #{id}")
    DynamicConfigItemHistory findById(Long id);

    @Select("SELECT " + COLS + " FROM dynamic_config_item_history " +
            "WHERE config_id = #{configId} AND domain = #{domain} AND item_key = #{itemKey} AND version_range = #{versionRange} " +
            "ORDER BY create_time DESC, id DESC LIMIT 1")
    DynamicConfigItemHistory findLatestByConfigIdAndKey(@Param("configId") Long configId,
                                                        @Param("domain") String domain,
                                                        @Param("itemKey") String itemKey,
                                                        @Param("versionRange") String versionRange);

    @Delete("DELETE FROM dynamic_config_item_history WHERE config_id = #{configId}")
    int deleteByConfigId(Long configId);
}
