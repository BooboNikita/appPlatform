package com.app.app.mapper;

import com.app.app.entity.DynamicConfig;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface DynamicConfigMapper {
    int insert(DynamicConfig config);

    DynamicConfig selectById(Long id);

    List<DynamicConfig> selectAll();

    List<DynamicConfig> selectByEnv(@Param("env") String env);

    DynamicConfig selectLatestByVersionAndEnv(@Param("version") String version, @Param("env") String env);

    int update(DynamicConfig config);

    int deleteById(Long id);
}
