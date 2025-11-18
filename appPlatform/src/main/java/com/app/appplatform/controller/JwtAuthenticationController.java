package com.app.appplatform.controller;

import com.app.appplatform.common.Result;
import com.app.appplatform.model.JwtRequest;
import com.app.appplatform.dto.UserInfoDto;
import com.app.appplatform.model.JwtResponse;
import com.app.appplatform.service.JwtAuthenticationService;
import jakarta.annotation.security.PermitAll;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor // 省略构造
public class JwtAuthenticationController {

    private final JwtAuthenticationService authenticationService;

    @PermitAll()
    @PostMapping("/login")
    public Result<JwtResponse> createAuthenticationToken(@RequestBody JwtRequest authenticationRequest) throws Exception {
        final String token = authenticationService
                .createAuthenticationToken(authenticationRequest)
                .getToken();

        return Result.success(new JwtResponse(token));
    }

    @GetMapping("/userinfo")
    public Result<UserInfoDto> getUserInfo() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = ((UserDetails) authentication.getPrincipal()).getUsername();
        UserInfoDto userInfo = authenticationService.getUserInfo(username);
        return Result.success(userInfo);
    }
}