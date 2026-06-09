package com.app.crash.util;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Map;

/**
 * DeviceInfo parsing utility class
 * Used to parse device information from deviceInfo header
 */
public class DeviceInfoUtil {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Parse device info from header
     * @param deviceInfoHeader Device info header (JSON format string)
     * @return DeviceInfoData object, throws exception if parsing fails
     * @throws IllegalArgumentException When deviceInfo is null, format error, or missing required fields
     */
    public static DeviceInfoData parseDeviceInfo(String deviceInfoHeader) {
        if (deviceInfoHeader == null) {
            throw new IllegalArgumentException("Missing deviceInfo header");
        }

        try {
            // Parse deviceInfo
            Map<String, Object> deviceInfo = objectMapper.readValue(deviceInfoHeader, new TypeReference<Map<String, Object>>() {});
            if (deviceInfo == null) {
                throw new IllegalArgumentException("deviceInfo format error");
            }

            return new DeviceInfoData(
                (String) deviceInfo.get("deviceId"),
                (String) deviceInfo.get("brand"),
                (String) deviceInfo.get("model"),
                (String) deviceInfo.get("os"),
                (String) deviceInfo.get("osVersion"),
                (String) deviceInfo.get("ip"),
                (String) deviceInfo.get("networkType"),
                (String) deviceInfo.get("screenResolution")
            );
        } catch (Exception e) {
            throw new IllegalArgumentException("deviceInfo parse error: " + e.getMessage());
        }
    }

    /**
     * Device info data class
     */
    public static class DeviceInfoData {
        private final String deviceId;
        private final String brand;
        private final String model;
        private final String os;
        private final String osVersion;
        private final String ip;
        private final String networkType;
        private final String screenResolution;

        public DeviceInfoData(String deviceId, String brand, String model, String os,
                            String osVersion, String ip, String networkType, String screenResolution) {
            this.deviceId = deviceId;
            this.brand = brand;
            this.model = model;
            this.os = os;
            this.osVersion = osVersion;
            this.ip = ip;
            this.networkType = networkType;
            this.screenResolution = screenResolution;
        }

        public String getDeviceId() {
            return deviceId;
        }

        public String getBrand() {
            return brand;
        }

        public String getModel() {
            return model;
        }

        public String getOs() {
            return os;
        }

        public String getOsVersion() {
            return osVersion;
        }

        public String getIp() {
            return ip;
        }

        public String getNetworkType() {
            return networkType;
        }

        public String getScreenResolution() {
            return screenResolution;
        }
    }
}
