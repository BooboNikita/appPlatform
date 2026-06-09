package com.app.auth.service.impl;

import com.app.auth.entity.User;
import com.app.auth.mapper.UserMapper;
import com.app.auth.service.JwtUserDetailsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class JwtUserDetailsServiceImpl implements JwtUserDetailsService {
    @Autowired
    private UserMapper userMapper;

    private static final Logger logger = LoggerFactory.getLogger(JwtUserDetailsServiceImpl.class);

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        logger.info("尝试加载用户: {}", username);
        User user = userMapper.findByUsername(username);
        if (user == null) {
            logger.error("用户未找到: {}", username);
            throw new UsernameNotFoundException("User not found with username: " + username);
        }
        logger.info("找到用户: {}, 角色: {}", user.getUsername(), user.getAuthorities());
        return user;
    }
}
