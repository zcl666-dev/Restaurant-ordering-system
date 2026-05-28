package com.zcl.service;

import com.zcl.dto.AdminLoginRequest;
import com.zcl.dto.AdminLoginResponse;
import com.zcl.entity.Admin;
import com.zcl.repository.AdminRepository;
import com.zcl.util.JwtUtil;
import org.mindrot.jbcrypt.BCrypt;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class AdminAuthService {

    private final AdminRepository adminRepository;
    private final JwtUtil jwtUtil;

    public AdminAuthService(AdminRepository adminRepository, JwtUtil jwtUtil) {
        this.adminRepository = adminRepository;
        this.jwtUtil = jwtUtil;
    }

    public AdminLoginResponse login(AdminLoginRequest request) {
        Admin admin = adminRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new RuntimeException("用户名或密码错误"));

        if (admin.getStatus() != 1) {
            throw new RuntimeException("账号已被禁用");
        }

        if (!BCrypt.checkpw(request.getPassword(), admin.getPassword())) {
            throw new RuntimeException("用户名或密码错误");
        }

        // 更新最后登录时间
        admin.setLastLoginTime(LocalDateTime.now());
        adminRepository.save(admin);

        String token = jwtUtil.generateAdminToken(admin.getId(), admin.getUsername(), admin.getRole());

        return AdminLoginResponse.builder()
                .token(token)
                .adminId(admin.getId())
                .username(admin.getUsername())
                .role(admin.getRole())
                .build();
    }

    public Admin getAdminById(Long adminId) {
        return adminRepository.findById(adminId)
                .orElseThrow(() -> new RuntimeException("管理员不存在"));
    }
}
