-- event_db 数据库表结构
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
