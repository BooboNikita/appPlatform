package com.app.appplatform.service.impl;

import com.app.appplatform.entity.User;
import com.app.appplatform.mapper.primary.UserMapper;
import com.app.appplatform.service.JwtUserDetailsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
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
        logger.info("找到用户: {}, 密码: {}, 角色: {}",
                user.getUsername(),
                user.getPassword(),  // 记录密码（仅用于调试）
                user.getAuthorities());
        return user;
    }
}
