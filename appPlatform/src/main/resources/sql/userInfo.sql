use app_platform;

drop IF EXISTS user_info;
create table user_info (
    id int primary key auto_increment,
    username varchar(255) not null,
    password varchar(255) not null,
    email varchar(255) not null,
    phone varchar(255) not null,
    create_time datetime not null,
    update_time datetime not null
);