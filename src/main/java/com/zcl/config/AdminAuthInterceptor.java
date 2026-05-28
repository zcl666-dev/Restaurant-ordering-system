package com.zcl.config;

import com.zcl.util.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class AdminAuthInterceptor implements HandlerInterceptor {

    private static final Logger log = LoggerFactory.getLogger(AdminAuthInterceptor.class);

    @Autowired
    private JwtUtil jwtUtil;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String token = request.getHeader("Authorization");

        if (token == null || token.isEmpty()) {
            writeUnauthorized(response, "请先登录管理后台");
            return false;
        }

        if (token.startsWith("Bearer ")) {
            token = token.substring(7);
        }

        try {
            if (!jwtUtil.validateToken(token)) {
                writeUnauthorized(response, "Token无效或已过期");
                return false;
            }

            String tokenType = jwtUtil.getTokenTypeFromToken(token);
            if (!"admin".equals(tokenType)) {
                writeUnauthorized(response, "无权访问管理后台");
                return false;
            }

            Long adminId = jwtUtil.getAdminIdFromToken(token);
            String username = jwtUtil.getUsernameFromToken(token);
            String role = jwtUtil.getRoleFromToken(token);

            request.setAttribute("adminId", adminId);
            request.setAttribute("adminUsername", username);
            request.setAttribute("adminRole", role);

            log.debug("Admin Token 验证成功: adminId={}, username={}", adminId, username);
            return true;
        } catch (Exception e) {
            log.error("Admin Token 验证失败", e);
            writeUnauthorized(response, "Token验证失败");
            return false;
        }
    }

    private void writeUnauthorized(HttpServletResponse response, String message) throws Exception {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write("{\"code\":401,\"message\":\"" + message + "\",\"data\":null}");
    }
}
