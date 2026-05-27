package com.zcl.service;

import com.zcl.entity.User;
import com.zcl.repository.UserRepository;
import com.zcl.util.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * 用户信息服务类
 * 用于获取当前登录用户的信息
 */
@Service
public class UserInfoService {

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private UserRepository userRepository;

    /**
     * 从请求中获取当前用户的 openId
     *
     * @return openId，如果未登录则返回 null
     */
    public String getCurrentOpenId() {
        try {
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes == null) {
                return null;
            }
            HttpServletRequest request = attributes.getRequest();
            return (String) request.getAttribute("openId");
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 从请求中获取当前用户的 ID
     *
     * @return userId，如果未登录则返回 null
     */
    public Long getCurrentUserId() {
        try {
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes == null) {
                return null;
            }
            HttpServletRequest request = attributes.getRequest();
            return (Long) request.getAttribute("userId");
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 从请求头中获取 Token 并解析出用户完整信息
     *
     * @return 用户实体，如果未登录或用户不存在则返回 null
     */
    public User getCurrentUser() {
        try {
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes == null) {
                return null;
            }
            HttpServletRequest request = attributes.getRequest();
            
            // 从请求头获取 Token
            String token = request.getHeader("Authorization");
            if (token == null || token.isEmpty()) {
                return null;
            }

            // 去除 "Bearer " 前缀
            if (token.startsWith("Bearer ")) {
                token = token.substring(7);
            }

            // 从 Token 中获取 openId
            String openId = jwtUtil.getOpenIdFromToken(token);
            if (openId == null) {
                return null;
            }

            // 从数据库查询用户信息
            return userRepository.findByOpenId(openId).orElse(null);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 验证用户是否已登录
     *
     * @return true-已登录，false-未登录
     */
    public boolean isAuthenticated() {
        return getCurrentOpenId() != null;
    }
}
