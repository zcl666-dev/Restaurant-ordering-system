package com.zcl.action;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.opensymphony.xwork2.ActionSupport;
import com.zcl.dto.AdminInfoVO;
import com.zcl.dto.AdminLoginRequest;
import com.zcl.dto.AdminLoginResponse;
import com.zcl.dto.Result;
import com.zcl.entity.Admin;
import com.zcl.service.AdminAuthService;
import org.apache.struts2.ServletActionContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Controller
@Scope("prototype")
public class AdminAuthAction extends ActionSupport {

    @Autowired
    private AdminAuthService adminAuthService;

    private static final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

    private void writeJson(Result<?> result) {
        try {
            HttpServletResponse response = ServletActionContext.getResponse();
            response.setContentType("application/json;charset=UTF-8");
            objectMapper.writeValue(response.getOutputStream(), result);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String login() {
        try {
            HttpServletRequest request = ServletActionContext.getRequest();
            AdminLoginRequest loginRequest = objectMapper.readValue(request.getInputStream(), AdminLoginRequest.class);
            AdminLoginResponse loginResponse = adminAuthService.login(loginRequest);
            writeJson(Result.success("登录成功", loginResponse));
        } catch (Exception e) {
            writeJson(Result.error(500, e.getMessage()));
        }
        return NONE;
    }

    public String info() {
        try {
            HttpServletRequest request = ServletActionContext.getRequest();
            Long adminId = (Long) request.getAttribute("adminId");
            Admin admin = adminAuthService.getAdminById(adminId);
            // 转换为VO，不返回密码字段
            AdminInfoVO vo = AdminInfoVO.builder()
                    .id(admin.getId())
                    .username(admin.getUsername())
                    .realName(admin.getRealName())
                    .role(admin.getRole())
                    .status(admin.getStatus())
                    .lastLoginTime(admin.getLastLoginTime())
                    .createdAt(admin.getCreatedAt())
                    .build();
            writeJson(Result.success("获取成功", vo));
        } catch (Exception e) {
            writeJson(Result.error(500, "获取管理员信息失败: " + e.getMessage()));
        }
        return NONE;
    }
}
