package com.zcl.controller;

import com.zcl.dto.AdminLoginRequest;
import com.zcl.dto.AdminLoginResponse;
import com.zcl.dto.Result;
import com.zcl.entity.Admin;
import com.zcl.service.AdminAuthService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
public class AdminAuthController {

    private final AdminAuthService adminAuthService;

    public AdminAuthController(AdminAuthService adminAuthService) {
        this.adminAuthService = adminAuthService;
    }

    @PostMapping("/login")
    public ResponseEntity<Result<AdminLoginResponse>> login(@RequestBody AdminLoginRequest request) {
        if (request.getUsername() == null || request.getUsername().isEmpty()) {
            return ResponseEntity.badRequest().body(Result.error(400, "用户名不能为空"));
        }
        if (request.getPassword() == null || request.getPassword().isEmpty()) {
            return ResponseEntity.badRequest().body(Result.error(400, "密码不能为空"));
        }

        try {
            AdminLoginResponse response = adminAuthService.login(request);
            return ResponseEntity.ok(Result.success("登录成功", response));
        } catch (RuntimeException e) {
            return ResponseEntity.ok(Result.error(401, e.getMessage()));
        }
    }

    @GetMapping("/info")
    public ResponseEntity<Result<Map<String, Object>>> getInfo(HttpServletRequest request) {
        Long adminId = (Long) request.getAttribute("adminId");
        Admin admin = adminAuthService.getAdminById(adminId);

        Map<String, Object> info = new HashMap<>();
        info.put("adminId", admin.getId());
        info.put("username", admin.getUsername());
        info.put("realName", admin.getRealName());
        info.put("role", admin.getRole());

        return ResponseEntity.ok(Result.success("获取成功", info));
    }
}
