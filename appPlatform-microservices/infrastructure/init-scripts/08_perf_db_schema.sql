-- perf_db 数据库表结构
USE `perf_db`;

-- 绩效评估配置表
CREATE TABLE IF NOT EXISTS `performance_review` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `dept_id` VARCHAR(50) NOT NULL UNIQUE COMMENT '部门ID',
    `name` VARCHAR(100) NOT NULL COMMENT '组织名称',
    `cover_image` VARCHAR(500) COMMENT '封面图URL',
    `deadline` VARCHAR(50) COMMENT '截止时间',
    `description` TEXT COMMENT '描述',
    `create_time` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    `update_time` TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `created_by` VARCHAR(50) COMMENT '创建人',
    `updated_by` VARCHAR(50) COMMENT '更新人',
    INDEX `idx_dept_id` (`dept_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='绩效评估配置表';
