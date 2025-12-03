package com.app.appplatform.filter;

import com.app.appplatform.service.JwtUserDetailsService;
import com.app.appplatform.util.JwtTokenUtil;
import jakarta.annotation.security.PermitAll;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerExecutionChain;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

@Component
public class JwtRequestFilter extends OncePerRequestFilter {

    private static final List<Pattern> PUBLIC_PATTERNS = Arrays.asList(
            Pattern.compile("^/api/auth/.*"),
            Pattern.compile("^/v2/api-docs$"),
            Pattern.compile("^/swagger-resources/.*"),
            Pattern.compile("^/swagger-ui/.*"),
            Pattern.compile("^/webjars/.*"),
            Pattern.compile("^/favicon\\.ico$"),
            Pattern.compile("^/actuator/.*"),
            Pattern.compile("^/.well-known/.*"),
            Pattern.compile("^/ws.*")
    );

    @Autowired
    private RequestMappingHandlerMapping requestMappingHandlerMapping;

    @Autowired
    private JwtUserDetailsService jwtUserDetailsService;

    @Autowired
    private JwtTokenUtil jwtTokenUtil;

    private boolean isPublicEndpoint(HttpServletRequest request) {
        try {
            // 获取请求对应的处理器方法
            HandlerExecutionChain handler = requestMappingHandlerMapping.getHandler(request);

            // 检查是否是需要放行的请求
            if (handler != null && handler.getHandler() instanceof HandlerMethod handlerMethod) {

                // 检查方法上是否有 @PermitAll 或 @PreAuthorize("permitAll()") 注解
                boolean isPermitAll = handlerMethod.hasMethodAnnotation(PermitAll.class) ||
                        (handlerMethod.hasMethodAnnotation(PreAuthorize.class) &&
                                handlerMethod.getMethodAnnotation(PreAuthorize.class).value().equals("permitAll()"));

                if (isPermitAll) {
                    return true;
                }
            }

            String requestURI = request.getRequestURI();
            logger.debug("检查公共端点: " + requestURI);
            return PUBLIC_PATTERNS.stream()
                    .anyMatch(pattern -> pattern.matcher(requestURI).matches());
        } catch (Exception e) {
            // 记录错误日志
            logger.error("检查公共端点时出错: " + e.getMessage(), e);
            // 如果发生异常，默认不认为是公共端点，让后续的认证逻辑处理
            return false;
        }
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException {
        final String requestTokenHeader = request.getHeader("Authorization");
        String username = null;
        String jwtToken = null;

        try {
            // 检查是否是公共端点
            if (isPublicEndpoint(request)) {
                SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(
                        "anonymous", null, List.of()));
                chain.doFilter(request, response);
                return;
            }

            // 检查 Token 是否存在且格式正确
            if (requestTokenHeader == null || !requestTokenHeader.startsWith("Bearer ")) {
                throw new AuthenticationCredentialsNotFoundException("未提供有效的认证信息");
            }

            jwtToken = requestTokenHeader.substring(7);
            username = jwtTokenUtil.extractUsername(jwtToken);
            logger.debug("从Token中提取用户名: " + username);

            if (username != null) {
                UserDetails userDetails = this.jwtUserDetailsService.loadUserByUsername(username);

                if (jwtTokenUtil.validateToken(jwtToken)) {
                    UsernamePasswordAuthenticationToken authenticationToken =
                            new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                    authenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authenticationToken);
                }
            }

            chain.doFilter(request, response);

        } catch (Exception e) {
            // 将异常交给 Spring Security 处理
//            SecurityContextHolder.clearContext();
//            request.setAttribute("javax.servlet.error.exception", e);
//            request.getRequestDispatcher("/error/unauthorized").forward(request, response);
            // 或者直接抛出 AuthenticationException
            throw new BadCredentialsException("认证失败: " + e.getMessage(), e);
        }
    }
}