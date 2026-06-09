-- crash_db 数据库表结构
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
