use app_platform;

drop table if exists app_event;
create table if not exists app_event
(
    id                bigint primary key auto_increment,
    app_ver           varchar(255) not null COMMENT 'app版本',
    app_buildNum      varchar(255) not null COMMENT 'app构建号',
    user_id           varchar(255) not null COMMENT '用户ID',
    user_name         varchar(255) not null COMMENT '用户名称',
    event_id          varchar(255) COMMENT '事件ID',
    event_type        varchar(50) COMMENT '事件类型(view/click/exposure)',
    event_time        datetime,
    recv_time         datetime     not null,
    page_url          varchar(500) COMMENT '页面URL',
    referrer          varchar(500) COMMENT '来源页面',
    session_id        varchar(255) COMMENT '会话ID',
    os                varchar(50) COMMENT '操作系统',
    os_ver            varchar(50) COMMENT '操作系统版本',
    device_id         varchar(255) COMMENT '设备ID',
    device_model      varchar(100) COMMENT '设备型号',
    device_brand      varchar(100) COMMENT '设备品牌',
    device_ip         varchar(50) COMMENT '设备IP',
    network_type      varchar(20) COMMENT '网络类型',
    screen_resolution varchar(50) COMMENT '屏幕分辨率',
    extra             json,
    status            tinyint DEFAULT 0 COMMENT '状态(0:正常, 1:测试)'
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT = '事件日志表';