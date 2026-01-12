use app_platform;

DROP TABLE IF EXISTS dynamic_config;
CREATE TABLE IF NOT EXISTS dynamic_config (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    version_range VARCHAR(100) NOT NULL COMMENT '版本范围或具体版本，如 1.0.0-2.0.0 或 1.5.0',
    file_url VARCHAR(500) NOT NULL COMMENT 'MinIO中的文件保存地址',
    env VARCHAR(20) DEFAULT 'prod' COMMENT '环境类型：prod(生产), test(测试)',
    remark VARCHAR(500) COMMENT '备注',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'
) COMMENT='动态配置文件元数据表';
