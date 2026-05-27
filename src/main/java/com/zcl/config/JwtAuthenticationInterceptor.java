package com.zcl.config;

import com.zcl.util.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * JWT 认证拦截器
 * 用于验证请求中的 Token 是否有效
 */
@Component
public class JwtAuthenticationInterceptor implements HandlerInterceptor {

    private static final Logger log = LoggerFactory.getLogger(JwtAuthenticationInterceptor.class);

    @Autowired
    private JwtUtil jwtUtil;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 获取请求头中的 Token
        String token = request.getHeader("Authorization");

        // 如果没有 Token，直接放行（登录接口不需要验证）
        if (token == null || token.isEmpty()) {
            return true;
        }

        // 去除 "Bearer " 前缀
        if (token.startsWith("Bearer ")) {
            token = token.substring(7);
        }

        try {
            // 验证 Token 是否有效
            if (jwtUtil.validateToken(token)) {
                // 将用户信息存入请求属性，供后续使用
                String openId = jwtUtil.getOpenIdFromToken(token);
                Long userId = jwtUtil.getUserIdFromToken(token);
                String nickName = jwtUtil.getNickNameFromToken(token);

                request.setAttribute("openId", openId);
                request.setAttribute("userId", userId);
                request.setAttribute("nickName", nickName);

                log.debug("Token 验证成功: userId={}, openId={}", userId, openId);
                return true;
            } else {
                log.warn("Token 无效或已过期");
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.setContentType("application/json;charset=UTF-8");
                response.getWriter().write("{\"code\":401,\"message\":\"Token无效或已过期\",\"data\":null}");
                return false;
            }
        } catch (Exception e) {
            log.error("Token 验证失败", e);
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"code\":401,\"message\":\"Token验证失败\",\"data\":null}");
            return false;
        }
    }
}
