package com.app.appplatform.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class AppModule {
    private Long id;
    private String title;       // 模块标题
    private String iconUrl;     // 图标URL
    private String targetUrl;   // 跳转路径
    private Integer port;       // 目标URL端口
    private String color;       // 模块颜色
    private String route;       // 路由类型，对应RouteType枚举
    private Integer sortOrder;  // 排序字段
    private Boolean isActive;   // 是否启用
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private boolean hideForTest;
}