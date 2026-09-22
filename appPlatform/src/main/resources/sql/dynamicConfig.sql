use app_platform;

DROP TABLE IF EXISTS dynamic_config;
CREATE TABLE IF NOT EXISTS dynamic_config (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    version_range VARCHAR(100) NOT NULL COMMENT '版本范围或具体版本，如 1.0.0-2.0.0 或 1.5.0',
    env VARCHAR(20) DEFAULT 'prod' COMMENT '环境类型：prod(生产), test(测试)',
    remark VARCHAR(500) COMMENT '备注',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY uk_env_version_range (env, version_range)
) COMMENT='动态配置表（一个环境+版本范围唯一对应一份配置）';
