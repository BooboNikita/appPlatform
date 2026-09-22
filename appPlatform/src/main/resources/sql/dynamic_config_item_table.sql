-- =============================================================
-- 动态配置「配置项 + 灰度下发」改造
-- 1. 三张新表：配置项 / 灰度名单 / 配置项级历史
-- 2. 存量库升级语句（全新库执行 dynamicConfig.sql 与
--    dynamic_config_history_table.sql 时可跳过第 2 部分）
-- =============================================================
use app_platform;

-- -------------------------------------------------------------
-- 1. 新表
-- -------------------------------------------------------------

CREATE TABLE IF NOT EXISTS `dynamic_config_item` (
  `id`           bigint       NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `config_id`    bigint       NOT NULL COMMENT '所属配置ID(dynamic_config.id)',
  `version_range`varchar(100) NOT NULL COMMENT '版本范围，如 1.0.0-2.0.0 / 1.5.0 / *',
  `env`          varchar(50)  NOT NULL DEFAULT 'prod' COMMENT '环境：prod(生产)/test(测试)',
  `domain`       varchar(100) NOT NULL COMMENT '所属域，如 homepage/detail/global',
  `item_key`     varchar(200) NOT NULL COMMENT '配置项 key，如 ai_entry',
  `item_value`   text         NOT NULL COMMENT '配置项值(JSON 序列化文本，保留原始类型)',
  `value_type`   varchar(20)  NOT NULL COMMENT '值类型：BOOLEAN/NUMBER/STRING/JSON',
  `enabled`      tinyint      NOT NULL DEFAULT 1 COMMENT '是否启用：1启用(下发)，0停用(不下发)',
  `gray_enabled` tinyint      NOT NULL DEFAULT 0 COMMENT '是否开启灰度：1开启，0关闭(全量下发)',
  `gray_percent` int          NOT NULL DEFAULT 0 COMMENT '灰度百分比 0-100',
  `remark`       varchar(500) DEFAULT NULL COMMENT '备注',
  `create_time`  datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time`  datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_config_domain_key` (`config_id`,`domain`,`item_key`),
  KEY `idx_env` (`env`),
  KEY `idx_version_range` (`version_range`),
  KEY `idx_domain` (`domain`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='动态配置项表';

CREATE TABLE IF NOT EXISTS `dynamic_config_item_gray_user` (
  `id`          bigint       NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `item_id`     bigint       NOT NULL COMMENT '配置项ID(dynamic_config_item.id)',
  `list_type`   varchar(10)  NOT NULL COMMENT '名单类型：WHITE(白名单)/BLACK(黑名单)',
  `username`    varchar(100) NOT NULL COMMENT '用户名',
  `operator`    varchar(100) NOT NULL DEFAULT 'system' COMMENT '操作人',
  `create_time` datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_item_type_user` (`item_id`,`list_type`,`username`),
  KEY `idx_item_type` (`item_id`,`list_type`),
  KEY `idx_username` (`username`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='动态配置项灰度名单表';

CREATE TABLE IF NOT EXISTS `dynamic_config_item_history` (
  `id`              bigint       NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `config_id`       bigint       NOT NULL COMMENT '所属配置ID(dynamic_config.id)',
  `domain`          varchar(100) NOT NULL COMMENT '所属域',
  `item_key`        varchar(200) NOT NULL COMMENT '配置项 key',
  `version_range`   varchar(100) NOT NULL COMMENT '版本范围(冗余，便于展示)',
  `env`             varchar(50)  NOT NULL COMMENT '环境(冗余，便于展示)',
  `item_value`      text         NOT NULL COMMENT '本次变更后的值(JSON 序列化文本)',
  `value_type`      varchar(20)  NOT NULL COMMENT '值类型：BOOLEAN/NUMBER/STRING/JSON',
  `enabled`         tinyint      NOT NULL DEFAULT 1 COMMENT '本次变更后是否启用',
  `gray_enabled`    tinyint      NOT NULL DEFAULT 0 COMMENT '本次变更后是否开启灰度',
  `gray_percent`    int          NOT NULL DEFAULT 0 COMMENT '本次变更后的灰度百分比',
  `gray_users_json` text         COMMENT '名单快照 JSON 数组，如 [{"listType":"WHITE","username":"u1"}]',
  `remark`          varchar(500) DEFAULT NULL COMMENT '备注',
  `operation_type`  varchar(20)  NOT NULL COMMENT '操作类型：CREATE/UPDATE/DELETE/REVERT',
  `operator`        varchar(100) NOT NULL DEFAULT 'system' COMMENT '操作人',
  `create_time`     datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_config_domain_key` (`config_id`,`domain`,`item_key`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='动态配置项历史表';

-- -------------------------------------------------------------
-- 2. 存量库升级
--    注意：加唯一键前必须先清理重复的 (env, version_range)，
--    以下保留每组重复中 id 最大的一条。
-- -------------------------------------------------------------
-- DELETE c1 FROM dynamic_config c1
-- INNER JOIN dynamic_config c2
--   ON c1.env = c2.env AND c1.version_range = c2.version_range AND c1.id < c2.id;

ALTER TABLE dynamic_config DROP COLUMN file_url;
ALTER TABLE dynamic_config ADD UNIQUE KEY uk_env_version_range (env, version_range);

ALTER TABLE dynamic_config_history DROP COLUMN file_url;
ALTER TABLE dynamic_config_history
  ADD COLUMN snapshot_json LONGTEXT COMMENT '整包快照JSON(含 items 与灰度名单)' AFTER version_range;
