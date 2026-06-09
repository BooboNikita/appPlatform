-- config_db 数据库表结构
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
('log_request_timeout_minutes', '120', 'INT', '日志请求超时时间(分钟)', 0)
ON DUPLICATE KEY UPDATE `config_key` = `config_key`;
