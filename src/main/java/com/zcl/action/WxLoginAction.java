package com.zcl.action;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.opensymphony.xwork2.ActionSupport;
import com.zcl.dto.Result;
import com.zcl.dto.WxLoginRequest;
import com.zcl.dto.WxLoginResponse;
import com.zcl.service.WxLoginService;
import org.apache.struts2.ServletActionContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Controller
@Scope("prototype")
public class WxLoginAction extends ActionSupport {

    @Autowired
    private WxLoginService wxLoginService;

    private static final ObjectMapper objectMapper = new ObjectMapper();

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
            WxLoginRequest loginRequest = objectMapper.readValue(request.getInputStream(), WxLoginRequest.class);
            WxLoginResponse loginResponse = wxLoginService.login(loginRequest);
            writeJson(Result.success("登录成功", loginResponse));
        } catch (Exception e) {
            writeJson(Result.error(500, e.getMessage()));
        }
        return NONE;
    }
}
