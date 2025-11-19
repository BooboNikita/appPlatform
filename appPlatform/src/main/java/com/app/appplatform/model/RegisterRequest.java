package com.app.appplatform.model;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class RegisterRequest {
    @NotBlank(message = "用户名不能为空")
    private String username;
    
    @NotBlank(message = "密码不能为空")
    private String password;
    
    private String role = "USER"; // 默认角色为USER
    
    private String avatar = "http://172.31.101.166:8008/static/png/person-img-BRBJlwLp.png"; // 用户头像URL
}
