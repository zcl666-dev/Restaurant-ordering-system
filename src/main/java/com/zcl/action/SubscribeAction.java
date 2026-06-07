package com.zcl.action;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.opensymphony.xwork2.ActionSupport;
import com.zcl.dto.Result;
import com.zcl.dto.SubscribeSaveRequest;
import com.zcl.service.SubscribeService;
import org.apache.struts2.ServletActionContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Controller
@Scope("prototype")
public class SubscribeAction extends ActionSupport {

    @Autowired
    private SubscribeService subscribeService;

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

    public String save() {
        try {
            HttpServletRequest request = ServletActionContext.getRequest();
            SubscribeSaveRequest saveRequest = objectMapper.readValue(request.getInputStream(), SubscribeSaveRequest.class);
            Long userId = (Long) request.getAttribute("userId");
            subscribeService.saveSubscribe(userId, saveRequest);
            writeJson(Result.success("保存成功", null));
        } catch (Exception e) {
            writeJson(Result.error(500, "保存订阅失败: " + e.getMessage()));
        }
        return NONE;
    }
}
