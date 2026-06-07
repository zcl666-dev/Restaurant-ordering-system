package com.zcl.action;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.opensymphony.xwork2.ActionSupport;
import com.zcl.dto.AdminUserVO;
import com.zcl.dto.PageResult;
import com.zcl.dto.Result;
import com.zcl.service.AdminUserService;
import org.apache.struts2.ServletActionContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;

import javax.servlet.http.HttpServletResponse;

@Controller
@Scope("prototype")
public class AdminUserAction extends ActionSupport {

    @Autowired
    private AdminUserService adminUserService;

    private static final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

    private Long id;
    private int page = 0;
    private int size = 10;
    private String keyword;
    private AdminUserVO userVO;

    /**
     * 直接使用Jackson写出JSON，绕过Struts2 JSON插件的序列化问题
     */
    private void writeJson(Result<?> result) {
        try {
            HttpServletResponse response = ServletActionContext.getResponse();
            response.setContentType("application/json;charset=UTF-8");
            objectMapper.writeValue(response.getOutputStream(), result);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String list() {
        try {
            PageResult<AdminUserVO> pageResult = adminUserService.getUserList(page, size, keyword);
            writeJson(Result.success("获取成功", pageResult));
        } catch (Exception e) {
            writeJson(Result.error(500, "获取用户列表失败: " + e.getMessage()));
        }
        return NONE; // 已手动写出响应，返回NONE让Struts2不再处理
    }

    public String detail() {
        try {
            AdminUserVO user = adminUserService.getUserDetail(id);
            writeJson(Result.success("获取成功", user));
        } catch (Exception e) {
            writeJson(Result.error(404, e.getMessage()));
        }
        return NONE;
    }

    public String update() {
        try {
            adminUserService.updateUser(id, userVO);
            writeJson(Result.success("更新成功", null));
        } catch (Exception e) {
            writeJson(Result.error(500, "更新用户失败: " + e.getMessage()));
        }
        return NONE;
    }

    public String delete() {
        try {
            adminUserService.deleteUser(id);
            writeJson(Result.success("删除成功", null));
        } catch (Exception e) {
            writeJson(Result.error(500, "删除用户失败: " + e.getMessage()));
        }
        return NONE;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public int getPage() { return page; }
    public void setPage(int page) { this.page = page; }
    public int getSize() { return size; }
    public void setSize(int size) { this.size = size; }
    public String getKeyword() { return keyword; }
    public void setKeyword(String keyword) { this.keyword = keyword; }
    public AdminUserVO getUserVO() { return userVO; }
    public void setUserVO(AdminUserVO userVO) { this.userVO = userVO; }
}
