package com.app.appplatform.util;

/**
 * 版本号比较工具类，支持语义化版本 (SemVer)
 */
public class VersionUtil {

    /**
     * 比较两个版本号
     * @return 0:相等, 1:v1 > v2, -1:v1 < v2
     */
    public static int compare(String v1, String v2) {
        if (v1 == null || v2 == null) return 0;
        if (v1.equals(v2)) return 0;

        String[] parts1 = v1.split("\\.");
        String[] parts2 = v2.split("\\.");
        int length = Math.max(parts1.length, parts2.length);

        for (int i = 0; i < length; i++) {
            int p1 = i < parts1.length ? Integer.parseInt(parts1[i]) : 0;
            int p2 = i < parts2.length ? Integer.parseInt(parts2[i]) : 0;
            if (p1 > p2) return 1;
            if (p1 < p2) return -1;
        }
        return 0;
    }

    /**
     * 判断指定版本是否匹配版本范围规则
     * @param version 待校验的版本 (如 1.2.5)
     * @param range 规则 (如 1.2.0, 1.0.0-2.0.0, 1.5.0+, *)
     * @return 是否匹配
     */
    public static boolean isMatch(String version, String range) {
        if (range == null || range.isEmpty()) return false;
        if ("*".equals(range)) return true;

        // 1. 大于等于匹配 (1.5.0+)
        if (range.endsWith("+")) {
            String minVersion = range.substring(0, range.length() - 1);
            return compare(version, minVersion) >= 0;
        }

        // 2. 闭区间范围匹配 (1.0.0-2.0.0)
        if (range.contains("-")) {
            String[] parts = range.split("-");
            if (parts.length == 2) {
                return compare(version, parts[0]) >= 0 && compare(version, parts[1]) <= 0;
            }
        }

        // 3. 精确匹配
        return compare(version, range) == 0;
    }
}
