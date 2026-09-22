package com.app.appplatform.util;

import com.app.appplatform.entity.DynamicConfigItem;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Set;

/**
 * 动态配置灰度下发工具类
 *
 * 判定优先级：停用 > 黑名单（始终生效） > 未开启灰度全量 > 白名单 > 百分比灰度 > 默认不下发
 */
public class GrayReleaseUtil {

    private GrayReleaseUtil() {
    }

    /**
     * 判断某配置项是否对指定用户下发
     *
     * @param item     配置项
     * @param whiteSet 白名单用户名集合
     * @param blackSet 黑名单用户名集合
     * @param username 请求用户名，可为空（为空时灰度中的配置项不下发）
     */
    public static boolean isReleased(DynamicConfigItem item, Set<String> whiteSet, Set<String> blackSet, String username) {
        if (item == null) {
            return false;
        }
        // 1. 停用，永不下发
        if (item.getEnabled() == null || item.getEnabled() == 0) {
            return false;
        }
        // 2. 黑名单最优先，始终剔除（无论是否开启灰度）
        if (username != null && blackSet != null && blackSet.contains(username)) {
            return false;
        }
        // 3. 未开启灰度：除黑名单外全量下发
        if (item.getGrayEnabled() == null || item.getGrayEnabled() == 0) {
            return true;
        }
        // 4. 白名单次之，一定下发
        if (username != null && whiteSet != null && whiteSet.contains(username)) {
            return true;
        }
        // 5. 无法判断身份，灰度中的配置项不下发
        if (username == null || username.isEmpty()) {
            return false;
        }
        // 6. 百分比灰度
        int percent = clampPercent(item.getGrayPercent());
        if (percent <= 0) {
            return false;
        }
        if (percent >= 100) {
            return true;
        }
        return bucket(seedOf(item), username) < percent;
    }

    /**
     * username 为空的重载（灰度中的配置项恒不下发）
     */
    public static boolean isReleased(DynamicConfigItem item, Set<String> whiteSet, Set<String> blackSet) {
        return isReleased(item, whiteSet, blackSet, null);
    }

    /**
     * 分桶 seed：每个配置项独立分桶，使不同功能的灰度人群错开
     */
    public static String seedOf(DynamicConfigItem item) {
        return item.getDomain() + "." + item.getItemKey();
    }

    /**
     * 确定性分桶：MD5(seed|username) 前 4 字节映射到 0-99
     * 同一用户 + 同一配置项的结果恒定，不依赖任何有状态服务
     */
    public static int bucket(String seed, String username) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] digest = md.digest((seed + "|" + username).getBytes(StandardCharsets.UTF_8));
            int v = ((digest[0] & 0xFF) << 24)
                    | ((digest[1] & 0xFF) << 16)
                    | ((digest[2] & 0xFF) << 8)
                    | (digest[3] & 0xFF);
            return Math.floorMod(v, 100);
        } catch (Exception e) {
            // MD5 不可用时退化为 hashCode（JVM 内一致）
            return Math.floorMod((seed + "|" + username).hashCode(), 100);
        }
    }

    /**
     * 灰度百分比越界裁剪
     */
    public static int clampPercent(Integer percent) {
        if (percent == null) {
            return 0;
        }
        return Math.max(0, Math.min(100, percent));
    }
}
