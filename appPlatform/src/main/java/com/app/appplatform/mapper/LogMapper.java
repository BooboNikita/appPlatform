package com.app.appplatform.mapper;

import com.app.appplatform.entity.LogInfo;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface LogMapper {
    
    @Insert("INSERT INTO log_info (username, nickname, upload_time, path, app_name, version) " +
           "VALUES (#{username}, #{nickname}, #{uploadTime}, #{path}, #{appName}, #{version})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insert(LogInfo logInfo);
    
    @Update("UPDATE log_info SET username=#{username}, nickname=#{nickname}, " +
           "upload_time=#{uploadTime}, path=#{path}, app_name=#{appName}, version=#{version} " +
           "WHERE id=#{id}")
    void update(LogInfo logInfo);
    
    @Select("SELECT * FROM log_info WHERE id = #{id}")
    LogInfo findById(Integer id);
    
    @Select({"<script>",
            "SELECT * FROM log_info WHERE 1=1 ",
            "<if test='appName != null and appName != \"\"'> AND app_name LIKE CONCAT('%', #{appName}, '%')</if>",
            "<if test='username != null and username != \"\"'> AND username LIKE CONCAT('%', #{username}, '%')</if>",
            "<if test='startDate != null and startDate != \"\"'> AND DATE(upload_time) &gt;= #{startDate}</if>",
            "<if test='endDate != null and endDate != \"\"'> AND upload_time &lt;= #{endDate}</if>",
            " ORDER BY upload_time DESC",
            "</script>"})
    List<LogInfo> findByCondition(
            @Param("appName") String appName, 
            @Param("username") String username,
            @Param("startDate") String startDate,
            @Param("endDate") String endDate);
    
    @Delete("DELETE FROM log_info WHERE id = #{id}")
    void delete(Integer id);
}
