use app_platform;

drop table if exists app_module;
CREATE TABLE if not exists app_module(
                              `id` bigint NOT NULL AUTO_INCREMENT,
                              `title` varchar(50) NOT NULL COMMENT '模块标题',
                              `iconUrl` varchar(255) NOT NULL COMMENT '图标URL',
                              `route` VARCHAR(255) NOT NULL COMMENT '路由类型：under_development-开发中, inner-内部页面, webview-网页',
                              `color` varchar(20) COMMENT '模块颜色',
                              `targetUrl` varchar(255) NOT NULL COMMENT '跳转路径',
                              `port` int DEFAULT NULL COMMENT '目标URL端口',
                              `sortOrder` int DEFAULT '0' COMMENT '排序字段，数字越小越靠前',
                              `isActive` tinyint(1) DEFAULT '1' COMMENT '是否启用',
                              `createdAt` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                              `updatedAt` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                              `hideForTest` boolean DEFAULT false COMMENT '是否对测试账号隐藏',
                              PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='App模块入口配置表';

ALTER TABLE app_module ADD COLUMN route VARCHAR(255) NOT NULL COMMENT '路由类型：under_development-开发中, inner-内部页面, webview-网页',
    ADD COLUMN color VARCHAR(20) COMMENT '模块颜色';

ALTER TABLE app_module ADD  COLUMN  port int DEFAULT NULL COMMENT '目标URL端口';

ALTER TABLE app_module
    MODIFY COLUMN color VARCHAR(255) COMMENT '模块颜色';

ALTER TABLE app_module
    ADD COLUMN hideForTest boolean DEFAULT false COMMENT '是否对测试账号隐藏';

ALTER TABLE app_module
    MODIFY COLUMN  id bigint NOT NULL AUTO_INCREMENT;
