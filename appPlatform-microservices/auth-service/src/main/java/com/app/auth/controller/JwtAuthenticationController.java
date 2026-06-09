package com.app.auth.controller;

import com.app.auth.dto.UserInfoDto;
import com.app.auth.model.JwtRequest;
import com.app.auth.model.JwtResponse;
import com.app.auth.model.RegisterRequest;
import com.app.auth.service.JwtAuthenticationService;
import com.app.common.Result;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class JwtAuthenticationController {

    private final JwtAuthenticationService authenticationService;

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

    @PostMapping("/register")
    public Result<UserInfoDto> registerUser(@Valid @RequestBody RegisterRequest registerRequest) {
        UserInfoDto newUser = authenticationService.registerUser(registerRequest);
        return Result.success(newUser);
    }
}
