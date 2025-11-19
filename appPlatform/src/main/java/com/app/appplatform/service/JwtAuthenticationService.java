package com.app.appplatform.service;

import com.app.appplatform.dto.UserInfoDto;
import com.app.appplatform.model.JwtRequest;
import com.app.appplatform.model.JwtResponse;
import com.app.appplatform.model.RegisterRequest;

public interface JwtAuthenticationService {
    JwtResponse createAuthenticationToken(JwtRequest authenticationRequest) throws Exception;
    
    UserInfoDto getUserInfo(String username);
    
    /**
     * 注册新用户
     * @param registerRequest 注册请求对象
     * @return 注册成功的用户信息
     */
    UserInfoDto registerUser(RegisterRequest registerRequest);
}