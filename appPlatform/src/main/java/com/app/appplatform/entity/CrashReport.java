package com.app.appplatform.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

/**
 * Flutter Crash SDK Report Entity
 * Used to store application crash information
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class CrashReport {
    
    /**
     * Primary key ID
     */
    private Long id;
    
    /**
     * Crash unique identifier
     */
    private String crashId;
    
    /**
     * Application ID
     */
    private String appId;
    
    /**
     * User ID
     */
    private String userId;
    
    /**
     * Session ID
     */
    private String sessionId;
    
    /**
     * Crash type (error/exception/fatal/anr)
     */
    private String crashType;
    
    /**
     * Error message
     */
    private String message;
    
    /**
     * Stack trace information
     */
    private String stackTrace;
    
    /**
     * Application version
     */
    private String appVersion;
    
    /**
     * Application build number
     */
    private String appBuildNumber;
    
    /**
     * Device information
     */
    private DeviceInfo deviceInfo;
    
    /**
     * Device model
     */
    private String deviceModel;
    
    /**
     * Device brand
     */
    private String deviceBrand;
    
    /**
     * Operating system version
     */
    private String osVersion;
    
    /**
     * Platform type
     */
    private String platform;
    
    /**
     * Screen resolution
     */
    private String screenResolution;
    
    /**
     * Total memory (MB)
     */
    private Integer totalMemory;
    
    /**
     * Available memory (MB)
     */
    private Integer availableMemory;
    
    /**
     * Network type
     */
    private String networkType;
    
    /**
     * Battery level percentage
     */
    private Integer batteryLevel;
    
    /**
     * Custom business data (JSON format)
     */
    private String customData;
    
    /**
     * Crash occurrence time
     */
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
    private LocalDateTime crashTimestamp;
    
    /**
     * Report time
     */
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
    private LocalDateTime reportTimestamp;
    
    /**
     * SDK version number
     */
    private String sdkVersion;
    
    /**
     * Logical deletion flag (0=not deleted, 1=deleted)
     */
    private Boolean deleted;
    
    /**
     * Deletion time
     */
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
    private LocalDateTime deletedAt;
    
    /**
     * Creation time
     */
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
    private LocalDateTime createdAt;

    /**
     * Constructor with DeviceInfo
     */
    public CrashReport(String crashId, String appId, String userId, String sessionId,
                      String crashType, String message, String stackTrace,
                      String appVersion, String appBuildNumber, DeviceInfo deviceInfo,
                      String customData, LocalDateTime crashTimestamp,
                      LocalDateTime reportTimestamp, String sdkVersion) {
        this.crashId = crashId;
        this.appId = appId;
        this.userId = userId;
        this.sessionId = sessionId;
        this.crashType = crashType;
        this.message = message;
        this.stackTrace = stackTrace;
        this.appVersion = appVersion;
        this.appBuildNumber = appBuildNumber;
        this.deviceInfo = deviceInfo;
        this.customData = customData;
        this.crashTimestamp = crashTimestamp;
        this.reportTimestamp = reportTimestamp;
        this.sdkVersion = sdkVersion;

        // Extract fields from DeviceInfo to database fields
        if (deviceInfo != null) {
            this.deviceModel = deviceInfo.getModel();
            this.deviceBrand = deviceInfo.getBrand();
            this.osVersion = deviceInfo.getOsVersion();
            this.platform = deviceInfo.getPlatform();
            this.screenResolution = deviceInfo.getScreenResolution();
            this.totalMemory = deviceInfo.getTotalMemory();
            this.availableMemory = deviceInfo.getAvailableMemory();
            this.networkType = deviceInfo.getNetworkType();
            this.batteryLevel = deviceInfo.getBatteryLevel();
        }
    }

    @Override
    public String toString() {
        return "CrashReport{" +
                "id=" + id +
                ", crashId='" + crashId + '\'' +
                ", appId='" + appId + '\'' +
                ", userId='" + userId + '\'' +
                ", sessionId='" + sessionId + '\'' +
                ", crashType='" + crashType + '\'' +
                ", message='" + message + '\'' +
                ", appVersion='" + appVersion + '\'' +
                ", appBuildNumber='" + appBuildNumber + '\'' +
                ", crashTimestamp=" + crashTimestamp +
                ", reportTimestamp=" + reportTimestamp +
                ", sdkVersion='" + sdkVersion + '\'' +
                ", deleted=" + deleted +
                '}';
    }
}
