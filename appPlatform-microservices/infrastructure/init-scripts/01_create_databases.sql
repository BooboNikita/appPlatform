-- 创建所有微服务数据库
-- 执行方式: mysql -u root -p < 01_create_databases.sql

-- 1. 认证服务数据库
CREATE DATABASE IF NOT EXISTS `auth_db`
    DEFAULT CHARACTER SET utf8mb4
    DEFAULT COLLATE utf8mb4_unicode_ci;

-- 2. 应用管理服务数据库
CREATE DATABASE IF NOT EXISTS `app_db`
    DEFAULT CHARACTER SET utf8mb4
    DEFAULT COLLATE utf8mb4_unicode_ci;

-- 3. 日志服务数据库
CREATE DATABASE IF NOT EXISTS `log_db`
    DEFAULT CHARACTER SET utf8mb4
    DEFAULT COLLATE utf8mb4_unicode_ci;

-- 4. 事件追踪服务数据库
CREATE DATABASE IF NOT EXISTS `event_db`
    DEFAULT CHARACTER SET utf8mb4
    DEFAULT COLLATE utf8mb4_unicode_ci;

-- 5. 崩溃报告服务数据库
CREATE DATABASE IF NOT EXISTS `crash_db`
    DEFAULT CHARACTER SET utf8mb4
    DEFAULT COLLATE utf8mb4_unicode_ci;

-- 6. 商店链接服务数据库
CREATE DATABASE IF NOT EXISTS `store_db`
    DEFAULT CHARACTER SET utf8mb4
    DEFAULT COLLATE utf8mb4_unicode_ci;

-- 7. 绩效评估服务数据库
CREATE DATABASE IF NOT EXISTS `perf_db`
    DEFAULT CHARACTER SET utf8mb4
    DEFAULT COLLATE utf8mb4_unicode_ci;

-- 8. 配置服务数据库
CREATE DATABASE IF NOT EXISTS `config_db`
    DEFAULT CHARACTER SET utf8mb4
    DEFAULT COLLATE utf8mb4_unicode_ci;

-- 显示创建结果
SHOW DATABASES LIKE '%_db';
