-- 应用商店链接配置表
use app_platform;
DROP TABLE IF EXISTS store_link_config;

CREATE TABLE IF NOT EXISTS store_link_config (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    device_brand VARCHAR(50) NOT NULL UNIQUE COMMENT '设备品牌（如：xiaomi、huawei、honor、oppo等）',
    brand_aliases VARCHAR(500) DEFAULT NULL COMMENT '品牌别名（多个别名用逗号分隔，如：redmi,mi,小米）',
    link_template VARCHAR(500) NOT NULL COMMENT '应用商店链接模板（如：market://details?id={packageName}）',
    enabled TINYINT DEFAULT 1 COMMENT '是否启用（1=启用，0=禁用）',
    sort_order INT DEFAULT 0 COMMENT '排序权重',
    remark VARCHAR(200) DEFAULT NULL COMMENT '备注',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_device_brand (device_brand),
    INDEX idx_enabled (enabled)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='应用商店链接配置表';

-- 插入默认配置数据
INSERT INTO store_link_config (device_brand, brand_aliases, link_template, enabled, sort_order, remark) VALUES
('xiaomi', 'redmi,mi,小米', 'market://details?id={packageName}', 1, 1, '小米应用商店'),
('huawei', '华为,hw', 'appmarket://details?id={packageName}', 1, 2, '华为应用市场'),
('honor', '荣耀', 'honormarket://details?id={packageName}', 1, 3, '荣耀应用市场'),
('oppo', 'OPPO,欧泊', 'oppomarket://details?id={packageName}', 1, 4, 'OPPO应用商店')
ON DUPLICATE KEY UPDATE 
    brand_aliases = VALUES(brand_aliases),
    link_template = VALUES(link_template),
    enabled = VALUES(enabled),
    sort_order = VALUES(sort_order),
    remark = VALUES(remark),
    update_time = NOW();
