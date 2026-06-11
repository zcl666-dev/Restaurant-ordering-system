package com.zcl.action;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.opensymphony.xwork2.ActionSupport;
import com.zcl.dto.Result;
import com.zcl.service.AdminProductService;
import org.apache.struts2.ServletActionContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;

import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Map;

/**
 * 规格组管理接口（管理员端）
 */
@Controller
@Scope("prototype")
public class OptionGroupAction extends ActionSupport {

    @Autowired
    private AdminProductService adminProductService;

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

    /**
     * 获取所有启用的规格组列表
     * GET /api_admin_option_groups.action
     */
    public String list() {
        try {
            List<Map<String, Object>> groupList = adminProductService.getAllEnabledOptionGroups();
            writeJson(Result.success("获取成功", groupList));
        } catch (Exception e) {
            writeJson(Result.error(500, "获取规格组列表失败: " + e.getMessage()));
        }
        return NONE;
    }
}
