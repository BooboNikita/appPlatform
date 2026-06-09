package com.app.auth.service;

import com.app.auth.dto.UserInfoDto;
import com.app.auth.model.JwtRequest;
import com.app.auth.model.JwtResponse;
import com.app.auth.model.RegisterRequest;

public interface JwtAuthenticationService {
    JwtResponse createAuthenticationToken(JwtRequest authenticationRequest) throws Exception;

    UserInfoDto getUserInfo(String username);

    UserInfoDto registerUser(RegisterRequest registerRequest);
}
