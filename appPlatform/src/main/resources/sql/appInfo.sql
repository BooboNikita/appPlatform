# create database app_platform;
use app_platform;


drop table if exists app_info;
create table app_info
(
    id            int primary key auto_increment,
    appName       varchar(255) not null,
    packageName   varchar(255) not null,
    version       varchar(255) not null,
    buildNumber   long not null,
    features      varchar(255) not null,
    isBeta        boolean not null default false,
    path          varchar(255) not null,
    createTime    TIMESTAMP     DEFAULT CURRENT_TIMESTAMP,
    downloadTimes int          not null,
    size          varchar(255) not null,
    deleted       boolean      not null default false,
    showUpdatePopup boolean    not null default false,
    forceUpdate   boolean      not null default false
);

ALTER TABLE app_info
    MODIFY COLUMN id INT AUTO_INCREMENT,
    ADD PRIMARY KEY (id);

-- 处理反斜杠路径 (Windows) - 使用四个反斜杠
UPDATE app_info
SET path = SUBSTRING_INDEX(path, '\\\\', -1)
WHERE path LIKE '%\\\\%';

-- 处理正斜杠路径 (Linux/Unix)
UPDATE app_info
SET path = SUBSTRING_INDEX(path, '/', -1)
WHERE path LIKE '%/%';