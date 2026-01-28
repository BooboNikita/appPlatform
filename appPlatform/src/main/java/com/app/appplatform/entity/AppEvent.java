package com.app.appplatform.entity;

import com.app.appplatform.config.json.UnixTimestampDeserializer;
import com.app.appplatform.config.json.UnixTimestampSerializer;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AppEvent {
    // 基础信息
    private Long id;
    private String userId;
    private String userName;
    private String nickname; // 用户昵称
    private String sessionId;
    private String pageUrl;
    private String referrer;
    private Integer status; // 0:正常, 1:测试
    
    // 应用信息
    private AppInfo app;
    
    // 设备信息
    private DeviceInfo device;
    
    // 事件信息
    private EventInfo eventInfo;
    
    // 其他扩展信息
    private String extra;

    @Data
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class AppInfo {
        private String version;      // 应用版本
        private String buildNumber;  // 构建号
    }

    @Data
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class DeviceInfo {
        private String deviceId;        // 设备ID
        private String model;           // 设备型号
        private String brand;           // 设备品牌
        private String ip;              // 设备IP
        private String os;              // 操作系统
        private String osVersion;       // 操作系统版本
        private String networkType;     // 网络类型
        private String screenResolution; // 屏幕分辨率
    }

    @Data
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class EventInfo {
        private String eventId;         // 事件ID
        private String eventType;       // 事件类型: view/click/exposure
        
        @JsonDeserialize(using = UnixTimestampDeserializer.class)
        @JsonSerialize(using = UnixTimestampSerializer.class)
        private LocalDateTime eventTime; // 事件时间
        
        @JsonDeserialize(using = UnixTimestampDeserializer.class)
        @JsonSerialize(using = UnixTimestampSerializer.class)
        private LocalDateTime recvTime; // 接收时间
    }
}
