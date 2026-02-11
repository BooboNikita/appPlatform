package com.app.appplatform.util;

import com.fasterxml.jackson.core.type.TypeReference;
import java.util.Map;

/**
 * 应用信息解析工具类
 * 用于从应用信息JSON字符串中解析应用相关信息
 */
public class AppInfoUtil {

    /**
     * 解析应用信息获取版本号（包名已固定）
     * @param appInfoHeader 应用信息header（JSON格式字符串）
     * @return 包含version的AppInfoData对象，解析失败抛出异常
     * @throws IllegalArgumentException 当appInfo为空、格式错误或缺少version字段时抛出
     */
    public static AppInfoData parseAppInfo(String appInfoHeader) {
        if (appInfoHeader == null) {
            throw new IllegalArgumentException("缺少appinfo请求头");
        }

        // 解析appinfo
        Map<String, Object> appInfo = JsonUtil.toObject(appInfoHeader, new TypeReference<Map<String, Object>>() {});
        if (appInfo == null) {
            throw new IllegalArgumentException("appinfo格式错误");
        }

        String currentVersion = (String) appInfo.get("version");

        if (currentVersion == null) {
            throw new IllegalArgumentException("appinfo缺少必要字段(version)");
        }

        return new AppInfoData(currentVersion);
    }

    /**
     * 应用信息数据类
     */
    public static class AppInfoData {
        private final String version;

        public AppInfoData(String version) {
            this.version = version;
        }

        public String getVersion() {
            return version;
        }
    }
}
