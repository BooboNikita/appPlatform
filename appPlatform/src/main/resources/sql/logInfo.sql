use app_platform;

drop table if exists log_info;
create table log_info (
    id int primary key auto_increment,
    username varchar(255) not null,
    nickname varchar(255) not null,
    upload_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    path TEXT not null,
    app_name varchar(255) not null,
    version varchar(255) not null,
    imageUrls TEXT not null,
    problem TEXT not null
);

ALTER TABLE log_info
    ADD COLUMN imageUrls VARCHAR(255) NOT NULL DEFAULT '',
    ADD COLUMN problem VARCHAR(255) NOT NULL DEFAULT '';

ALTER TABLE log_info
    MODIFY COLUMN path TEXT,
    MODIFY COLUMN imageUrls TEXT,
    MODIFY COLUMN problem TEXT;