package com.app.appplatform.util;

import com.fasterxml.jackson.core.type.TypeReference;
import java.util.Map;

/**
 * 设备信息解析工具类
 * 用于从设备信息JSON字符串中解析设备相关信息
 */
public class DeviceUtil {

    /**
     * 解析设备信息获取品牌
     * @param deviceInfoHeader 设备信息header（JSON格式字符串）
     * @return 设备品牌，解析失败返回null
     */
    public static String parseDeviceBrand(String deviceInfoHeader) {
        if (deviceInfoHeader == null) {
            return null;
        }
        
        try {
            Map<String, Object> deviceInfo = JsonUtil.toObject(deviceInfoHeader, new TypeReference<>() {});
            if (deviceInfo != null) {
                return (String) deviceInfo.get("brand");
            }
        } catch (Exception e) {
            // 解析失败，记录日志但不影响主流程
            System.err.println("Failed to parse deviceInfo: " + e.getMessage());
        }
        
        return null;
    }
}
