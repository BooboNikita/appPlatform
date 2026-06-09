-- app_db 数据库表结构
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
    `title` VARCHAR(100) NOT NULL COMMENT '模块标题',
    `icon_url` VARCHAR(500) COMMENT '图标地址',
    `target_url` VARCHAR(500) COMMENT '目标URL',
    `port` INT COMMENT '端口号',
    `color` VARCHAR(20) COMMENT '颜色',
    `route` VARCHAR(100) COMMENT '路由路径',
    `sort_order` INT DEFAULT 0 COMMENT '排序顺序',
    `is_active` BOOLEAN NOT NULL DEFAULT TRUE COMMENT '是否启用',
    `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    `updated_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `hide_for_test` BOOLEAN NOT NULL DEFAULT FALSE COMMENT '测试环境隐藏',
    INDEX `idx_is_active` (`is_active`),
    INDEX `idx_sort_order` (`sort_order`)
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

-- 初始化系统配置
INSERT INTO `sys_config` (`config_key`, `config_value`, `description`) VALUES
('app_update_total_enabled', 'true', 'APP更新总开关')
ON DUPLICATE KEY UPDATE `config_key` = `config_key`;
