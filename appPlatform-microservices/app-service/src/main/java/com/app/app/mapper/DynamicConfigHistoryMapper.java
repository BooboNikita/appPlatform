package com.app.app.mapper;

import com.app.app.entity.DynamicConfigHistory;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface DynamicConfigHistoryMapper {
    int insert(DynamicConfigHistory history);

    DynamicConfigHistory selectById(Long id);

    List<DynamicConfigHistory> selectByConfigId(Long configId);

    List<DynamicConfigHistory> selectAll();

    List<DynamicConfigHistory> selectByCondition(@Param("env") String env, @Param("versionRange") String versionRange);
}
