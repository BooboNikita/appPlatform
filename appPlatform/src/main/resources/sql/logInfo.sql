use app_platform;

drop table if exists log_info;
create table log_info (
    id int primary key auto_increment,
    username varchar(255) not null,
    nickname varchar(255) not null,
    upload_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    path varchar(255) not null,
    app_name varchar(255) not null,
    version varchar(255) not null
);
