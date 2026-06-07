package com.zcl.service;

import com.zcl.dao.AdminDao;
import com.zcl.dto.AdminLoginRequest;
import com.zcl.dto.AdminLoginResponse;
import com.zcl.entity.Admin;
import com.zcl.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@Transactional
public class AdminAuthService {

    @Autowired
    private AdminDao adminDao;

    @Autowired
    private JwtUtil jwtUtil;

    public AdminLoginResponse login(AdminLoginRequest request) {
        Admin admin = adminDao.findByUsername(request.getUsername());
        if (admin == null) {
            throw new RuntimeException("用户名或密码错误");
        }

        if (admin.getStatus() != 1) {
            throw new RuntimeException("账号已被禁用");
        }

        // 直接比较明文密码
        if (!request.getPassword().equals(admin.getPassword())) {
            throw new RuntimeException("用户名或密码错误");
        }

        // 更新最后登录时间
        admin.setLastLoginTime(LocalDateTime.now());
        adminDao.save(admin);

        String token = jwtUtil.generateAdminToken(admin.getId(), admin.getUsername(), admin.getRole());

        return AdminLoginResponse.builder()
                .token(token)
                .adminId(admin.getId())
                .username(admin.getUsername())
                .role(admin.getRole())
                .build();
    }

    public Admin getAdminById(Long adminId) {
        Admin admin = adminDao.findById(adminId);
        if (admin == null) {
            throw new RuntimeException("管理员不存在");
        }
        return admin;
    }
}
