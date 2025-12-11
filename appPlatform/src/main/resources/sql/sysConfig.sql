use app_platform;

CREATE TABLE IF NOT EXISTS sys_config (
                                          id BIGINT PRIMARY KEY AUTO_INCREMENT,
                                          config_key VARCHAR(100) NOT NULL UNIQUE COMMENT '配置键',
    config_value VARCHAR(500) COMMENT '配置值',
    remark VARCHAR(500) COMMENT '备注',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'
    ) COMMENT='系统配置表';

-- 初始化配置
INSERT IGNORE INTO sys_config(config_key, config_value, remark)
VALUES ('event_tracking_enabled', 'true', '是否开启埋点上报');