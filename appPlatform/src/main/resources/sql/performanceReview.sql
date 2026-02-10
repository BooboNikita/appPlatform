use app_platform;

DROP TABLE IF EXISTS performance_review;
CREATE TABLE IF NOT EXISTS performance_review (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    dept_id VARCHAR(50) NOT NULL UNIQUE COMMENT '部门ID',
    name VARCHAR(100) COMMENT '组织名称',
    cover_image VARCHAR(500) COMMENT '封面图URL地址',
    deadline VARCHAR(20) COMMENT '截止时间，格式：yyyy-MM-dd HH:mm',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_dept_id (dept_id),
    INDEX idx_create_time (create_time),
    INDEX idx_update_time (update_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='绩效评估配置表';