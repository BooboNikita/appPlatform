package com.app.appplatform;

import de.codecentric.boot.admin.server.config.EnableAdminServer;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@EnableAdminServer
public class AppPlatformApplication {

    public static void main(String[] args) {
        SpringApplication.run(AppPlatformApplication.class, args);
    }

}
