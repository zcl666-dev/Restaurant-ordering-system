package com.zcl.action;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.opensymphony.xwork2.ActionSupport;
import com.zcl.dto.Result;
import com.zcl.entity.User;
import com.zcl.service.UserInfoService;
import org.apache.struts2.ServletActionContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.time.LocalDate;

@Controller
@Scope("prototype")
public class UserInfoAction extends ActionSupport {

    @Autowired
    private UserInfoService userInfoService;

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

    public String info() {
        try {
            HttpServletRequest request = ServletActionContext.getRequest();
            Long userId = (Long) request.getAttribute("userId");
            User user = userInfoService.getUserById(userId);
            writeJson(Result.success("获取成功", user));
        } catch (Exception e) {
            writeJson(Result.error(500, "获取用户信息失败: " + e.getMessage()));
        }
        return NONE;
    }

    public String update() {
        try {
            HttpServletRequest request = ServletActionContext.getRequest();
            Long userId = (Long) request.getAttribute("userId");

            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = request.getReader().readLine()) != null) {
                sb.append(line);
            }
            JsonNode json = objectMapper.readTree(sb.toString());

            String nickName = json.has("nickName") ? json.get("nickName").asText(null) : null;
            String avatarUrl = json.has("avatarUrl") ? json.get("avatarUrl").asText(null) : null;
            String phone = json.has("phone") ? json.get("phone").asText(null) : null;
            Integer gender = json.has("gender") && !json.get("gender").isNull() ? json.get("gender").asInt() : null;
            LocalDate birthday = null;
            if (json.has("birthday") && !json.get("birthday").isNull()) {
                birthday = LocalDate.parse(json.get("birthday").asText());
            }

            User user = userInfoService.updateProfile(userId, nickName, avatarUrl, phone, gender, birthday);
            writeJson(Result.success("保存成功", user));
        } catch (Exception e) {
            writeJson(Result.error(500, "更新个人信息失败: " + e.getMessage()));
        }
        return NONE;
    }

    public String checkLogin() {
        try {
            HttpServletRequest request = ServletActionContext.getRequest();
            Long userId = (Long) request.getAttribute("userId");
            if (userId != null) {
                writeJson(Result.success("已登录", true));
            } else {
                writeJson(Result.success("未登录", false));
            }
        } catch (Exception e) {
            writeJson(Result.success("未登录", false));
        }
        return NONE;
    }
}
