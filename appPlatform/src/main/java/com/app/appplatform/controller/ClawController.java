package com.app.appplatform.controller;

import jakarta.annotation.security.PermitAll;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/claw")
public class ClawController {

    private static final Logger logger = LoggerFactory.getLogger(ClawController.class);

    /**
     * 接收字符串参数并打印出来
     * @param message 要打印的字符串消息
     * @return 包含打印消息的响应
     */
    @PostMapping("/message")
    @PermitAll
    public String printMessage(@RequestBody String message) {
        logger.info("接收到消息: {}", message);
        System.out.println("打印消息: " + message);
        
        return "消息已成功打印: " + message;
    }

    /**
     * 通过GET请求接收字符串参数并打印出来
     * @param message 要打印的字符串消息
     * @return 包含打印消息的响应
     */
    @GetMapping("/message")
    public String printMessageGet(@RequestParam String message) {
        logger.info("GET请求接收到消息: {}", message);
        System.out.println("GET打印消息: " + message);
        
        return "GET消息已成功打印: " + message;
    }
}
