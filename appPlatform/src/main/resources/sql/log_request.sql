use app_platform;

-- 对已存在的表添加软删除字段（用于已有表结构的场景）
ALTER TABLE log_request ADD COLUMN is_deleted tinyint DEFAULT 0 comment '是否删除：0-未删除 1-已删除' AFTER status;
ALTER TABLE log_request ADD INDEX idx_is_deleted (is_deleted);

-- 完整的建表语句（用于新环境）
drop table if exists log_request;
create table log_request (
    id int primary key auto_increment,
    username varchar(255) not null comment '请求日志的用户名',
    request_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP comment '请求时间',
    expire_time TIMESTAMP not null comment '过期时间',
    status tinyint DEFAULT 0 comment '状态：0-待上传 1-已上传 2-已过期',
    is_deleted tinyint DEFAULT 0 comment '是否删除：0-未删除 1-已删除',
    INDEX idx_username (username),
    INDEX idx_status (status),
    INDEX idx_expire_time (expire_time),
    INDEX idx_is_deleted (is_deleted)
) comment='日志请求记录表';
