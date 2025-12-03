package com.app.appplatform.mapper;

import com.app.appplatform.entity.AppEvent;
import org.apache.ibatis.annotations.*;

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
            #{event.eventId}, #{event.eventType}, #{event.eventTime}, #{event.recvTime},
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
        @Result(property = "event.eventId", column = "event_id"),
        @Result(property = "event.eventType", column = "event_type"),
        @Result(property = "event.eventTime", column = "event_time"),
        @Result(property = "event.recvTime", column = "recv_time"),
        @Result(property = "event.eventContent", column = "event_content")
    })
    AppEvent selectById(Long id);

    @Select("SELECT * FROM app_event ORDER BY recv_time DESC")
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
        @Result(property = "event.eventId", column = "event_id"),
        @Result(property = "event.eventType", column = "event_type"),
        @Result(property = "event.eventTime", column = "event_time"),
        @Result(property = "event.recvTime", column = "recv_time"),
        @Result(property = "event.eventContent", column = "event_content")
    })
    // 使用PageHelper进行分页，不需要在SQL中写limit
    List<AppEvent> selectRecentEvents();
}
