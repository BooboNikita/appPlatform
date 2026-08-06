package com.app.appplatform.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Flutter Crash SDK API Protocol -  CrashReportRequest
 * 2024-04-07 API Protocol
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CrashReportRequest {
    
    private String crashId;
    
    private LocalDateTime timestamp;
    
    private String crashType;
    
    private String message;
    
    private String stackTrace;
    
    private String appVersion;
    
    private String appBuildNumber;
    
    private String username;
    
    private String nickname;
    
    private String version;
    
    private String sessionId;
    
    private Map<String, Object> customData;

    @Override
    public String toString() {
        return "CrashReportRequest{" +
                "crashId='" + crashId + '\'' +
                ", timestamp=" + timestamp +
                ", crashType='" + crashType + '\'' +
                ", message='" + message + '\'' +
                ", appVersion='" + appVersion + '\'' +
                ", appBuildNumber='" + appBuildNumber + '\'' +
                ", username='" + username + '\'' +
                ", nickname='" + nickname + '\'' +
                ", version='" + version + '\'' +
                ", sessionId='" + sessionId + '\'' +
                '}';
    }
}
