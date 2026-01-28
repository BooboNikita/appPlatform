package com.app.appplatform.mapper.primary;

import com.app.appplatform.entity.AppEvent;
import org.apache.ibatis.annotations.*;
import org.apache.ibatis.type.LocalDateTimeTypeHandler;

import java.util.List;

@Mapper
public interface AppEventMapper {
    @Insert("""
        INSERT INTO app_event (
            app_ver, app_buildNum, user_id, user_name, 
            event_id, event_type, event_time, recv_time,
            page_url, referrer, session_id, os, os_ver,
            device_id, device_model, device_brand, device_ip, network_type,
            screen_resolution, extra, status
        ) VALUES (
            #{app.version}, #{app.buildNumber}, #{userId}, #{userName}, 
            #{eventInfo.eventId}, #{eventInfo.eventType}, #{eventInfo.eventTime}, #{eventInfo.recvTime},
            #{pageUrl}, #{referrer}, #{sessionId}, #{device.os}, #{device.osVersion},
            #{device.deviceId}, #{device.model}, #{device.brand}, #{device.ip}, #{device.networkType},
            #{device.screenResolution}, #{extra}, #{status}
        )
    """)
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(AppEvent appEvent);

    @Select("SELECT * FROM app_event WHERE id = #{id}")
    @Results({
        // Top-level fields
        @Result(property = "id", column = "id"),
        @Result(property = "userId", column = "user_id"),
        @Result(property = "userName", column = "user_name"),
        @Result(property = "sessionId", column = "session_id"),
        @Result(property = "pageUrl", column = "page_url"),
        @Result(property = "referrer", column = "referrer"),
        @Result(property = "status", column = "status"),
        @Result(property = "extra", column = "extra"),
        
        // AppInfo mappings
        @Result(property = "app.version", column = "app_ver"),
        @Result(property = "app.buildNumber", column = "app_buildNum"),
        
        // DeviceInfo mappings
        @Result(property = "device.deviceId", column = "device_id"),
        @Result(property = "device.model", column = "device_model"),
        @Result(property = "device.brand", column = "device_brand"),
        @Result(property = "device.ip", column = "device_ip"),
        @Result(property = "device.os", column = "os"),
        @Result(property = "device.osVersion", column = "os_ver"),
        @Result(property = "device.networkType", column = "network_type"),
        @Result(property = "device.screenResolution", column = "screen_resolution"),
        
        // EventInfo mappings
        @Result(property = "eventInfo.eventId", column = "event_id"),
        @Result(property = "eventInfo.eventType", column = "event_type"),
        @Result(property = "eventInfo.eventTime", column = "event_time", typeHandler = LocalDateTimeTypeHandler.class),
        @Result(property = "eventInfo.recvTime", column = "recv_time", typeHandler = LocalDateTimeTypeHandler.class),
        @Result(property = "eventInfo.eventContent", column = "event_content")
    })
    AppEvent selectById(Long id);

    @Select({
        "<script>",
        "SELECT * FROM app_event ",
        "<where>",
        "   <if test='userId != null and userId != \"\"'> AND user_id LIKE CONCAT('%', #{userId}, '%') </if>",
        "   <if test='userName != null and userName != \"\"'> AND user_name LIKE CONCAT('%', #{userName}, '%') </if>",
        "   <if test='eventId != null and eventId != \"\"'> AND event_id LIKE CONCAT('%', #{eventId}, '%') </if>",
        "   <if test='pageUrl != null and pageUrl != \"\"'> AND page_url LIKE CONCAT('%', #{pageUrl}, '%') </if>",
        "   <if test='eventType != null and eventType != \"\"'> AND event_type = #{eventType} </if>",
        "   <if test='filterKey != null and filterKey != \"\" and filterValue != null and filterValue != \"\"'> ",
        "       <choose>",
        "           <when test='filterKey == \"userId\"'> AND user_id LIKE CONCAT('%', #{filterValue}, '%') </when>",
        "           <when test='filterKey == \"userName\"'> AND user_name LIKE CONCAT('%', #{filterValue}, '%') </when>",
        "           <when test='filterKey == \"eventId\"'> AND event_id LIKE CONCAT('%', #{filterValue}, '%') </when>",
        "           <when test='filterKey == \"pageUrl\"'> AND page_url LIKE CONCAT('%', #{filterValue}, '%') </when>",
        "           <when test='filterKey == \"eventType\"'> AND event_type = #{filterValue} </when>",
        "       </choose>",
        "   </if>",
        "</where>",
        " ORDER BY recv_time DESC",
        "</script>"
    })
    @Results({
        // 保持原有的 @Results 映射
        @Result(property = "id", column = "id"),
        @Result(property = "userId", column = "user_id"),
        @Result(property = "userName", column = "user_name"),
        @Result(property = "sessionId", column = "session_id"),
        @Result(property = "pageUrl", column = "page_url"),
        @Result(property = "referrer", column = "referrer"),
        @Result(property = "status", column = "status"),
        @Result(property = "extra", column = "extra"),
        @Result(property = "app.version", column = "app_ver"),
        @Result(property = "app.buildNumber", column = "app_buildNum"),
        @Result(property = "device.deviceId", column = "device_id"),
        @Result(property = "device.model", column = "device_model"),
        @Result(property = "device.brand", column = "device_brand"),
        @Result(property = "device.ip", column = "device_ip"),
        @Result(property = "device.os", column = "os"),
        @Result(property = "device.osVersion", column = "os_ver"),
        @Result(property = "device.networkType", column = "network_type"),
        @Result(property = "device.screenResolution", column = "screen_resolution"),
        @Result(property = "eventInfo.eventId", column = "event_id"),
        @Result(property = "eventInfo.eventType", column = "event_type"),
        @Result(property = "eventInfo.eventTime", column = "event_time", typeHandler = LocalDateTimeTypeHandler.class),
        @Result(property = "eventInfo.recvTime", column = "recv_time", typeHandler = LocalDateTimeTypeHandler.class),
        @Result(property = "eventInfo.eventContent", column = "event_content")
    })
    // 使用PageHelper进行分页，不需要在SQL中写limit
    List<AppEvent> selectRecentEventsWithFilters(
            @Param("userId") String userId,
            @Param("userName") String userName,
            @Param("eventId") String eventId,
            @Param("pageUrl") String pageUrl,
            @Param("eventType") String eventType,
            @Param("filterKey") String filterKey,
            @Param("filterValue") String filterValue
    );
    
    @Select("SELECT * FROM app_event ORDER BY recv_time DESC")
    @Results({
        // 保持原有的 @Results 映射
        @Result(property = "id", column = "id"),
        @Result(property = "userId", column = "user_id"),
        @Result(property = "userName", column = "user_name"),
        @Result(property = "sessionId", column = "session_id"),
        @Result(property = "pageUrl", column = "page_url"),
        @Result(property = "referrer", column = "referrer"),
        @Result(property = "status", column = "status"),
        @Result(property = "extra", column = "extra"),
        @Result(property = "app.version", column = "app_ver"),
        @Result(property = "app.buildNumber", column = "app_buildNum"),
        @Result(property = "device.deviceId", column = "device_id"),
        @Result(property = "device.model", column = "device_model"),
        @Result(property = "device.brand", column = "device_brand"),
        @Result(property = "device.ip", column = "device_ip"),
        @Result(property = "device.os", column = "os"),
        @Result(property = "device.osVersion", column = "os_ver"),
        @Result(property = "device.networkType", column = "network_type"),
        @Result(property = "device.screenResolution", column = "screen_resolution"),
        @Result(property = "eventInfo.eventId", column = "event_id"),
        @Result(property = "eventInfo.eventType", column = "event_type"),
        @Result(property = "eventInfo.eventTime", column = "event_time"),
        @Result(property = "eventInfo.recvTime", column = "recv_time"),
        @Result(property = "eventInfo.eventContent", column = "event_content")
    })
    // 使用PageHelper进行分页，不需要在SQL中写limit
    List<AppEvent> selectRecentEvents();
}
