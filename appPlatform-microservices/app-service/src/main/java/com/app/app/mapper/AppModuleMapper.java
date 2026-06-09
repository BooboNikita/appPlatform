package com.app.app.mapper;

import com.app.app.entity.AppModule;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface AppModuleMapper {
    int insert(AppModule appModule);

    AppModule selectById(Long id);

    List<AppModule> selectAll();

    List<AppModule> selectActiveModules();

    int update(AppModule appModule);

    int deleteById(Long id);

    AppModule selectByTitle(@Param("title") String title);
}
