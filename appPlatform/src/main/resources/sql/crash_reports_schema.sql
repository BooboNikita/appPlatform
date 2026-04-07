-- =============================================
-- Flutter Crash SDK Database Schema
-- Only contains crash reports table
-- =============================================
DROP TABLE IF EXISTS crash_reports;
-- Crash reports table
CREATE TABLE crash_reports (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT 'Primary key ID',
    crash_id VARCHAR(255) UNIQUE NOT NULL COMMENT 'Crash unique identifier',
    app_id VARCHAR(100) NOT NULL COMMENT 'Application ID',
    user_id VARCHAR(100) COMMENT 'User ID',
    session_id VARCHAR(255) NOT NULL COMMENT 'Session ID',
    crash_type ENUM('error', 'exception', 'fatal', 'anr') NOT NULL COMMENT 'Crash type',
    message TEXT NOT NULL COMMENT 'Error message',
    stack_trace LONGTEXT NOT NULL COMMENT 'Stack trace information',
    app_version VARCHAR(50) NOT NULL COMMENT 'Application version',
    app_build_number VARCHAR(20) COMMENT 'Application build number',
    device_model VARCHAR(100) COMMENT 'Device model',
    device_brand VARCHAR(50) COMMENT 'Device brand',
    os_version VARCHAR(50) COMMENT 'Operating system version',
    platform VARCHAR(20) COMMENT 'Platform type (iOS/Android)',
    screen_resolution VARCHAR(20) COMMENT 'Screen resolution',
    total_memory INT COMMENT 'Total memory (MB)',
    available_memory INT COMMENT 'Available memory (MB)',
    network_type VARCHAR(20) COMMENT 'Network type',
    battery_level INT COMMENT 'Battery level (0-100)',
    custom_data JSON COMMENT 'Custom business data',
    crash_timestamp DATETIME NOT NULL COMMENT 'Crash occurrence time',
    report_timestamp DATETIME NOT NULL COMMENT 'Report time',
    sdk_version VARCHAR(20) NOT NULL COMMENT 'SDK version',
    deleted TINYINT(1) DEFAULT 0 COMMENT 'Logical deletion flag (0=not deleted, 1=deleted)',
    deleted_at TIMESTAMP NULL COMMENT 'Deletion time',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT 'Creation time',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Update time'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Crash reports table';

-- =============================================
-- Index Creation
-- =============================================

-- crash_reports table indexes
CREATE INDEX idx_crash_reports_app_id ON crash_reports(app_id);
CREATE INDEX idx_crash_reports_user_id ON crash_reports(user_id);
CREATE INDEX idx_crash_reports_crash_timestamp ON crash_reports(crash_timestamp);
CREATE INDEX idx_crash_reports_crash_type ON crash_reports(crash_type);
CREATE INDEX idx_crash_reports_session_id ON crash_reports(session_id);
CREATE INDEX idx_crash_reports_app_version ON crash_reports(app_version);
CREATE INDEX idx_crash_reports_platform ON crash_reports(platform);
CREATE INDEX idx_crash_reports_created_at ON crash_reports(created_at);
CREATE INDEX idx_crash_reports_deleted ON crash_reports(deleted);

-- Batch query optimization indexes
CREATE INDEX idx_crash_reports_app_time ON crash_reports(app_id, crash_timestamp);
CREATE INDEX idx_crash_reports_user_time ON crash_reports(user_id, crash_timestamp);
CREATE INDEX idx_crash_reports_type_time ON crash_reports(crash_type, crash_timestamp);

-- =============================================
-- Performance Optimization Suggestions
-- =============================================

-- 1. Periodic cleanup of old data (recommend creating scheduled tasks)
-- UPDATE crash_reports SET deleted = 1 WHERE created_at < DATE_SUB(NOW(), INTERVAL 90 DAY);

-- 2. Partitioned table (if data volume is large, consider partitioning by time)
-- ALTER TABLE crash_reports PARTITION BY RANGE (YEAR(created_at)) (
--     PARTITION p2023 VALUES LESS THAN (2024),
--     PARTITION p2024 VALUES LESS THAN (2025),
--     PARTITION p2025 VALUES LESS THAN (2026),
--     PARTITION p_future VALUES LESS THAN MAXVALUE
-- );

-- 3. Monitor table size and performance
-- SELECT 
--     table_name,
--     ROUND(((data_length + index_length) / 1024 / 1024), 2) AS 'Size (MB)',
--     table_rows
-- FROM information_schema.tables 
-- WHERE table_schema = DATABASE() 
-- AND table_name = 'crash_reports';
