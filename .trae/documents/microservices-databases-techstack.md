# AppPlatform 微服务拆分 - 数据库与技术栈清单

## 一、新建数据库列表

### 1.1 数据库规划概览

采用 **Database Per Service** 模式，每个微服务拥有独立的数据库，实现数据隔离和解耦。

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                           MySQL 实例 (或集群)                                 │
├─────────────────────────────────────────────────────────────────────────────┤
│                                                                             │
│  ┌─────────────────┐  ┌─────────────────┐  ┌─────────────────┐             │
│  │   auth_db       │  │   app_db        │  │   log_db        │             │
│  │   (认证服务)     │  │   (应用服务)     │  │   (日志服务)     │             │
│  │                 │  │                 │  │                 │             │
│  │  • user         │  │  • app_info     │  │  • log_info     │             │
│  │                 │  │  • app_module   │  │  • log_request  │             │
│  │                 │  │  • dynamic_config│  │                 │             │
│  │                 │  │  • dynamic_config_history              │             │
│  │                 │  │  • sys_config   │  │                 │             │
│  └─────────────────┘  └─────────────────┘  └─────────────────┘             │
│                                                                             │
│  ┌─────────────────┐  ┌─────────────────┐  ┌─────────────────┐             │
│  │   event_db      │  │   crash_db      │  │   store_db      │             │
│  │   (事件服务)     │  │   (崩溃服务)     │  │   (商店服务)     │             │
│  │                 │  │                 │  │                 │             │
│  │  • app_event    │  │  • crash_reports│  │  • store_link_config          │
│  │                 │  │                 │  │                 │             │
│  └─────────────────┘  └─────────────────┘  └─────────────────┘             │
│                                                                             │
│  ┌─────────────────┐  ┌─────────────────┐                                   │
│  │   perf_db       │  │   config_db     │                                   │
│  │   (绩效服务)     │  │   (配置服务)     │                                   │
│  │                 │  │                 │                                   │
│  │  • performance_review│  • sys_config │                                   │
│  │                 │  │                 │                                   │
│  └─────────────────┘  └─────────────────┘                                   │
│                                                                             │
└─────────────────────────────────────────────────────────────────────────────┘
```

### 1.2 数据库详细清单

#### 1. auth_db - 认证服务数据库

```sql
-- 数据库创建
CREATE DATABASE IF NOT EXISTS `auth_db` 
    DEFAULT CHARACTER SET utf8mb4 
    DEFAULT COLLATE utf8mb4_unicode_ci;

USE `auth_db`;

-- 用户表
CREATE TABLE IF NOT EXISTS `user` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `username` VARCHAR(50) NOT NULL UNIQUE COMMENT '用户名',
    `password` VARCHAR(100) NOT NULL COMMENT '密码(BCrypt加密)',
    `role` VARCHAR(20) NOT NULL DEFAULT 'USER' COMMENT '角色(ADMIN/USER)',
    `avatar` VARCHAR(255) COMMENT '头像URL',
    `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    `updated_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `deleted` TINYINT(1) DEFAULT 0 COMMENT '逻辑删除标记'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- 初始化管理员账号
INSERT INTO `user` (`username`, `password`, `role`, `avatar`) 
VALUES ('admin', '$2a$10$RKQHFEWWtI0Ch9qrlCCNtOCqj3B1IzWRQCmMl/RsVaIC1v026uMtm', 'ADMIN', 
        'http://172.31.101.166:8008/static/png/person-img-BRBJlwLp.png');
```

#### 2. app_db - 应用管理服务数据库

```sql
-- 数据库创建
CREATE DATABASE IF NOT EXISTS `app_db` 
    DEFAULT CHARACTER SET utf8mb4 
    DEFAULT COLLATE utf8mb4_unicode_ci;

USE `app_db`;

-- 应用信息表
CREATE TABLE IF NOT EXISTS `app_info` (
    `id` INT AUTO_INCREMENT PRIMARY KEY,
    `app_name` VARCHAR(255) NOT NULL COMMENT '应用名称',
    `package_name` VARCHAR(255) NOT NULL COMMENT '包名',
    `version` VARCHAR(255) NOT NULL COMMENT '版本号',
    `build_number` BIGINT NOT NULL COMMENT '构建号',
    `features` VARCHAR(255) NOT NULL COMMENT '新特性描述',
    `is_beta` BOOLEAN NOT NULL DEFAULT FALSE COMMENT '是否为beta版本',
    `path` VARCHAR(255) NOT NULL COMMENT 'MinIO文件存储路径',
    `create_time` TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `download_times` INT NOT NULL DEFAULT 0 COMMENT '下载次数',
    `size` VARCHAR(255) NOT NULL COMMENT '文件大小',
    `deleted` BOOLEAN NOT NULL DEFAULT FALSE COMMENT '逻辑删除标记',
    `show_update_popup` BOOLEAN NOT NULL DEFAULT FALSE COMMENT '是否显示更新弹窗',
    `force_update` BOOLEAN NOT NULL DEFAULT FALSE COMMENT '是否强制更新',
    INDEX `idx_package_name` (`package_name`),
    INDEX `idx_version` (`version`),
    INDEX `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='应用信息表';

-- 应用模块表
CREATE TABLE IF NOT EXISTS `app_module` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `module_name` VARCHAR(100) NOT NULL COMMENT '模块名称',
    `module_code` VARCHAR(50) NOT NULL UNIQUE COMMENT '模块代码',
    `description` VARCHAR(255) COMMENT '模块描述',
    `enabled` BOOLEAN NOT NULL DEFAULT TRUE COMMENT '是否启用',
    `sort_order` INT DEFAULT 0 COMMENT '排序顺序',
    `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    `updated_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX `idx_enabled` (`enabled`),
    INDEX `idx_module_code` (`module_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='应用模块表';

-- 动态配置表
CREATE TABLE IF NOT EXISTS `dynamic_config` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `config_name` VARCHAR(100) NOT NULL COMMENT '配置名称',
    `version_range` VARCHAR(100) NOT NULL COMMENT '版本范围(如: 1.0.0-2.0.0)',
    `env` VARCHAR(20) NOT NULL DEFAULT 'prod' COMMENT '环境(dev/prod)',
    `file_path` VARCHAR(255) NOT NULL COMMENT 'MinIO文件路径',
    `remark` VARCHAR(500) COMMENT '备注',
    `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    `updated_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `created_by` VARCHAR(50) COMMENT '创建人',
    INDEX `idx_version_range` (`version_range`),
    INDEX `idx_env` (`env`),
    INDEX `idx_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='动态配置表';

-- 动态配置历史表
CREATE TABLE IF NOT EXISTS `dynamic_config_history` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `config_id` BIGINT NOT NULL COMMENT '关联的配置ID',
    `config_name` VARCHAR(100) NOT NULL COMMENT '配置名称',
    `version_range` VARCHAR(100) NOT NULL COMMENT '版本范围',
    `env` VARCHAR(20) NOT NULL COMMENT '环境',
    `file_path` VARCHAR(255) NOT NULL COMMENT '文件路径',
    `remark` VARCHAR(500) COMMENT '备注',
    `operator` VARCHAR(50) COMMENT '操作人',
    `operation` VARCHAR(20) COMMENT '操作类型(CREATE/UPDATE/DELETE)',
    `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX `idx_config_id` (`config_id`),
    INDEX `idx_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='动态配置历史表';

-- 系统配置表
CREATE TABLE IF NOT EXISTS `sys_config` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `config_key` VARCHAR(100) NOT NULL UNIQUE COMMENT '配置键',
    `config_value` TEXT COMMENT '配置值',
    `description` VARCHAR(255) COMMENT '配置说明',
    `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    `updated_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX `idx_config_key` (`config_key`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统配置表';
```

#### 3. log_db - 日志服务数据库

```sql
-- 数据库创建
CREATE DATABASE IF NOT EXISTS `log_db` 
    DEFAULT CHARACTER SET utf8mb4 
    DEFAULT COLLATE utf8mb4_unicode_ci;

USE `log_db`;

-- 日志信息表
CREATE TABLE IF NOT EXISTS `log_info` (
    `id` INT AUTO_INCREMENT PRIMARY KEY,
    `username` VARCHAR(100) NOT NULL COMMENT '用户名',
    `nickname` VARCHAR(100) COMMENT '用户昵称',
    `upload_time` TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '上传时间',
    `path` TEXT NOT NULL COMMENT '文件路径(多个用逗号分隔)',
    `app_name` VARCHAR(100) NOT NULL COMMENT '应用名称',
    `version` VARCHAR(50) NOT NULL COMMENT '应用版本',
    `image_urls` TEXT COMMENT '截图URL列表',
    `problem` TEXT COMMENT '问题描述',
    INDEX `idx_username` (`username`),
    INDEX `idx_app_name` (`app_name`),
    INDEX `idx_upload_time` (`upload_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='日志信息表';

-- 日志请求表
CREATE TABLE IF NOT EXISTS `log_request` (
    `id` INT AUTO_INCREMENT PRIMARY KEY,
    `username` VARCHAR(100) NOT NULL COMMENT '目标用户名',
    `status` TINYINT NOT NULL DEFAULT 0 COMMENT '状态(0:待上传 1:已上传 2:超时)',
    `request_time` TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '请求时间',
    `expire_time` TIMESTAMP NOT NULL COMMENT '过期时间',
    `completed_time` TIMESTAMP NULL COMMENT '完成时间',
    INDEX `idx_username` (`username`),
    INDEX `idx_status` (`status`),
    INDEX `idx_expire_time` (`expire_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='日志请求表';
```

#### 4. event_db - 事件追踪服务数据库

```sql
-- 数据库创建
CREATE DATABASE IF NOT EXISTS `event_db` 
    DEFAULT CHARACTER SET utf8mb4 
    DEFAULT COLLATE utf8mb4_unicode_ci;

USE `event_db`;

-- 应用事件表
CREATE TABLE IF NOT EXISTS `app_event` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `app_ver` VARCHAR(255) NOT NULL COMMENT 'APP版本',
    `app_build_num` VARCHAR(255) NOT NULL COMMENT 'APP构建号',
    `user_id` VARCHAR(255) NOT NULL COMMENT '用户ID',
    `user_name` VARCHAR(255) COMMENT '用户名称',
    `event_id` VARCHAR(255) COMMENT '事件ID',
    `event_type` VARCHAR(50) COMMENT '事件类型(view/click/exposure)',
    `event_time` DATETIME COMMENT '事件发生时间',
    `recv_time` DATETIME NOT NULL COMMENT '接收时间',
    `page_url` VARCHAR(500) COMMENT '页面URL',
    `referrer` VARCHAR(500) COMMENT '来源页面',
    `session_id` VARCHAR(255) COMMENT '会话ID',
    `os` VARCHAR(50) COMMENT '操作系统',
    `os_ver` VARCHAR(50) COMMENT '操作系统版本',
    `device_id` VARCHAR(255) COMMENT '设备ID',
    `device_model` VARCHAR(100) COMMENT '设备型号',
    `device_brand` VARCHAR(100) COMMENT '设备品牌',
    `device_ip` VARCHAR(50) COMMENT '设备IP',
    `network_type` VARCHAR(20) COMMENT '网络类型',
    `screen_resolution` VARCHAR(50) COMMENT '屏幕分辨率',
    `extra` JSON COMMENT '扩展信息(JSON格式)',
    `status` TINYINT DEFAULT 0 COMMENT '状态(0:正常 1:测试)',
    INDEX `idx_user_id` (`user_id`),
    INDEX `idx_event_id` (`event_id`),
    INDEX `idx_event_time` (`event_time`),
    INDEX `idx_recv_time` (`recv_time`),
    INDEX `idx_page_url` (`page_url`),
    INDEX `idx_event_type` (`event_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='应用事件表';

-- 按时间分区建议(根据数据量)
-- ALTER TABLE app_event PARTITION BY RANGE (YEAR(recv_time)) (...);
```

#### 5. crash_db - 崩溃报告服务数据库

```sql
-- 数据库创建
CREATE DATABASE IF NOT EXISTS `crash_db` 
    DEFAULT CHARACTER SET utf8mb4 
    DEFAULT COLLATE utf8mb4_unicode_ci;

USE `crash_db`;

-- 崩溃报告表
CREATE TABLE IF NOT EXISTS `crash_reports` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `crash_id` VARCHAR(100) NOT NULL UNIQUE COMMENT '崩溃唯一标识',
    `app_id` VARCHAR(100) NOT NULL COMMENT '应用ID',
    `user_id` VARCHAR(100) COMMENT '用户ID',
    `session_id` VARCHAR(100) COMMENT '会话ID',
    `crash_type` VARCHAR(50) NOT NULL COMMENT '崩溃类型(error/exception/fatal/anr)',
    `message` TEXT COMMENT '错误消息',
    `stack_trace` LONGTEXT COMMENT '堆栈信息',
    `app_version` VARCHAR(50) COMMENT '应用版本',
    `app_build_number` VARCHAR(50) COMMENT '应用构建号',
    `device_model` VARCHAR(100) COMMENT '设备型号',
    `device_brand` VARCHAR(100) COMMENT '设备品牌',
    `os_version` VARCHAR(50) COMMENT '操作系统版本',
    `platform` VARCHAR(20) COMMENT '平台类型(ios/android)',
    `screen_resolution` VARCHAR(50) COMMENT '屏幕分辨率',
    `total_memory` INT COMMENT '总内存(MB)',
    `available_memory` INT COMMENT '可用内存(MB)',
    `network_type` VARCHAR(20) COMMENT '网络类型',
    `battery_level` INT COMMENT '电池电量百分比',
    `custom_data` JSON COMMENT '自定义业务数据',
    `crash_timestamp` DATETIME NOT NULL COMMENT '崩溃发生时间',
    `report_timestamp` DATETIME NOT NULL COMMENT '上报时间',
    `sdk_version` VARCHAR(50) COMMENT 'SDK版本',
    `deleted` TINYINT(1) DEFAULT 0 COMMENT '逻辑删除标记',
    `deleted_at` DATETIME COMMENT '删除时间',
    `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX `idx_crash_id` (`crash_id`),
    INDEX `idx_app_id` (`app_id`),
    INDEX `idx_user_id` (`user_id`),
    INDEX `idx_crash_type` (`crash_type`),
    INDEX `idx_crash_timestamp` (`crash_timestamp`),
    INDEX `idx_device_brand` (`device_brand`),
    INDEX `idx_app_version` (`app_version`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='崩溃报告表';
```

#### 6. store_db - 商店链接服务数据库

```sql
-- 数据库创建
CREATE DATABASE IF NOT EXISTS `store_db` 
    DEFAULT CHARACTER SET utf8mb4 
    DEFAULT COLLATE utf8mb4_unicode_ci;

USE `store_db`;

-- 商店链接配置表
CREATE TABLE IF NOT EXISTS `store_link_config` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `device_brand` VARCHAR(50) NOT NULL COMMENT '设备品牌',
    `store_name` VARCHAR(100) NOT NULL COMMENT '商店名称',
    `store_url` VARCHAR(500) NOT NULL COMMENT '商店链接',
    `package_name` VARCHAR(100) COMMENT '包名',
    `enabled` TINYINT(1) DEFAULT 1 COMMENT '是否启用(0:禁用 1:启用)',
    `sort_order` INT DEFAULT 0 COMMENT '排序顺序',
    `description` VARCHAR(255) COMMENT '描述',
    `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    `updated_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY `uk_device_brand` (`device_brand`),
    INDEX `idx_enabled` (`enabled`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商店链接配置表';
```

#### 7. perf_db - 绩效评估服务数据库

```sql
-- 数据库创建
CREATE DATABASE IF NOT EXISTS `perf_db` 
    DEFAULT CHARACTER SET utf8mb4 
    DEFAULT COLLATE utf8mb4_unicode_ci;

USE `perf_db`;

-- 绩效评估配置表
CREATE TABLE IF NOT EXISTS `performance_review` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `dept_id` VARCHAR(50) NOT NULL UNIQUE COMMENT '部门ID',
    `name` VARCHAR(100) NOT NULL COMMENT '组织名称',
    `cover_image` VARCHAR(500) COMMENT '封面图URL',
    `deadline` VARCHAR(50) COMMENT '截止时间',
    `description` TEXT COMMENT '描述',
    `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    `updated_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `created_by` VARCHAR(50) COMMENT '创建人',
    `updated_by` VARCHAR(50) COMMENT '更新人',
    INDEX `idx_dept_id` (`dept_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='绩效评估配置表';
```

#### 8. config_db - 配置服务数据库

```sql
-- 数据库创建
CREATE DATABASE IF NOT EXISTS `config_db` 
    DEFAULT CHARACTER SET utf8mb4 
    DEFAULT COLLATE utf8mb4_unicode_ci;

USE `config_db`;

-- 系统配置表
CREATE TABLE IF NOT EXISTS `sys_config` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `config_key` VARCHAR(100) NOT NULL UNIQUE COMMENT '配置键',
    `config_value` TEXT COMMENT '配置值',
    `value_type` VARCHAR(20) DEFAULT 'STRING' COMMENT '值类型(STRING/INT/BOOLEAN/JSON)',
    `description` VARCHAR(255) COMMENT '配置说明',
    `is_public` TINYINT(1) DEFAULT 0 COMMENT '是否公开(0:私有 1:公开)',
    `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    `updated_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `created_by` VARCHAR(50) COMMENT '创建人',
    `updated_by` VARCHAR(50) COMMENT '更新人',
    INDEX `idx_config_key` (`config_key`),
    INDEX `idx_is_public` (`is_public`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统配置表';

-- 初始化配置数据
INSERT INTO `sys_config` (`config_key`, `config_value`, `value_type`, `description`, `is_public`) VALUES
('app_update_total_enabled', 'true', 'BOOLEAN', 'APP更新总开关', 1),
('event_tracking_enabled', 'true', 'BOOLEAN', '埋点上报开关', 1),
('log_request_timeout_minutes', '120', 'INT', '日志请求超时时间(分钟)', 0);
```

### 1.3 数据库连接配置示例

```yaml
# 各服务的 application.yml 数据库配置示例

# auth-service
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/auth_db?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai
    username: ${DB_USERNAME:root}
    password: ${DB_PASSWORD:password}
    driver-class-name: com.mysql.cj.jdbc.Driver

# app-service
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/app_db?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai
    username: ${DB_USERNAME:root}
    password: ${DB_PASSWORD:password}
    driver-class-name: com.mysql.cj.jdbc.Driver

# 其他服务类似...
```

---

## 二、新增技术栈列表

### 2.1 技术栈概览

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                         微服务技术栈架构图                                    │
├─────────────────────────────────────────────────────────────────────────────┤
│                                                                             │
│  ┌─────────────────────────────────────────────────────────────────────┐   │
│  │                         接入层 (Access Layer)                        │   │
│  │  ┌───────────────┐  ┌───────────────┐  ┌───────────────┐           │   │
│  │  │  Nginx        │  │  Spring Cloud │  │  Vue 3        │           │   │
│  │  │  (负载均衡)    │  │  Gateway      │  │  (前端)       │           │   │
│  │  └───────────────┘  └───────────────┘  └───────────────┘           │   │
│  └─────────────────────────────────────────────────────────────────────┘   │
│                                    │                                        │
│  ┌─────────────────────────────────────────────────────────────────────┐   │
│  │                      服务治理层 (Service Governance)                  │   │
│  │  ┌───────────────┐  ┌───────────────┐  ┌───────────────┐           │   │
│  │  │  Nacos        │  │  Sentinel     │  │  SkyWalking   │           │   │
│  │  │  (注册/配置)   │  │  (熔断限流)    │  │  (链路追踪)    │           │   │
│  │  └───────────────┘  └───────────────┘  └───────────────┘           │   │
│  └─────────────────────────────────────────────────────────────────────┘   │
│                                    │                                        │
│  ┌─────────────────────────────────────────────────────────────────────┐   │
│  │                        服务层 (Service Layer)                        │   │
│  │  ┌───────────────┐  ┌───────────────┐  ┌───────────────┐           │   │
│  │  │  Spring Boot  │  │  OpenFeign    │  │  Spring Security         │   │
│  │  │  3.5.7        │  │  (服务调用)    │  │  (安全认证)    │           │   │
│  │  └───────────────┘  └───────────────┘  └───────────────┘           │   │
│  └─────────────────────────────────────────────────────────────────────┘   │
│                                    │                                        │
│  ┌─────────────────────────────────────────────────────────────────────┐   │
│  │                        数据层 (Data Layer)                           │   │
│  │  ┌───────────────┐  ┌───────────────┐  ┌───────────────┐           │   │
│  │  │  MySQL 8.0    │  │  Redis        │  │  RabbitMQ     │           │   │
│  │  │  (主数据库)    │  │  (缓存)       │  │  (消息队列)    │           │   │
│  │  └───────────────┘  └───────────────┘  └───────────────┘           │   │
│  │  ┌───────────────┐  ┌───────────────┐                              │   │
│  │  │  MinIO        │  │  MyBatis Plus │                              │   │
│  │  │  (对象存储)    │  │  (ORM框架)    │                              │   │
│  │  └───────────────┘  └───────────────┘                              │   │
│  └─────────────────────────────────────────────────────────────────────┘   │
│                                    │                                        │
│  ┌─────────────────────────────────────────────────────────────────────┐   │
│  │                      监控运维层 (Observability)                       │   │
│  │  ┌───────────────┐  ┌───────────────┐  ┌───────────────┐           │   │
│  │  │  Prometheus   │  │  Grafana      │  │  ELK Stack    │           │   │
│  │  │  (指标采集)    │  │  (可视化)      │  │  (日志收集)    │           │   │
│  │  └───────────────┘  └───────────────┘  └───────────────┘           │   │
│  └─────────────────────────────────────────────────────────────────────┘   │
│                                    │                                        │
│  ┌─────────────────────────────────────────────────────────────────────┐   │
│  │                      容器化层 (Container Layer)                       │   │
│  │  ┌───────────────┐  ┌───────────────┐  ┌───────────────┐           │   │
│  │  │  Docker       │  │  Docker       │  │  Kubernetes   │           │   │
│  │  │  (容器化)      │  │  Compose      │  │  (编排调度)    │           │   │
│  │  └───────────────┘  └───────────────┘  └───────────────┘           │   │
│  └─────────────────────────────────────────────────────────────────────┘   │
│                                                                             │
└─────────────────────────────────────────────────────────────────────────────┘
```

### 2.2 新增技术组件清单

#### 1. 服务治理与注册发现

| 技术 | 版本 | 用途 | 引入方式 |
|------|------|------|----------|
| **Nacos** | 2.3.0+ | 服务注册发现、配置中心 | Spring Cloud Alibaba |
| **Spring Cloud Gateway** | 4.1.0+ | API 网关、路由转发 | Spring Cloud |
| **OpenFeign** | 4.1.0+ | 声明式 HTTP 客户端 | Spring Cloud |
| **Spring Cloud LoadBalancer** | 4.1.0+ | 客户端负载均衡 | Spring Cloud |

**Maven 依赖:**
```xml
<!-- Spring Cloud Alibaba Nacos -->
<dependency>
    <groupId>com.alibaba.cloud</groupId>
    <artifactId>spring-cloud-starter-alibaba-nacos-discovery</artifactId>
</dependency>
<dependency>
    <groupId>com.alibaba.cloud</groupId>
    <artifactId>spring-cloud-starter-alibaba-nacos-config</artifactId>
</dependency>

<!-- Spring Cloud Gateway -->
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-gateway</artifactId>
</dependency>

<!-- OpenFeign -->
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-openfeign</artifactId>
</dependency>
```

#### 2. 熔断限流与容错

| 技术 | 版本 | 用途 | 引入方式 |
|------|------|------|----------|
| **Sentinel** | 1.8.8+ | 流量控制、熔断降级、系统保护 | Spring Cloud Alibaba |
| **Resilience4j** | 2.1.0+ | 容错处理(备选) | 直接依赖 |

**Maven 依赖:**
```xml
<!-- Sentinel -->
<dependency>
    <groupId>com.alibaba.cloud</groupId>
    <artifactId>spring-cloud-starter-alibaba-sentinel</artifactId>
</dependency>
<!-- Sentinel 网关适配 -->
<dependency>
    <groupId>com.alibaba.cloud</groupId>
    <artifactId>spring-cloud-alibaba-sentinel-gateway</artifactId>
</dependency>
```

#### 3. 监控与可观测性

| 技术 | 版本 | 用途 | 引入方式 |
|------|------|------|----------|
| **Micrometer** | 1.12.0+ | 指标采集(集成 Prometheus) | Spring Boot Actuator |
| **Prometheus** | 2.50.0+ | 时序数据库、指标存储 | 独立部署 |
| **Grafana** | 10.3.0+ | 监控可视化面板 | 独立部署 |
| **SkyWalking** | 9.7.0+ | 分布式链路追踪 | Agent 方式 |
| **ELK Stack** | 8.12.0+ | 日志收集与分析 | 独立部署 |

**Maven 依赖:**
```xml
<!-- Actuator + Micrometer -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-actuator</artifactId>
</dependency>
<dependency>
    <groupId>io.micrometer</groupId>
    <artifactId>micrometer-registry-prometheus</artifactId>
</dependency>

<!-- SkyWalking Agent (无需 Maven 依赖，启动时附加) -->
<!-- -javaagent:/path/to/skywalking-agent.jar -Dskywalking.agent.service_name=xxx -->
```

#### 4. 容器化与部署

| 技术 | 版本 | 用途 | 说明 |
|------|------|------|------|
| **Docker** | 25.0+ | 应用容器化 | 构建镜像 |
| **Docker Compose** | 2.24+ | 本地开发环境编排 | 多服务启动 |
| **Kubernetes** | 1.29+ | 生产环境容器编排(可选) | 云原生部署 |

**Dockerfile 示例:**
```dockerfile
# 多阶段构建
FROM eclipse-temurin:17-jdk-alpine AS builder
WORKDIR /app
COPY . .
RUN ./mvnw clean package -DskipTests

FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY --from=builder /app/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
```

#### 5. 数据访问增强

| 技术 | 版本 | 用途 | 引入方式 |
|------|------|------|----------|
| **MyBatis Plus** | 3.5.5+ | 增强 ORM 框架(可选升级) | 直接依赖 |
| **ShardingSphere** | 5.5.0+ | 分库分表(未来扩展) | 直接依赖 |
| **Redisson** | 3.26.0+ | 分布式锁、高级 Redis 操作 | 直接依赖 |

**Maven 依赖:**
```xml
<!-- MyBatis Plus -->
<dependency>
    <groupId>com.baomidou</groupId>
    <artifactId>mybatis-plus-boot-starter</artifactId>
    <version>3.5.5</version>
</dependency>

<!-- Redisson -->
<dependency>
    <groupId>org.redisson</groupId>
    <artifactId>redisson-spring-boot-starter</artifactId>
    <version>3.26.0</version>
</dependency>
```

#### 6. 安全与认证增强

| 技术 | 版本 | 用途 | 引入方式 |
|------|------|------|----------|
| **OAuth2 Resource Server** | 3.2.0+ | JWT 令牌验证 | Spring Security |
| **Spring Authorization Server** | 1.3.0+ | OAuth2 授权服务器(可选) | 直接依赖 |

**Maven 依赖:**
```xml
<!-- OAuth2 Resource Server -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-oauth2-resource-server</artifactId>
</dependency>
```

#### 7. 文档与测试

| 技术 | 版本 | 用途 | 引入方式 |
|------|------|------|----------|
| **SpringDoc OpenAPI** | 2.3.0+ | API 文档(已有，保持) | 直接依赖 |
| **Spring Cloud Contract** | 4.1.0+ | 契约测试 | Spring Cloud |
| **Testcontainers** | 1.19.0+ | 集成测试容器化 | 直接依赖 |

**Maven 依赖:**
```xml
<!-- Spring Cloud Contract -->
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-contract-verifier</artifactId>
    <scope>test</scope>
</dependency>

<!-- Testcontainers -->
<dependency>
    <groupId>org.testcontainers</groupId>
    <artifactId>testcontainers</artifactId>
    <scope>test</scope>
</dependency>
<dependency>
    <groupId>org.testcontainers</groupId>
    <artifactId>mysql</artifactId>
    <scope>test</scope>
</dependency>
```

### 2.3 完整依赖管理 POM

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 
                             http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>
    
    <groupId>com.app</groupId>
    <artifactId>app-platform-parent</artifactId>
    <version>1.0.0-SNAPSHOT</version>
    <packaging>pom</packaging>
    
    <name>AppPlatform Microservices Parent</name>
    <description>AppPlatform 微服务父 POM</description>
    
    <parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>3.5.7</version>
        <relativePath/>
    </parent>
    
    <modules>
        <module>app-platform-common</module>
        <module>app-platform-gateway</module>
        <module>auth-service</module>
        <module>app-service</module>
        <module>log-service</module>
        <module>event-service</module>
        <module>crash-service</module>
        <module>file-service</module>
        <module>store-service</module>
        <module>perf-service</module>
    </modules>
    
    <properties>
        <java.version>17</java.version>
        <maven.compiler.source>17</maven.compiler.source>
        <maven.compiler.target>17</maven.compiler.target>
        <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
        
        <!-- Spring Cloud 版本 -->
        <spring-cloud.version>2023.0.0</spring-cloud.version>
        
        <!-- Spring Cloud Alibaba 版本 -->
        <spring-cloud-alibaba.version>2023.0.0.0</spring-cloud-alibaba.version>
        
        <!-- 第三方组件版本 -->
        <mybatis-plus.version>3.5.5</mybatis-plus.version>
        <redisson.version>3.26.0</redisson.version>
        <jjwt.version>0.12.3</jjwt.version>
        <minio.version>8.5.15</minio.version>
        <springdoc.version>2.3.0</springdoc.version>
        <testcontainers.version>1.19.3</testcontainers.version>
    </properties>
    
    <dependencyManagement>
        <dependencies>
            <!-- Spring Cloud BOM -->
            <dependency>
                <groupId>org.springframework.cloud</groupId>
                <artifactId>spring-cloud-dependencies</artifactId>
                <version>${spring-cloud.version}</version>
                <type>pom</type>
                <scope>import</scope>
            </dependency>
            
            <!-- Spring Cloud Alibaba BOM -->
            <dependency>
                <groupId>com.alibaba.cloud</groupId>
                <artifactId>spring-cloud-alibaba-dependencies</artifactId>
                <version>${spring-cloud-alibaba.version}</version>
                <type>pom</type>
                <scope>import</scope>
            </dependency>
            
            <!-- Testcontainers BOM -->
            <dependency>
                <groupId>org.testcontainers</groupId>
                <artifactId>testcontainers-bom</artifactId>
                <version>${testcontainers.version}</version>
                <type>pom</type>
                <scope>import</scope>
            </dependency>
            
            <!-- MyBatis Plus -->
            <dependency>
                <groupId>com.baomidou</groupId>
                <artifactId>mybatis-plus-boot-starter</artifactId>
                <version>${mybatis-plus.version}</version>
            </dependency>
            
            <!-- Redisson -->
            <dependency>
                <groupId>org.redisson</groupId>
                <artifactId>redisson-spring-boot-starter</artifactId>
                <version>${redisson.version}</version>
            </dependency>
            
            <!-- MinIO -->
            <dependency>
                <groupId>io.minio</groupId>
                <artifactId>minio</artifactId>
                <version>${minio.version}</version>
            </dependency>
            
            <!-- SpringDoc OpenAPI -->
            <dependency>
                <groupId>org.springdoc</groupId>
                <artifactId>springdoc-openapi-starter-webmvc-ui</artifactId>
                <version>${springdoc.version}</version>
            </dependency>
            
            <!-- JJWT -->
            <dependency>
                <groupId>io.jsonwebtoken</groupId>
                <artifactId>jjwt-api</artifactId>
                <version>${jjwt.version}</version>
            </dependency>
            <dependency>
                <groupId>io.jsonwebtoken</groupId>
                <artifactId>jjwt-impl</artifactId>
                <version>${jjwt.version}</version>
            </dependency>
            <dependency>
                <groupId>io.jsonwebtoken</groupId>
                <artifactId>jjwt-jackson</artifactId>
                <version>${jjwt.version}</version>
            </dependency>
        </dependencies>
    </dependencyManagement>
    
    <build>
        <pluginManagement>
            <plugins>
                <plugin>
                    <groupId>org.springframework.boot</groupId>
                    <artifactId>spring-boot-maven-plugin</artifactId>
                    <configuration>
                        <excludes>
                            <exclude>
                                <groupId>org.projectlombok</groupId>
                                <artifactId>lombok</artifactId>
                            </exclude>
                        </excludes>
                    </configuration>
                </plugin>
            </plugins>
        </pluginManagement>
    </build>
    
    <repositories>
        <repository>
            <id>spring-milestones</id>
            <name>Spring Milestones</name>
            <url>https://repo.spring.io/milestone</url>
            <snapshots>
                <enabled>false</enabled>
            </snapshots>
        </repository>
    </repositories>
</project>
```

### 2.4 基础设施部署清单

#### Docker Compose 基础设施配置

```yaml
# infrastructure/docker-compose.yml
version: '3.8'

services:
  # MySQL 数据库
  mysql:
    image: mysql:8.0.36
    container_name: app-platform-mysql
    environment:
      MYSQL_ROOT_PASSWORD: rootpassword
      MYSQL_DATABASE: app_platform
    ports:
      - "3306:3306"
    volumes:
      - mysql_data:/var/lib/mysql
      - ./init-scripts:/docker-entrypoint-initdb.d
    command: --default-authentication-plugin=mysql_native_password
    networks:
      - app-platform-network

  # Redis 缓存
  redis:
    image: redis:7.2-alpine
    container_name: app-platform-redis
    ports:
      - "6379:6379"
    volumes:
      - redis_data:/data
    networks:
      - app-platform-network

  # RabbitMQ 消息队列
  rabbitmq:
    image: rabbitmq:3.13-management-alpine
    container_name: app-platform-rabbitmq
    environment:
      RABBITMQ_DEFAULT_USER: admin
      RABBITMQ_DEFAULT_PASS: admin
    ports:
      - "5672:5672"
      - "15672:15672"
    volumes:
      - rabbitmq_data:/var/lib/rabbitmq
    networks:
      - app-platform-network

  # Nacos 注册中心/配置中心
  nacos:
    image: nacos/nacos-server:v2.3.0
    container_name: app-platform-nacos
    environment:
      MODE: standalone
      SPRING_DATASOURCE_PLATFORM: mysql
      MYSQL_SERVICE_HOST: mysql
      MYSQL_SERVICE_PORT: 3306
      MYSQL_SERVICE_DB_NAME: nacos
      MYSQL_SERVICE_USER: root
      MYSQL_SERVICE_PASSWORD: rootpassword
      NACOS_AUTH_ENABLE: true
    ports:
      - "8848:8848"
      - "9848:9848"
    depends_on:
      - mysql
    networks:
      - app-platform-network

  # MinIO 对象存储
  minio:
    image: minio/minio:RELEASE.2024-02-17T01-15-57Z
    container_name: app-platform-minio
    command: server /data --console-address ":9001"
    environment:
      MINIO_ROOT_USER: minio
      MINIO_ROOT_PASSWORD: minio123
    ports:
      - "9000:9000"
      - "9001:9001"
    volumes:
      - minio_data:/data
    networks:
      - app-platform-network

  # Prometheus 监控
  prometheus:
    image: prom/prometheus:v2.50.0
    container_name: app-platform-prometheus
    ports:
      - "9090:9090"
    volumes:
      - ./prometheus.yml:/etc/prometheus/prometheus.yml
      - prometheus_data:/prometheus
    networks:
      - app-platform-network

  # Grafana 可视化
  grafana:
    image: grafana/grafana:10.3.1
    container_name: app-platform-grafana
    ports:
      - "3000:3000"
    environment:
      GF_SECURITY_ADMIN_PASSWORD: admin
    volumes:
      - grafana_data:/var/lib/grafana
    networks:
      - app-platform-network

volumes:
  mysql_data:
  redis_data:
  rabbitmq_data:
  minio_data:
  prometheus_data:
  grafana_data:

networks:
  app-platform-network:
    driver: bridge
```

### 2.5 技术栈迁移对比

| 层级 | 原有技术 | 新增/替换技术 | 说明 |
|------|----------|---------------|------|
| **网关层** | 无 | Spring Cloud Gateway | 新增统一入口 |
| **注册发现** | 无 | Nacos | 新增服务治理 |
| **配置中心** | 本地配置文件 | Nacos Config | 集中化配置管理 |
| **服务调用** | 直接 HTTP | OpenFeign + LoadBalancer | 声明式调用 |
| **熔断限流** | 无 | Sentinel | 新增流量控制 |
| **监控** | Spring Boot Admin | Prometheus + Grafana + SkyWalking | 更专业的监控方案 |
| **容器化** | 无 | Docker + Docker Compose | 标准化部署 |
| **ORM** | MyBatis | MyBatis (保持) / MyBatis Plus (可选) | 可升级 |
| **缓存** | Spring Data Redis | Spring Data Redis / Redisson | 可升级分布式锁 |

---

## 三、总结

### 3.1 数据库清单汇总

| 序号 | 数据库名称 | 所属服务 | 数据表数量 | 主要表 |
|------|-----------|----------|-----------|--------|
| 1 | auth_db | auth-service | 1 | user |
| 2 | app_db | app-service | 5 | app_info, app_module, dynamic_config, dynamic_config_history, sys_config |
| 3 | log_db | log-service | 2 | log_info, log_request |
| 4 | event_db | event-service | 1 | app_event |
| 5 | crash_db | crash-service | 1 | crash_reports |
| 6 | store_db | store-service | 1 | store_link_config |
| 7 | perf_db | perf-service | 1 | performance_review |
| 8 | config_db | config-service | 1 | sys_config |

**总计**: 8 个数据库，13 张业务表

### 3.2 新增技术栈汇总

| 类别 | 技术组件 | 数量 |
|------|----------|------|
| **服务治理** | Nacos, Spring Cloud Gateway, OpenFeign, LoadBalancer | 4 |
| **容错保护** | Sentinel | 1 |
| **监控观测** | Prometheus, Grafana, SkyWalking, ELK | 4 |
| **容器化** | Docker, Docker Compose, Kubernetes(可选) | 2-3 |
| **数据访问** | MyBatis Plus(可选), Redisson(可选) | 0-2 |
| **测试** | Spring Cloud Contract, Testcontainers | 2 |

**核心新增**: 11 个技术组件

### 3.3 实施建议

1. **数据库迁移**: 使用 Flyway 或 Liquibase 管理数据库版本
2. **逐步引入**: 先引入 Nacos + Gateway，再逐步拆分服务
3. **监控先行**: 在拆分前先搭建好监控体系
4. **灰度发布**: 使用 Nacos 的权重配置实现灰度
