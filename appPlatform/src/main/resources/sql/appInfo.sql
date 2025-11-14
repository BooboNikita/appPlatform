create database app_platform;
use app_platform;


drop table app_info;
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
    deleted       boolean      not null default false
);

