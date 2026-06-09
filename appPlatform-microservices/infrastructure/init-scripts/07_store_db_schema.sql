-- store_db 数据库表结构
USE `store_db`;

-- 商店链接配置表
CREATE TABLE IF NOT EXISTS `store_link_config` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `device_brand` VARCHAR(50) NOT NULL COMMENT '设备品牌',
    `brand_aliases` VARCHAR(500) DEFAULT NULL COMMENT '品牌别名（多个别名用逗号分隔，如：redmi,mi,小米）',
    `link_template` VARCHAR(500) NOT NULL COMMENT '应用商店链接模板（如：market://details?id={packageName}）',
    `enabled` TINYINT(1) DEFAULT 1 COMMENT '是否启用(0:禁用 1:启用)',
    `sort_order` INT DEFAULT 0 COMMENT '排序顺序',
    `remark` VARCHAR(200) DEFAULT NULL COMMENT '备注',
    `create_time` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    `update_time` TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY `uk_device_brand` (`device_brand`),
    INDEX `idx_enabled` (`enabled`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商店链接配置表';
