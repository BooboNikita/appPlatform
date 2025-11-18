package com.app.appplatform.service;

import com.app.appplatform.dto.UserInfoDto;
import com.app.appplatform.model.JwtRequest;
import com.app.appplatform.model.JwtResponse;

public interface JwtAuthenticationService {
    JwtResponse createAuthenticationToken(JwtRequest authenticationRequest) throws Exception;
    
    UserInfoDto getUserInfo(String username);
}