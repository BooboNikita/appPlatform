package com.app.crash;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication(scanBasePackages = {"com.app.crash", "com.app.common"}, exclude = {SecurityAutoConfiguration.class})
@EnableDiscoveryClient
public class CrashServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(CrashServiceApplication.class, args);
    }
}
