package com.app.gateway.config;

import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GatewayConfig {

    @Bean
    public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
        return builder.routes()
                // 认证服务
                .route("auth-service", r -> r.path("/auth/**")
                        .uri("lb://auth-service"))
                // 应用管理服务
                .route("app-service", r -> r.path("/api-app/**", "/api-modules/**", "/api-dynamic-config/**")
                        .uri("lb://app-service"))
                // 日志服务
                .route("log-service", r -> r.path("/api-logs/**")
                        .uri("lb://log-service"))
                // 事件追踪服务
                .route("event-service", r -> r.path("/api-events/**")
                        .uri("lb://event-service"))
                // 崩溃报告服务
                .route("crash-service", r -> r.path("/api/crash/**")
                        .uri("lb://crash-service"))
                // 文件存储服务
                .route("file-service", r -> r.path("/api-files/**")
                        .uri("lb://file-service"))
                // 商店链接服务
                .route("store-service", r -> r.path("/store-link-config/**")
                        .uri("lb://store-service"))
                // 绩效评估服务
                .route("perf-service", r -> r.path("/api-performance-review/**")
                        .uri("lb://perf-service"))
                .build();
    }
}
