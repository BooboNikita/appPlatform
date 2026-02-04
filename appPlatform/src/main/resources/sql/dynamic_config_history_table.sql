-- 创建动态配置历史版本表
use app_platform;
CREATE TABLE `dynamic_config_history` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `config_id` bigint(20) NOT NULL COMMENT '原配置ID',
  `version_range` varchar(255) NOT NULL COMMENT '版本范围或具体版本',
  `file_url` varchar(500) NOT NULL COMMENT 'MinIO中的文件保存地址',
  `env` varchar(50) NOT NULL COMMENT '环境类型：prod(生产), test(测试)',
  `remark` text COMMENT '备注',
  `operation_type` varchar(20) NOT NULL COMMENT '操作类型：CREATE(创建), UPDATE(更新), DELETE(删除)',
  `operator` varchar(100) NOT NULL DEFAULT 'system' COMMENT '操作人',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_config_id` (`config_id`),
  KEY `idx_env` (`env`),
  KEY `idx_create_time` (`create_time`),
  KEY `idx_operation_type` (`operation_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='动态配置历史版本表';
