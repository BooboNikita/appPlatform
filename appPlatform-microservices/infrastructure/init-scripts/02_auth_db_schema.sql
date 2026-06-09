-- auth_db 数据库表结构
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

-- 初始化管理员账号 (密码: admin123)
INSERT INTO `user` (`username`, `password`, `role`, `avatar`)
VALUES ('admin', '$2a$10$RKQHFEWWtI0Ch9qrlCCNtOCqj3B1IzWRQCmMl/RsVaIC1v026uMtm', 'ADMIN',
        'http://172.31.101.166:8008/static/png/person-img-BRBJlwLp.png')
ON DUPLICATE KEY UPDATE `username` = `username`;
