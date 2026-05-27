package com.zcl.controller;

import com.zcl.dto.Result;
import com.zcl.entity.User;
import com.zcl.service.UserInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 用户信息控制器
 * 演示如何使用 JWT Token 进行身份验证
 */
@RestController
@RequestMapping("/api/user")
public class UserInfoController {

    @Autowired
    private UserInfoService userInfoService;

    /**
     * 获取当前登录用户信息
     * 需要在请求头中携带有效的 JWT Token
     * 
     * 请求示例：
     * GET /api/user/info
     * Header: Authorization: Bearer <your_jwt_token>
     */
    @GetMapping("/info")
    public ResponseEntity<Result<User>> getUserInfo() {
        // 从 Token 中获取用户信息
        User user = userInfoService.getCurrentUser();
        
        if (user == null) {
            return ResponseEntity.status(401)
                    .body(Result.error(401, "未登录或Token已过期"));
        }

        // 返回用户信息（不返回敏感字段）
        user.setOpenId(null); // 隐藏 openId
        
        return ResponseEntity.ok(Result.success("获取成功", user));
    }

    /**
     * 检查登录状态
     * 
     * 请求示例：
     * GET /api/user/check-login
     * Header: Authorization: Bearer <your_jwt_token>
     */
    @GetMapping("/check-login")
    public ResponseEntity<Result<Boolean>> checkLogin() {
        boolean isAuthenticated = userInfoService.isAuthenticated();
        return ResponseEntity.ok(Result.success("检查成功", isAuthenticated));
    }
}
