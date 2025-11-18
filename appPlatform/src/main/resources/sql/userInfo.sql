use app_platform;

CREATE TABLE IF NOT EXISTS user (
                                    id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                    username VARCHAR(50) NOT NULL UNIQUE,
                                    password VARCHAR(100) NOT NULL,
                                    role VARCHAR(20) NOT NULL DEFAULT 'USER'
);

INSERT INTO user (username, password, role)
VALUES ('admin', '$2a$10$Y50UoM8xW1lP5p1mHvQ2/uG5QJvVXv5h5T5XU5Q5J5x5X5v5X5v5X', 'ADMIN');

ALTER TABLE user
ADD COLUMN avatar VARCHAR(255);