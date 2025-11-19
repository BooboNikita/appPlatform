package com.app.appplatform.enums;

/**
 * 路由类型枚举
 */
public enum RouteType {
    UNDER_DEVELOPMENT("under_development", "开发中"),
    INNER("inner", "内部页面"),
    WEBVIEW("webview", "网页");

    private final String value;
    private final String description;

    RouteType(String value, String description) {
        this.value = value;
        this.description = description;
    }

    public String getValue() {
        return value;
    }

    public String getDescription() {
        return description;
    }

    /**
     * 根据值获取枚举
     */
    public static RouteType fromValue(String value) {
        for (RouteType type : RouteType.values()) {
            if (type.value.equalsIgnoreCase(value)) {
                return type;
            }
        }
        throw new IllegalArgumentException("未知的路由类型: " + value);
    }

    /**
     * 检查值是否有效
     */
    public static boolean isValid(String value) {
        for (RouteType type : RouteType.values()) {
            if (type.value.equalsIgnoreCase(value)) {
                return true;
            }
        }
        return false;
    }
}
