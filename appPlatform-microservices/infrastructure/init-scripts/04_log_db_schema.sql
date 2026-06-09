-- log_db 数据库表结构
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
