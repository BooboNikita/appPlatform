package com.app.crash.entity;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * 设备信息实体类
 * 用于存储崩溃上报时的设备相关信息
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DeviceInfo {

    /**
     * 设备型号
     */
    private String model;

    /**
     * 设备品牌
     */
    private String brand;

    /**
     * 操作系统版本
     */
    private String osVersion;

    /**
     * 平台类型 (iOS/Android)
     */
    private String platform;

    /**
     * 屏幕分辨率
     */
    private String screenResolution;

    /**
     * 总内存(MB)
     */
    private Integer totalMemory;

    /**
     * 可用内存(MB)
     */
    private Integer availableMemory;

    /**
     * 网络类型
     */
    private String networkType;

    /**
     * 电量百分比(0-100)
     */
    private Integer batteryLevel;

    @Override
    public String toString() {
        return "DeviceInfo{" +
                "model='" + model + '\'' +
                ", brand='" + brand + '\'' +
                ", osVersion='" + osVersion + '\'' +
                ", platform='" + platform + '\'' +
                ", screenResolution='" + screenResolution + '\'' +
                ", totalMemory=" + totalMemory +
                ", availableMemory=" + availableMemory +
                ", networkType='" + networkType + '\'' +
                ", batteryLevel=" + batteryLevel +
                '}';
    }
}
