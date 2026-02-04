package com.app.appplatform.mapper.primary;

import com.app.appplatform.entity.DynamicConfigHistory;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface DynamicConfigHistoryMapper {
    
    @Insert("INSERT INTO dynamic_config_history(config_id, version_range, file_url, env, remark, operation_type, operator, create_time) " +
            "VALUES(#{configId}, #{versionRange}, #{fileUrl}, #{env}, #{remark}, #{operationType}, #{operator}, NOW())")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(DynamicConfigHistory history);

    @Select("SELECT id, config_id as configId, version_range as versionRange, file_url as fileUrl, env, remark, " +
            "operation_type as operationType, operator, create_time as createTime FROM dynamic_config_history " +
            "WHERE config_id = #{configId} ORDER BY create_time DESC")
    List<DynamicConfigHistory> findByConfigId(Long configId);

    @Select("SELECT id, config_id as configId, version_range as versionRange, file_url as fileUrl, env, remark, " +
            "operation_type as operationType, operator, create_time as createTime FROM dynamic_config_history " +
            "WHERE id = #{historyId}")
    DynamicConfigHistory findById(Long historyId);

    @Select("<script>" +
            "SELECT h.id, h.config_id as configId, h.version_range as versionRange, h.file_url as fileUrl, h.env, h.remark, " +
            "h.operation_type as operationType, h.operator, h.create_time as createTime " +
            "FROM dynamic_config_history h " +
            "INNER JOIN dynamic_config c ON h.config_id = c.id " +
            "<where>" +
            "  <if test='env != null'>AND h.env = #{env}</if>" +
            "  <if test='versionRange != null'>AND h.version_range LIKE CONCAT('%', #{versionRange}, '%')</if>" +
            "</where>" +
            "ORDER BY h.create_time DESC" +
            "</script>")
    List<DynamicConfigHistory> findHistoryWithFilters(@Param("env") String env, @Param("versionRange") String versionRange);

    @Select("SELECT COUNT(*) FROM dynamic_config_history WHERE config_id = #{configId}")
    int countByConfigId(Long configId);
}
