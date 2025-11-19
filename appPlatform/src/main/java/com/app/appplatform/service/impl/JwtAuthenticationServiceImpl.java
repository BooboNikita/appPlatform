package com.app.appplatform.service.impl;

import com.app.appplatform.dto.UserInfoDto;
import com.app.appplatform.entity.User;
import com.app.appplatform.mapper.UserMapper;
import com.app.appplatform.model.JwtRequest;
import com.app.appplatform.model.JwtResponse;
import com.app.appplatform.model.RegisterRequest;
import com.app.appplatform.service.JwtAuthenticationService;
import com.app.appplatform.service.JwtUserDetailsService;
import com.app.appplatform.util.JwtTokenUtil;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class JwtAuthenticationServiceImpl implements JwtAuthenticationService {
    private final AuthenticationManager authenticationManager;
    private final JwtTokenUtil jwtTokenUtil;
    private final JwtUserDetailsService userDetailsService;
    private final UserMapper userMapper;
    private static final Logger logger = LoggerFactory.getLogger(JwtAuthenticationServiceImpl.class);

    @Autowired
    public JwtAuthenticationServiceImpl(
            AuthenticationManager authenticationManager,
            JwtTokenUtil jwtTokenUtil,
            JwtUserDetailsService userDetailsService,
            UserMapper userMapper) {
        this.authenticationManager = authenticationManager;
        this.jwtTokenUtil = jwtTokenUtil;
        this.userDetailsService = userDetailsService;
        this.userMapper = userMapper;
    }

    @Override
    public JwtResponse createAuthenticationToken(JwtRequest authenticationRequest) throws Exception {
        logger.info("开始认证用户: {}", authenticationRequest.getUsername());

        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            authenticationRequest.getUsername(),
                            authenticationRequest.getPassword()
                    )
            );
        } catch (BadCredentialsException e) {
            throw new Exception("用户名或密码错误", e);
        }

        final UserDetails userDetails = userDetailsService
                .loadUserByUsername(authenticationRequest.getUsername());

        final String token = jwtTokenUtil.generateToken(userDetails);

        return new JwtResponse(token);
    }

    @Override
    public UserInfoDto getUserInfo(String username) {
        User user = userMapper.findByUsername(username);
        if (user == null) {
            throw new UsernameNotFoundException("User not found with username: " + username);
        }

        return new UserInfoDto(
            user.getUsername(),
            user.getAvatar(),
            user.getRole()
        );
    }

    @Override
    public UserInfoDto registerUser(RegisterRequest registerRequest) {
        User user = new User();
        user.setUsername(registerRequest.getUsername());

        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        user.setPassword(encoder.encode(registerRequest.getPassword()));

        user.setRole("USER");
        user.setAvatar(registerRequest.getAvatar());
        userMapper.insertUser(user);
        return getUserInfo(registerRequest.getUsername());
    }
}