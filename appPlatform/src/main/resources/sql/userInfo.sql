use app_platform;

DROP table if exists user;
CREATE TABLE IF NOT EXISTS user (
                                    id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                    username VARCHAR(50) NOT NULL UNIQUE,
                                    password VARCHAR(100) NOT NULL,
                                    role VARCHAR(20) NOT NULL DEFAULT 'USER',
                                    avatar VARCHAR(255)
);

INSERT INTO user (username, password, role, avatar)
VALUES ('admin', '$2a$10$RKQHFEWWtI0Ch9qrlCCNtOCqj3B1IzWRQCmMl/RsVaIC1v026uMtm', 'ADMIN', 'http://172.31.101.166:8008/static/png/person-img-BRBJlwLp.png');