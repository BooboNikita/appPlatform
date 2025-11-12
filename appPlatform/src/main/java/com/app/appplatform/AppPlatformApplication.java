package com.app.appplatform;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.app.appplatform.mapper")
public class AppPlatformApplication {

    public static void main(String[] args) {
        SpringApplication.run(AppPlatformApplication.class, args);
    }

}
