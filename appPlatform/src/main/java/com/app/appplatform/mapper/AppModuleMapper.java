package com.app.appplatform.mapper;

import com.app.appplatform.entity.AppModule;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface AppModuleMapper {
    @Select("SELECT * FROM app_module WHERE isActive = true ORDER BY sortOrder ASC, id ASC")
    List<AppModule> findAllActive();

    @Select("SELECT * FROM app_module WHERE isActive = true AND hideForTest = false ORDER BY sortOrder ASC, id ASC")
    List<AppModule> findAllActiveAndHideForTest();

    @Select("SELECT * FROM app_module ORDER BY sortOrder ASC, id ASC")
    List<AppModule> findAll();

    @Select("SELECT * FROM app_module WHERE id = #{id}")
    AppModule findById(Long id);

    @Insert("INSERT INTO app_module(title, iconUrl, targetUrl, port, color, route, sortOrder, isActive, hideForTest) " +
            "VALUES(#{title}, #{iconUrl}, #{targetUrl}, #{port}, #{color}, #{route}, #{sortOrder}, #{isActive}, #{hideForTest})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insert(AppModule appModule);

    @Update("UPDATE app_module SET " +
            "title = #{title}, " +
            "iconUrl = #{iconUrl}, " +
            "targetUrl = #{targetUrl}, " +
            "port = #{port}, " +
            "color = #{color}, " +
            "route = #{route}, " +
            "sortOrder = #{sortOrder}, " +
            "isActive = #{isActive}, " +
            "hideForTest = #{hideForTest} " +
            "WHERE id = #{id}")
    void update(AppModule appModule);

    @Delete("DELETE FROM app_module WHERE id = #{id}")
    void delete(Long id);
}